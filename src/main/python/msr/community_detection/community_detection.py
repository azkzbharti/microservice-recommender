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
Code for finding communities given inter-class usage JSON
"""

import networkx as nx
import networkx as nx
import json
import matplotlib.pyplot as plt
from networkx.algorithms import community
import pickle

g = nx.DiGraph() #NetworkX directed graph objects

""" Reading the graph schema json """
with open('graph.json') as json_file:
    data = json.load(json_file)

for i in data["edges"]:
	if i["type"] == "inter_class_connections":
		data_read = i["relationship"]
		break

def find_node(id_val):
	"""
	Finding the node-name for visualisation given the node-id from the json schema. 
	This function checks nodes infromation present in nodes data-type in schema and return the appropriate name.
	Input: ID of node
	Output: Label of node
	"""
	nodes = data["nodes"]
	for i in nodes:
		if i["id"] == id_val:
			return i["label"]
	print ("Something is wrong")

def find_node_id(name):
	"""
	Finding the node-id for forming communites in the schema JSON given the node-name.
	This function checks nodes infromation present in nodes data-type in schema and return the appropriate ids.
	Input: ID of node
	Output: Label of node
	"""
	nodes = data["nodes"]
	for i in nodes:
		if i["label"] == name:
			return i["id"]
	print ("Something is worng")


""" Creating the graph given the nodes and edges """
nodes = data["nodes"]
for i in nodes:
	g.add_node(i["label"])

for i in data_read:
	g.add_edge(find_node(i["properties"]["start"]), find_node(i["properties"]["end"]), frequency = int(i["frequency"]))

""" For saving the graph object """
# with open('graph.pkl', 'wb') as f:
#     pickle.dump(g, f)

""" Directed Community detection methods """
# communities_generator = community.girvan_newman(g)
communities_generator = community.asyn_lpa_communities(g)


""" Undirected community detection methods """
# communities_generator = community.kernighan_lin_bisection(g)
# communities_generator = community.k_clique_communities(g,2)
# communities_generator = community.label_propagation_communities(g)

# communities_generator = community.greedy_modularity_communities(g) #Remains 
# communities_generator = community.asyn_fluidc(g,2) #Remains

""" For Girvan-Newman algo """
# top_level_communities = next(communities_generator)
# next_level_communities = next(communities_generator)

""" For remaining algos """
next_level_communities = communities_generator
next_level_communities = sorted(map(sorted, next_level_communities))

""" Writing clusters detected to the schema """
counter = 0
for i in next_level_communities:
	new_temp_dict = {}
	new_temp_dict["label"] = "cluster"+str(counter)
	new_temp_dict["id"] = str(counter)
	counter += 1

	new_temp_dict["description"] = ""
	new_temp_dict["properties"] = {}
	new_temp_dict["properties"]["affected_business_domains"] = [""]
	new_temp_dict["properties"]["db_dependence"] = {}
	new_temp_dict["properties"]["db_dependence"]["db"] = ""
	new_temp_dict["properties"]["db_dependence"]["tables"] = [""]

	new_temp_dict["metrics"] = {}
	new_temp_dict["metrics"]["cohesion_score"] = ""
	new_temp_dict["metrics"]["semantic_relatedness_score"] = ""
	new_temp_dict["metrics"]["coupling_score"] = ""
	new_temp_dict["metrics"]["independence_score"] = ""
	new_temp_dict["metrics"]["data_independence_score"] = ""
	new_temp_dict["metrics"]["business_entity_affect"] = ""

	new_temp_dict["nodes"] = []
	for j in i:
		print (find_node_id(j))
		new_temp_dict["nodes"].append(find_node_id(j))
	data["clusters"].append(new_temp_dict)

with open('graph.json', 'w') as f:
	json.dump(data, f)

""" Evaluation """
print ("performance",nx.algorithms.community.quality.performance(g, next_level_communities))
print ("coverage",nx.algorithms.community.quality.coverage(g, next_level_communities))