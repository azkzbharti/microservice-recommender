"""*******************************************************************************
* Licensed Materials - Property of IBM
* (c) Copyright IBM Corporation ${year}. All Rights Reserved.
*
* Note to U.S. Government Users Restricted Rights:
* Use, duplication or disclosure restricted by GSA ADP Schedule
* Contract with IBM Corp.
*******************************************************************************"""

"""
Author: Raunak Sinha
Maintenance: Raunak Sinha
"""

"""
Code for reading nodes and edges from inter-class-usage-json and converting it to the new schema
"""
import json

with open('inter-class-usage.json') as json_file:
    data = json.load(json_file)

def node_used_to(node):
	"""
	Number of out-going edges 
	Input: Nodes from intern class
	Output: Count of out-going edges 
	"""
	count = 0
	for i in node["usedClassesToCount"].keys():
		count += int(node["usedClassesToCount"][i])
	return count

def node_used_by(node):
	"""
	Number of in-coming edges 
	Input: Nodes from intern class
	Output: Count of in-coming edges 
	"""
	count = 0
	for i in node["usedByClassesToCount"].keys():
		count += int(node["usedByClassesToCount"][i])
	return count

def make_node(current_node):
	"""
	Creating a new node following schema rules
	Input: Node data from inter-class-usage
	Output: Node data in schema format
	"""
	global count_node
	make_node = {}

	make_node["label"] = current_node["name"]
	make_node["id"] = str(count_node)
	count_node += 1
	make_node["description"]=""

	make_node["properties"] = {}
	make_node["properties"]["technical_type"] = ""
	make_node["properties"]["business_type"] = ""
	
	make_node["properties"]["used_by"] = node_used_by(current_node)
	make_node["properties"]["used_to"] = node_used_to(current_node)

	make_node["properties"]["num_ext_dependency"] = ""
	return make_node

def find_node_id(inp):
	"""
	Accessing node id from give node label
	Input: Label of node to find from inte-class-usage
	Output: Id of node given its label
	"""
	for i in schema["nodes"]:
		if i["label"] == inp:
			return i["id"]
	print (inp,"somthing is off")

def make_edge_func(source, sink, frequency):
	global count_edge
	
	make_edge = {}
	make_edge["label"] = ""
	make_edge["id"] = str(count_edge)
	count_edge += 1
	make_edge["description"] = ""
	make_edge["properties"] = {}
	make_edge["properties"]["start"] = source
	make_edge["properties"]["end"] = sink
	make_edge["frequency"] = str(frequency)
	make_edge["methods"] = [""]
	return make_edge

count_node = 0
count_edge = 0

# Converting to schema of given format
schema = {}
schema["clusters"] = []
schema["edges"] = []
schema["nodes"] = []


# Making Nodes
for i in data.keys():
	# print ("---------------")
	# print (i)
	# print (data[i])
	current_node = data[i]
	schema["nodes"].append(make_node(current_node))

# Making Edges

make_edge = {}
make_edge["type"] = "inter_class_connections"
make_edge["weight"] = ""
make_edge["description"] = ""
make_edge["relationship"] = []
schema["edges"].append(make_edge)

for i in data.keys():
	current_node = data[i]
	if current_node['type'] == 'sink' or current_node['type'] == 'both':
		for j in current_node['usedClassesToCount'].keys():
			# print (current_node['usedClassesToCount'][j])
			make_edge["relationship"].append(make_edge_func(find_node_id(current_node['name']), find_node_id(j), current_node['usedClassesToCount'][j]))

	if data[i]['type'] == 'source' or data[i]['type'] == 'both':
		for j in current_node['usedByClassesToCount'].keys():
			make_edge["relationship"].append(make_edge_func(find_node_id(j), find_node_id(current_node['name']), current_node['usedByClassesToCount'][j]))


with open('graph.json', 'w') as f:
	json.dump(schema, f)