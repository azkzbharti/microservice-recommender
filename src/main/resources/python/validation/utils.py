"""*******************************************************************************
* Licensed Materials - Property of IBM
* (c) Copyright IBM Corporation ${year}. All Rights Reserved.
*
* Note to U.S. Government Users Restricted Rights:
* Use, duplication or disclosure restricted by GSA ADP Schedule
* Contract with IBM Corp.
*******************************************************************************"""

"""
Author: Utkarsh Desai, Raunak Sinha
Maintenance: Utkarsh Desai
"""

"""
Several utilities used by other classes in performing partition validation
"""

import numpy as np

def find_node_type(data, id_val):
	nodes = data["nodes"]
	for i in nodes:
		if i["id"] == id_val:
			if i["entity_type"] == 'class':
				return 1
			else:
				return 0
	print ("Something is wrong. Cannot find node type of node id:" ,id_val)

def find_node(data, id_val):
	"""
	Finding the label of graph nodes given the node id
	Input: ID of node
	Output: Label of node
	"""
	nodes = data["nodes"]
	for i in nodes:
		if i["id"] == id_val:
			return i["label"]
	print ("Something is wrong. Unable to find node with id_val:", id_val)

def find_node_id(data, name):
	"""
	Finding the id of graph nodes given the node name
	Input: ID of node
	Output: Label of node
	"""
	nodes = data["nodes"]
	for i in nodes:
		if i["label"] == name:
			return i["id"]
	print ("Something is wrong. Unable to find node with name:", name)

def check_community(i,j, comm):
	flag = 0
	for k in comm:
		if i in k and j in k:
			flag = 1
	return flag

def get_clusters_and_edges(data):
	for i in data["edges"]:
		if i["type"] == "inter_class_connections":
			data_edge = i["relationship"]
			break
	edge = {}
	for i in data_edge:
		key = find_node(data, i["properties"]["start"])+"--"+find_node(data, i["properties"]["end"])
		edge[key] = int(i["frequency"])

	cluster_process = data["clusters"]
	cluster_groups = []
	for i in cluster_process:
		temp = []
		for j in i["nodes"]:
			if find_node_type(data, j):
				temp.append(find_node(data, j))
		cluster_groups.append(temp)

	clusters = {}
	for i,j in enumerate(cluster_groups):
		temp = {}
		temp['classes'] = j
		clusters[str(i)] = temp

	return clusters, edge

def compute_cohesion_coupling(clusters, edges):
	result = clusters
	# scoh: structural cohesiveness of a service
	scoh = []

	# scop: coupling between service
	scop = np.empty([len(result), len(result)], dtype=float)

	for _, value in result.items():
		n_cls = len(value['classes'])
		mu = 0
		for i in range(n_cls-1):
			for j in range(i, n_cls):
				c1 = value['classes'][i]
				c2 = value['classes'][j]
				if c1 + "--" + c2 in edges  or c2 +"--" + c1 in edges :
					mu += 1
		scoh.append(mu * 1.0/(n_cls * n_cls))

	for key1, value1 in result.items():
		for key2, value2 in result.items():
			sigma = 0
			if key1 != key2:
				c_i = value1['classes']
				c_j = value2['classes']
				for i in range(len(c_i)):
					for j in range(len(c_j)):
						c1 = c_i[i]
						c2 = c_j[j]
						if c1 + "--" + c2 in edges or c2 + "--" + c1 in edges:
							sigma += 1
				scop[int(key1)][int(key2)] = sigma * 1.0 / (2 * len(c_i) * len(c_j))
	"""Cohesion"""
	p1 = sum(scoh) * 1.0 / len(scoh)

	"""Coupling"""
	p2 = 0
	for i in range(len(scop)):
		for j in range(len(scop[0])):
			if i != j:
				p2 += scop[i][j]

	if len(scop) == 1:
		p2 = 0
	else:
		p2 = p2 / len(scop) / (len(scop) - 1) * 2

	return p1, p2

def reverse_entrypoint(eps):
	entrypoints = eps
	rev_ep = {}
	for ep in entrypoints.keys():
	    methods = entrypoints[ep]
	    for m in methods:
	        c = ".".join(m.split(".")[:-1])
	        #print("c:", c)
	        if rev_ep.get(c) is None:
	            rev_ep[c] = []
	        rev_ep[c].append(ep)
        #print("-", ep, c, "--",rev_ep[c])
	return rev_ep

class Node(object):
    """
    Represents a node in the graph
    """

    def __init__(self, nodename, classname, methodname):
        self.nodename = nodename
        self.classname = classname
        self.methodname = methodname

        self.annotations = set()
        self.outgoing_links = {}

    def get_outgoing_links(self):
        return self.outgoing_links

    def add_annotation(self, annotation):
        self.annotations.add(annotation)

    def get_name(self):
        return self.nodename

    def get_classname(self):
        return self.classname

    def get_annotations(self):
        return self.annotations

    def get_outgoing_links(self):
        return self.outgoing_links

    def add_outgoing_link(self, node):
        self.outgoing_links[node.get_name()] = node

class ClassNode(object):
    """
    Represents a class node in the graph
    """

    def __init__(self, nodename):
        self.nodename = nodename

        self.annotations = set()
        self.outgoing_links = {}

    def get_outgoing_links(self):
        return self.outgoing_links

    def add_annotation(self, annotation):
        self.annotations.add(annotation)

    def get_name(self):
        return self.nodename

    def get_annotations(self):
        return self.annotations

    def get_outgoing_links(self):
        return self.outgoing_links

    def add_outgoing_link(self, node):
        self.outgoing_links[node.get_name()] = node


def parse_callgraph_line(line):
    """
    Parses a line from the call graph file
    """
    parts = line.split("->")
    if len(parts) != 2:
        print("Bad line:", line)
        return None, None

    return process_line_part(parts[0]), process_line_part(parts[1])

def process_line_part(linepart):
    """
    Processes individual node info from a line part
    """
    linepart = linepart.strip().replace("\"", "")

    parts = linepart.split()
    if len(parts) > 2:
        return None
    annotation = None
    if len(parts) == 2:
        annotation = parts[0].replace("]", "")
        annotation = annotation.replace("[", "")

    nodename = parts[-1]
    nodepath = nodename.split(".")
    methodname = nodepath[-1]
    classname = ".".join(nodepath[:-1])

    node =  Node(nodename, classname, methodname)
    if annotation is not None:
        node.add_annotation(annotation)

    return node
