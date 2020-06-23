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
Code for re-enforcing user edits
"""

import sys
import argparse
import json
from datetime import datetime

def find_node_id(name,data):
	"""
	Finding the id of graph nodes given the node name
	Input: ID of node
	Output: Label of node
	"""
	nodes = data["nodes"]
	for i in nodes:
		# print (i["label"],name)
		if i["label"] == name:
			return i["id"]

	print ("Something is worng", i["label"], name)

def find_node(id_val, data):
	"""
	Finding the label of graph nodes given the node id
	Input: ID of node
	Output: Label of node
	"""
	nodes = data["nodes"]
	for i in nodes:
		if i["id"] == id_val:
			return i["label"]
	print ("Something is wrong")

def create_new(inp, label, data):
	new_temp_dict = {}
	new_temp_dict["label"] = label

	new_temp_dict["id"] = str(int(time.time()*10**16))
	ids.append(new_temp_dict["id"])
	# counter += 1
	new_temp_dict["type"] = label
	new_temp_dict["description"] = ""
	new_temp_dict["properties"] = {}
	new_temp_dict["properties"]["affected_business_domains"] = [""]
	new_temp_dict["properties"]["db_dependence"] = {}
	new_temp_dict["properties"]["db_dependence"]["db"] = ""
	new_temp_dict["properties"]["db_dependence"]["tables"] = [""]

	new_temp_dict["metrics"] = {}
	new_temp_dict["metrics"]["cohesion_score"] = ""
	new_temp_dict["metrics"]["conceptual_independence"] = ""
	new_temp_dict["metrics"]["coupling_score"] = ""
	new_temp_dict["metrics"]["independence_score"] = ""
	new_temp_dict["metrics"]["data_independence_score"] = ""
	new_temp_dict["metrics"]["volume_inter_partition_calls"] = ""
	new_temp_dict["metrics"]["transaction_independence_score"] = ""
	new_temp_dict["metrics"]["functional_encapsulation"] = ""
	new_temp_dict["metrics"]["modularity"] = ""
	new_temp_dict["metrics"]["structural_cohesivity"] = ""

	new_temp_dict["nodes"] = []
	new_temp_dict["transactions"] = []
	for j in inp:
		# print (find_node_id(j,data))
		new_temp_dict["nodes"].append(find_node_id(j,data))
	return new_temp_dict, [find_node_id(x,data) for x in inp]

def microservice_resolve(a,b):
	pass

if __name__ == "__main__":
	parser = argparse.ArgumentParser()
	parser.add_argument('--graphFile')
	parser.add_argument("--editInfo")
	parser.add_argument("--outFile")
	
	args = parser.parse_args()

	with open(args.graphFile) as json_file:
		graph = json.load(json_file)
	clusters = graph["clusters"]
	with open(args.editInfo) as json_file:
		edit_data = json.load(json_file)
	

	for i in edit_data:
		if i in ['unreachable_group', 'unassigned_group']:
			print ("Problem")
			# exit()

		# For enforcing all non-microserive edits
		elif i in ["utility_group", "refactor_candidates_group"]:
			remove = []
			flag = 0
			for j in clusters:
				if j["type"] == i:
					flag = 1
					for k in edit_data[i]:
						temp = find_node_id(k,graph)
						if temp not in j["nodes"]:
							j["nodes"].append(temp)
							remove.append(temp)
					break
			if flag == 0:
				cluster_new, remove = create_new(edit_data[i],i, graph)
				graph["clusters"].append(cluster_new)

			for j in clusters:
				if j["type"] != i:
					for k in remove:
						if k in j["nodes"]:
							j["nodes"].remove(k)

		# For edits to microservice
		elif i == 'seeds':
			#print (edit_data[i]), seeds
			microservice_ids = []
			microservice = {}

			mapping = []
			for j in clusters:
				if j["type"] == "microservices_group":
					microservice_ids.append(j["id"]) #TODO id to seed mapping
					microservice[j["id"]] = j

			if len(edit_data[i]) == len(microservice_ids): #Easy assignment TODO
				for j,k in zip(microservice_ids,edit_data[i]):
					mapping.append((j,k))#new_microservice <-> original_seed_list

					# microservice_resolve(microservice[j],edit_data[i][k])
					for l in edit_data[i][k]:
						if find_node_id(l,graph) not in microservice[j]['nodes']:
							microservice[j]['nodes'].append(find_node_id(l,graph))

				for k in mapping:
					for j in clusters:
						if j["id"] != k[0]:
							for l in edit_data[i][k[1]]:
								if find_node_id(l,graph) in j['nodes']:
									j['nodes'].remove(find_node_id(l,graph))
			else:
				"""
				TODO
				Stored which cluster was deleted --> now to map to seed and then do honor user edits for the non-deleted and non-deleted
				cluster.  (present id - seed mapping)
				For non-deleted 1-x-1 mapping
				For deleted is in unassigned then pull into cluster logic
				"""
				microservice_ids = []
				microservice = {}

				mapping = []
				for j in clusters:
					if j["type"] == "microservices_group":
						microservice_ids.append(j["id"]) #TODO id to seed mapping
						microservice[j["id"]] = j

				seeds_ids = []
				for j in edit_data[i]:
					seeds_ids.append(j)

				for j in edit_data['delete']:
					seeds_ids.pop(j)

				if len(microservice_ids) != len(seeds_ids):
					print ("Problem 1")

				for j,k in zip(microservice_ids,seeds_ids):
					mapping.append((j,k))#new_microservice <-> original_seed_list

					for l in edit_data[i][k]:
						if find_node_id(l,graph) not in microservice[j]['nodes']:
							microservice[j]['nodes'].append(find_node_id(l,graph))

				for k in mapping:
					for j in clusters:
						if j["id"] != k[0]:
							for l in edit_data[i][k[1]]:
								if find_node_id(l,graph) in j['nodes']:
									j['nodes'].remove(find_node_id(l,graph))

	with open(args.graphFile) as json_file:
		graph1 = json.load(json_file)

	with open(args.outFile, 'w') as f:
		json.dump(graph1, f)

