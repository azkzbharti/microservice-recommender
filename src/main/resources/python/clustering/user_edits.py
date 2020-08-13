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
The use may not want any of the list like refactor to change but what happens is
that say refactor may not need to be changed because of user edits but is changed 
becasue of the uncertaininty in the seed-expansion algo
"""


""" Code for identifying user edits"""
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

def compare_groups(org_group, edit_group):
	new_edits = []

	if org_group and edit_group:
		difference = set(org_group["nodes"]).symmetric_difference(set(edit_group["nodes"]))
		if difference:
			diff1 = set(org_group["nodes"]) - set(edit_group["nodes"]) #In o_refactor but not in e_refactor - don't need to handle this case
			diff2 = set(edit_group["nodes"]) - set(org_group["nodes"])
			print ("this",diff2)
			new_edits = list(diff2)
		else:	
			pass
	elif edit_group: #This info has to be added that these classes were added in a json
		new_edits = edit_group
	return new_edits

if __name__ == "__main__":
	parser = argparse.ArgumentParser()
	parser.add_argument('--originalFile')
	parser.add_argument("--editFile")
	parser.add_argument('--seedFile')
	parser.add_argument('--editSeed')
	parser.add_argument('--editInfo')
	args = parser.parse_args()

	with open(args.seedFile,'r') as f:
		seeds = f.readlines()
		seeds = [x.strip() for x in seeds]
		seeds = [x.split(",") for x in seeds]
		print ("Seeds Loaded\n")

	all_seeds = []
	for i in seeds:
		all_seeds += i

	with open(args.originalFile) as json_file:
		original_data = json.load(json_file)

	with open(args.editFile) as json_file:
		edited_data = json.load(json_file)


	original_clusters = original_data["clusters"]
	edited_clusters = edited_data["clusters"]

	"""
	-- Comparison of clusters
	0. Match o_microservices to the appropriate seeds
	1. Find the ones with highest intersection between
		Just compare the ids maybe
	"""
	edit_information = {}

	# Unasigned Group
	o_list = e_list = []
	for i in original_clusters:
		if i['type'] == 'unassigned_group':
			o_list = i
	for i in edited_clusters:
		if i['type'] == 'unassigned_group':
			e_list = i
	unassign_edit_list = compare_groups(o_list,e_list)
	unassign_edit_list = [find_node(x, original_data) for x in unassign_edit_list]
	for j in unassign_edit_list:
		if j in all_seeds:
			all_seeds.remove(j)
	if unassign_edit_list:
		edit_information["unassigned_group"] = unassign_edit_list

	#Utility Group
	o_list = e_list = []
	for i in original_clusters:
		if i['type'] == 'utility_group':
			o_list = i
	for i in edited_clusters:
		if i['type'] == 'utility_group':
			e_list = i
	utility_edit_list = compare_groups(o_list,e_list)
	utility_edit_list = [find_node(x, original_data) for x in utility_edit_list]
	for j in utility_edit_list:
		if j in all_seeds:
			all_seeds.remove(j)
	if utility_edit_list:
		edit_information["utility_group"] = utility_edit_list

	#Refactor Group
	o_list = e_list = []
	for i in original_clusters:
		if i['type'] == 'refactor_candidates_group':
			o_list = i
	for i in edited_clusters:
		if i['type'] == 'refactor_candidates_group':
			e_list = i
	refactor_edit_list = compare_groups(o_list,e_list)
	refactor_edit_list = [find_node(x, original_data) for x in refactor_edit_list]
	for j in refactor_edit_list:
		if j in all_seeds:
			all_seeds.remove(j)
	if refactor_edit_list:
		edit_information["refactor_candidates_group"] = refactor_edit_list

	#Unreachable Group
	# o_list = e_list = []
	# for i in original_clusters:
	# 	if i['type'] == 'unreachable_group':
	# 		o_list = i
	# for i in edited_clusters:
	# 	if i['type'] == 'unreachable_group':
	# 		e_list = i
	# unreachable_edit_list = compare_groups(o_list,e_list)
	# unreachable_edit_list = [find_node(x, original_data) for x in unreachable_edit_list]
	# if unreachable_edit_list:
	# 	edit_information["unreachable_group"] = unreachable_edit_list

	"""
	Comparing microservices
	"""
	o_microservice = {}
	for i in original_data["clusters"]:
		if i['type'] == "microservices_group":
			i["node_name"] = [find_node(x,original_data) for x in i["nodes"]]
			o_microservice[i["id"]] = i

	e_microservice = {}
	for i in edited_data["clusters"]:
		if i['type'] == "microservices_group":
			i["node_name"] = [find_node(x,edited_data) for x in i["nodes"]]
			e_microservice[i["id"]] = i

	# for i,j in zip(seeds, o_microservice):
	# 	j["seeds"] = i
	

	o_ms_ids = list(o_microservice.keys())
	e_ms_ids = list(e_microservice.keys())
	
	deleted_ms = set(o_ms_ids) - set(e_ms_ids)
	added_ms = set(e_ms_ids) - set(o_ms_ids)
	common_ms = set(o_ms_ids).intersection(e_ms_ids)
	print ("Deleted: ",deleted_ms, "Added: ",added_ms,"Common: ", common_ms)

	# For common microservices
	seed_mapping = {}
	for i in sorted(list(common_ms)):
		o_cluster = o_microservice[i]
		e_cluster = e_microservice[i]
		
		additional_seeds = compare_groups(o_cluster,e_cluster)

		additional_seeds = [find_node(x, original_data) for x in additional_seeds]
		for j in additional_seeds:
			if j in all_seeds:
				all_seeds.remove(j)
		seed_mapping[i] = additional_seeds
	print (seed_mapping)

	# For additional of a microservice
	for i in sorted(list(added_ms)):
		e_cluster = e_microservice[i]
		additional_seeds = e_cluster["nodes"]
		additional_seeds = [find_node(x, original_data) for x in additional_seeds]
		for j in additional_seeds:
			if j in all_seeds:
				all_seeds.remove(j)
		seed_mapping[i] = additional_seeds
	print (seed_mapping)
	# print ("---------------------")
	# for i in seed_mapping:
	# 	print (len(seed_mapping[i]))

	# Adding appropriate original seeed information appropriately
	for i in all_seeds:
		for j in o_microservice.keys():
			if i in o_microservice[j]["node_name"]:
				if o_microservice[j]['id'] not in seed_mapping.keys():
					print ("Error")
				seed_mapping[o_microservice[j]['id']].insert(0,i)
				
	#Checking for empty seeds, possible case			
	del_index = []
	for i in seed_mapping:
		if not seed_mapping[i]:
			del_index.append(i)

	for i in del_index:
		del seed_mapping[i]

	#Checking for duplicates of seeds
	all_seeds_temp = []
	for i in seed_mapping:
		all_seeds_temp += seed_mapping[i]

	duplicate_temp = {}
	for i in all_seeds_temp:
		if i not in duplicate_temp:
			duplicate_temp[i] = 0
		duplicate_temp[i] += 1

	for i in duplicate_temp:
		if duplicate_temp[i] > 1:
			for j in seed_mapping:
				if len(seed_mapping[j]) > 1:
					if i in seed_mapping[j]:
						seed_mapping[j].remove(i)

	edit_information["seeds"] = seed_mapping
	print (edit_information)
	with open(args.editInfo, 'w') as f:
		json.dump(edit_information, f)

	with open(args.editSeed, 'w') as f:
		for i in seed_mapping:
			obj = ",".join(seed_mapping[i])
			f.write(obj)
			f.write('\n')
	# print ("---------------------")
	# for i in seed_mapping:
	# 	print (len(seed_mapping[i]))
	