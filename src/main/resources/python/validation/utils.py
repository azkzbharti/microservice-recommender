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

    parts = linepart.split("] ")
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

    #print("----", nodename, methodname, classname, annotation)

    node =  Node(nodename, classname, methodname)
    if annotation is not None:
        annotation = clean_annotation(annotation)
        node.add_annotation(annotation)

    return node

def clean_annotation(annotation):
    tx_index = annotation.find(", txid: ")
    if tx_index > -1:
        annotation = annotation[:tx_index]+"}"
        lastfewchars = annotation[-10:]
        paren_idx = lastfewchars.rfind("(")
        if paren_idx > -1:
            paren_idx = 10 - paren_idx
            #print ("------", annotation, lastfewchars, paren_idx)
            annotation = annotation[:-paren_idx] + "}"
            #print ("after", annotation)
    return annotation

def extract_unique_usage_relations(icu, type=None):
	"""
	Constructs separate feature matrices for classes using or used by a single class
	ICU convention is row-uses-col, so icu[r][c] = 1 if r is used by c
	The type can be 'incoming', 'outgoing', 'both' or None which is same as 'both'
	"""
	incoming_relations = np.zeros_like(icu)
	outgoing_relations = np.zeros_like(icu)

	num_classes = icu.shape[0]
	incoming = 0
	outgoing = 0

	for ridx in range(num_classes):
		r = icu[ridx,:]
		if np.sum(r>0) == 1:
			incoming += 1
			incoming_relations[ridx,:] = r

	#print("Single Incoming found:", incoming)
	if type == "incoming":
		return incoming_relations

	for cidx in range(num_classes):
		c = icu[:,cidx]
		if np.sum(c>0) == 1:
			outgoing+=1
			outgoing_relations[:,cidx] = c

	#print("Single outgoing found:", outgoing)
	if type == "outgoing":
		return outgoing_relations

	return incoming_relations + outgoing_relations

def get_maintained_connections_proportion(clusters, single_usage_relations, class2idx):

	member2clustermap = {}

	for clusterid in clusters.keys():
		cm = clusters[clusterid]
		members = cm['classes']
		all_members = list(members)
		for m in all_members:
			memberid = class2idx[m]
			member2clustermap[memberid] = clusterid

	num_classes = single_usage_relations.shape[0]
	total_relations = 0.
	found_relations = 0.

	# We only look at the upper diagonal
	for ridx in range(num_classes):
		row_cluster = member2clustermap.get(ridx)
		if row_cluster is None:
			continue
		for cidx in range(ridx, num_classes):
			if single_usage_relations[ridx][cidx] == 1:
				col_cluster = member2clustermap.get(cidx)
				if col_cluster is None:
					continue
				total_relations += 1.0
				if row_cluster == col_cluster:
					found_relations += 1.0

	return found_relations/total_relations, total_relations

def get_interface_count(app, clusters):

	num_clusters = float(len(clusters))
	global_interface_count = 0.

	for clusterid in clusters.keys():
		cluster_interface_count = 0.

		cm = clusters[clusterid]
		members = cm['classes']
		all_members = list(members)
		all_member_idxs = [app.icuclass2idx[m] for m in all_members]  # all classes in this cluster

		for member_idx in all_member_idxs:
			col = app.icu[:,member_idx]		# cofirm if it should be row or col, i think col is correct
			for idx,value in enumerate(col):
				if value > 0 and idx not in all_member_idxs:
					# if the class is used by another class and that class is not from this cluster
					# treat it as an interface and move on the next class
					cluster_interface_count +=1
					break
		global_interface_count += cluster_interface_count

	return global_interface_count / num_clusters

def get_chattiness(app, clusters):

	total_chattiness = 0

	for c_i in clusters.keys():

		members_i = list(clusters[c_i]['classes'])
		member_idxs_i = [app.icuclass2idx[m] for m in members_i]

		for c_j in clusters.keys():
			if c_i == c_j:
				continue
			members_j = list(clusters[c_j]['classes'])
			member_idxs_j = [app.icuclass2idx[m] for m in members_j]

			filtered_members_idxs_i = [idx for idx in member_idxs_i if idx not in member_idxs_j]
			filtered_members_idxs_j = [idx for idx in member_idxs_j if idx not in member_idxs_i]

			num_pairs = len(filtered_members_idxs_i) * len(filtered_members_idxs_j)
			chat_count = 0.0
			for i in filtered_members_idxs_i:
				for j in filtered_members_idxs_j:
					if app.icu[i,j] > 0 or app.icu[j,i] > 0:
						chat_count += 1.0
			chattiness = chat_count/num_pairs
			#print("---", c_i, chattiness, chat_count, num_pairs)
			total_chattiness += chattiness

	num_clusters = len(clusters.keys())
	denominator = num_clusters * (num_clusters - 1) # all pairs of clusters
	chattiness_score = total_chattiness / denominator

	return chattiness_score

def compute_purity_metrics(entrypoints, reverse_entrypoints, clusters):
	rev_ep_map = reverse_entrypoints
	# convert entrypoints to have classes instead of methods
	ep_map = {}
	for ep in entrypoints.keys():
		methods = entrypoints[ep]
		for m in methods:
			c = ".".join(m.split(".")[:-1])
			#print("c:", c)
			if ep_map.get(ep) is None:
				ep_map[ep] = []
			ep_map[ep].append(c)

	cluster_purity = compute_cluster_purity(clusters, rev_ep_map)
	entrypoint_purity = compute_entrypoint_purity(clusters, ep_map)

	return cluster_purity, entrypoint_purity

def compute_cluster_purity(clusters, rev_ep):
	purities = []
	missing = []
	num_clusters = float(len(clusters))

	for c in clusters.keys():
		members = list(clusters[c]['classes'])

		entrypoints_involved = []
		for classname in members:
			eps = rev_ep.get(classname)
			if eps is None:
				missing.append(classname)
				continue
			entrypoints_involved.extend(eps)

		uniq_entrypoints_in_cluster = len(set(entrypoints_involved))
		purities.append(uniq_entrypoints_in_cluster)

	print("Classes ignored due to no entrypoint mapping:", len(missing))
	return np.sum(purities) / num_clusters

def compute_entrypoint_purity(clusters, ep):
	class2cluster_map = {}
	missing = []

	for clusid in clusters.keys():
		members = list(clusters[clusid]['classes'])
		for classname in members:
			class2cluster_map[classname] = clusid

	num_eps = len(ep)
	print("Number of entrypoints detected:", num_eps)
	purities = []

	for epname in ep.keys():
		classes_for_ep = ep[epname]
		clusters_involved = []
		for c in classes_for_ep:
			if class2cluster_map.get(c) is None:
				missing.append(c)
				continue
			clusters_involved.append(class2cluster_map[c])

		uniq_clusters_for_ep = len(set(clusters_involved))
		purities.append(uniq_clusters_for_ep)

	print("Classes ignored due to no cluster mapping:", len(missing))
	return np.sum(purities) / num_eps
