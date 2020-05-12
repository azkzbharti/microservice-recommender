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
Code for finding communities given inter-class usage JSON and seeds
"""

"""
This has closure, utility and island+dead_code
"""

import networkx as nx
import sys
import argparse
from collections import defaultdict
import time
import codecs
from multiprocessing import Pool
import pprgrow_min_cond
import json
import os
import pickle
import re 

INF = float('inf')

def pprgrow(args):
	seed,G,stopping,nruns,alpha,maxexpand,fast = args
	expandseq = [2,3,4,5,10,15]
	expands = list()
	curmod = 1
	while len(expands) < nruns:
		temp = [curmod*i for i in expandseq]
		for i in temp:
			expands.append(i)
		curmod *= 10

	expands = expands[:nruns]
	maxdeg = max(dict(G.degree(G.nodes())).values())
	bestcond = INF
	bestset = list()
	bestexpand = 0.0
	bestceil = 0.0
	if fast==True:
		expands = [1000]
	print (maxdeg)
	for ei in range(len(expands)):
		if fast==True:
			curexpand = expands[ei]
		else:
			curexpand = expands[ei]*len(seed)+maxdeg
		assert len(seed)>0.0
		if curexpand > maxexpand:
			continue
		if stopping=='cond':
			start_time = time.time()
			curset, cond = pprgrow_min_cond.pprgrow(G,seed,alpha,curexpand)
			end_time = time.time()
			print (end_time - start_time)
			if cond < bestcond:
				bestcond = cond
				bestset = curset
				bestexpand = curexpand

	return curset


def growclusters(G,seeds,expansion,stopping,nworkers,nruns,alpha,maxexpand,fast):
	if maxexpand == INF:
		maxexpand = G.number_of_edges()

	n = G.number_of_nodes()
	ns = len(seeds)
	communities = list()

	if nworkers==1:
		for i in range(ns):
			seed = seeds[i]
			if expansion=='ppr':
				curset = pprgrow((seed,G,stopping,nruns,alpha,maxexpand,fast))
			else:
				print ('Method not implemented yet')

			#H[curset,i] = 1.0
			communities.append(curset)
			print ('Seed',i,'Done')

	else:
		print ('Initiating parallel seed expansion')
		slen = len(seeds)
		args = zip(seeds,[G]*slen,[stopping]*slen,[nruns]*slen,[alpha]*slen,\
			[maxexpand]*slen,[fast]*slen)
		p = Pool(nworkers)
		if expansion=='ppr':
			communities = p.map(pprgrow, args)

	return communities


# TO DO: Write function to remove duplicate communities
def remove_duplicates(G, communities,delta):
	# Create node2com dictionary
	node2com = defaultdict(list)
	com_id = 0
	for comm in communities:
		for node in comm:
			node2com[node].append(com_id)
		com_id += 1

	deleted = dict()
	i = 0
	for i in range(len(communities)):
		comm = communities[i]
		if deleted.get(i,0) == 0:
			nbrnodes = nx.node_boundary(G, comm)
			for nbr in nbrnodes:
				nbrcomids = node2com[nbr]
				for nbrcomid in nbrcomids:
					if i!=nbrcomid and deleted.get(i,0)==0 and deleted.get(nbrcomid,0)==0:
						nbrcom = communities[nbrcomid]
						distance = 1.0 - (len(set(comm) & set(nbrcom))*1.0 / (min(len(comm),len(nbrcom))*1.0))

						if distance <= delta:
							# Near duplicate communities found.
							# Discard current community
							# Followed the idea of Lee et al. in GCE
							deleted[i] = 1
							for node in comm:
								node2com[node].remove(i)
	for i in range(len(communities)):
		if deleted.get(i,0)==1:
			communities[i] = []

	communities = filter(lambda c: c!=[], communities) # Discard empty communities
	return communities

def neighbor_inflation(G,seeds):
	# Seed = union(seedNode, egonet(seedNode))
	for i in range(len(seeds)):
		seed = seeds[i]
		egonet = list()
		for s in seed:
			egonet.append(s)
			[egonet.append(k) for k in G.neighbors(s)]
		seeds[i] = list(set(egonet))
		# print sorted([int(k) for k in seeds[i]])

	return seeds

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

def find_node(id_val):
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


def find_median(sorted_list):
	"""
	For quartile detection
	"""
	indices = []
	list_size = len(sorted_list)
	median = 0

	if list_size % 2 == 0:
		indices.append(int(list_size / 2) - 1)  # -1 because index starts from 0
		indices.append(int(list_size / 2))
		median = (sorted_list[indices[0]] + sorted_list[indices[1]]) / 2
	else:
		indices.append(int(list_size / 2))
		median = sorted_list[indices[0]]

	return median, indices
	pass	

if __name__ == "__main__":


	

	parser = argparse.ArgumentParser()
	parser.add_argument('--inPutFilePath')
	parser.add_argument("--outPutFilePath")
	parser.add_argument('--seed_file')
	parser.add_argument('--tempFilePath')
	parser.add_argument('--visFilePath')
	parser.add_argument('--filterFilePath')
	parser.add_argument('--serviceEntry')
	

	# parser.add_argument('graph_file',type=str,help='Input Graph File Path')
	# parser.add_argument('seed_file',type=str,help='Input Seeds File Path')
	parser.add_argument('--ninf',help='Neighbourhood Inflation parameter',action='store_false')
	parser.add_argument('--expansion',type=str,help='Seed expansion: PPR or VPPR',default='ppr')
	parser.add_argument('--stopping',type=str,help='Stopping criteria',default='cond')
	parser.add_argument('--nworkers',type=int,help='Number of Workers',default=1)
	parser.add_argument('--nruns',type=int,help='Maximum number of runs',default=13)
	parser.add_argument('--alpha',type=float,help='alpha value for Personalized PageRank expansion',default=0.99)
	parser.add_argument('--maxexpand',type=float,help='Maximum expansion allowed for approximate PPR',default=INF)
	parser.add_argument('--delta',type=float,help='Minimum distance parameter for near duplicate communities',default=0.2)
	args = parser.parse_args()

	print ("This",args.inPutFilePath)
	print (args.outPutFilePath)
	print (args.seed_file)

	# with open(args.outPutFilePath, 'r') as json_file:
	# 	data = json.load(json_file)

	
		
	# try: 
		# with open(args.inPutFilePath) as json_file:
		# 	data = json.load(json_file)
	
	with open(args.filterFilePath) as f:
		filter_read = f.readlines()
		regexp = re.compile(filter_read[0].strip())

	args = parser.parse_args()

	with open(args.outPutFilePath) as json_file:
		data = json.load(json_file)

	G = nx.read_edgelist(args.inPutFilePath)

	print ("Graph Loaded")
	if not os.path.exists(args.seed_file):
		default_seeds = ["com.ibm.websphere.samples.daytrader.beans.MarketSummaryDataBean","com.ibm.websphere.samples.daytrader.entities.OrderDataBean","com.ibm.websphere.samples.daytrader.entities.AccountProfileDataBean","com.ibm.websphere.samples.daytrader.beans.RunStatsDataBean"]
		with open(args.seed_file, 'w') as f:
		    for item in default_seeds:
		        f.write("%s\n" % item)

	with open(args.seed_file,'r') as f:
		seeds = f.readlines()
		seeds = [x.strip() for x in seeds]
		seeds = [[x.split(",")[0]] for x in seeds]
		print ("Seeds Loaded\n")
	print (seeds)
	# exit()
	# print seeds
	if args.ninf==True:
		seeds = neighbor_inflation(G,seeds)
	print ("Initiating Seed Expansion------")
	communities = growclusters(G,seeds,args.expansion,args.stopping,args.nworkers,args.nruns,args.alpha,args.maxexpand,False)

	print ("Seed Expansion Finished.\n")
	print ("Initiating removal of near duplicate communities.")

	communities = remove_duplicates(G,communities,args.delta)
	print ("Duplicate communities removed\n")

	communities = list(communities)
	print ("Writing communities to output file:")
	save_communities = []

	count = 0
	for c in communities:
		print (len(c))
		count += 1
		# save_communities.append([x.encode('UTF8') for x in c])
		save_communities.append([x for x in c])
	print (save_communities)
	# print (type(save_communities[0][0]))
	# exit()
	check_arr = []
	for i in communities:
		for j in i:
			check_arr.append(j)
	# print (len(check_arr))
	# exit()
	final_community = []

	for i in G.nodes():
		if i not in check_arr:
			final_community.append(i)

	count_check = 0
	for i in save_communities:
		count_check += len(i)
	count_check += len(final_community)

	print ("this is ",count)
	print ("Here",len(G.nodes()))

	"""
	[Processing 1] Closure Detection
	- Explore the edges from the unassigned group to the recommended microservices
	- If there are networks (paths) that are linked to only one microservice and no other classes (unassigned or other classes) then add
	  that to respective microservice through closure detection
	"""
	for i in data["edges"]:
		if i["type"] == "inter_class_connections":
			edges = i["relationship"]

	edge_list = []
	for i in edges:
		edge_list.append((i["properties"]["start"],i["properties"]["end"]))

	discarded_cluster = [find_node_id(x,data) for x in final_community]

	actual_clusters = []
	for i in save_communities:
		temp = []
		for j in i:
			temp.append(find_node_id(j,data))
		actual_clusters.append(temp)

	actual_clusters_complete = []
	for i in save_communities:
		for j in i:
			actual_clusters_complete.append(find_node_id(j,data))

	print (len(discarded_cluster))
	flag = 1
	count = 0
	while flag:
		flag = 0
		count += 1
		print (count)
		update = set()
		new_count = 0
		for i,n2_c in enumerate(actual_clusters):
			new_count += len(n2_c)
			seen = []
			for n1 in discarded_cluster:
				for n2 in n2_c:
					flag_check = 0
					if tuple((n1,n2)) in edge_list or tuple((n2, n1)) in edge_list:
						flag_check = 1
						for edge_temp in edge_list:
							if edge_temp[0] == n1:
								for i_s, n3_c in enumerate(actual_clusters):
									if edge_temp[1] in n3_c and i != i_s:
										flag_check = 0
							elif edge_temp[1] == n1:
								for i_s, n3_c in enumerate(actual_clusters):
									if edge_temp[0] in n3_c and i != i_s:
										flag_check = 0
					if flag_check == 1:
						update.add((n1,i))
						break

		print ("new_count", new_count+len(discarded_cluster))
		print ("discarder len curr",len(discarded_cluster))
		print ("new total cluster len",new_count)
		print ("Number of updates", len(update))
		for n1,i in list(update):
			if n1 not in actual_clusters[i]:
				actual_clusters[i].append(n1)
				flag = 1
			if n1 in discarded_cluster:
				discarded_cluster.remove(n1)
				flag = 1

	print ("Discarded", len(discarded_cluster))

	final_community = [find_node(x) for x in discarded_cluster]
	save_communities = []
	for i in actual_clusters:
		temp = []
		for j in i:
			temp.append(find_node(j))
		save_communities.append(temp)

	"""
	Closure detection gets over

	[Processing 2] Island Detection
	"""
	print ("Island Detection ************************")

	for i in data["edges"]:
		if i["type"] == "inter_class_connections":
			edges = i["relationship"]
	edge_list = []
	for i in edges:
		edge_list.append((i["properties"]["start"],i["properties"]["end"]))

	discarded_cluster = [find_node_id(x,data) for x in final_community]

	actual_clusters = []
	for i in save_communities:
		for j in i:
			actual_clusters.append(find_node_id(j,data))

	connected_nodes = set()
	for n1 in discarded_cluster:
		for n2 in actual_clusters:
			if tuple((n1,n2)) in edge_list or tuple((n2, n1)) in edge_list:
				connected_nodes.add(n1)
	for i in list(connected_nodes):
		discarded_cluster.remove(i)


	flag = 1
	pass_count = 0
	while flag:
		flag = 0
		pass_count += 1
		check_list = list(connected_nodes)
		for n1 in discarded_cluster:
			for n2 in check_list:
				if tuple((n1,n2)) in edge_list or tuple((n2, n1)) in edge_list:
					connected_nodes.add(n1)
					flag = 1
		for i in list(connected_nodes):
			if i in discarded_cluster:
				discarded_cluster.remove(i)
				flag = 1

	discarded_classes = [find_node(x).strip().split(".")[-1] for x in discarded_cluster]

	group = []
	while (discarded_cluster):
		island = set()
		queue = []
		queue.append(discarded_cluster[0])
		while (queue):
			n1 = queue.pop(0)
			island.add(n1)
			for n2 in discarded_cluster:
				if tuple((n1,n2)) in edge_list or tuple((n2, n1)) in edge_list:
					island.add(n2)
					queue.append(n2)
			if n1 in discarded_cluster:
				discarded_cluster.remove(n1)
		group.append([find_node(x).strip() for x in list(island)])

	with open(args.serviceEntry) as json_file:
		service_data = json.load(json_file)
	entry_points = []
	for i in service_data:
		for j in i["class_method_name"]:
			entry_points.append(".".join(j.split(".")[:-1]))
			# print (".".join(j.split(".")[:-1]))
	# exit()

	print ("Dead code analysis")
	dead_code = []
	for i in group:
		intersection = set.intersection(set(i), set(entry_points))
		if not intersection:
			dead_code.append(i)
			for j in i:
				if j in entry_points:
					print ("Yikes")
			# print (i)
	for i in dead_code:
		if i in group:
			group.remove(i)

	filter_for_util_detection = []
	for i in dead_code:
		filter_for_util_detection += i

	for i in group:
		filter_for_util_detection += i

	"""
	Insland detection gets over

	[Processing 3] Utitlity Detection
	"""
	print ("Utility Detection ************************")
	node_list = data["nodes"]

	degree = {}
	for i in node_list:
		if i["entity_type"] == 'class':
			degree[i["id"]] = {"in":[],"out":[]}

	edge_list = []
	for i in edges:
		if (i["properties"]["start"],i["properties"]["end"], i["frequency"]) in edge_list:
			print (i)
			exit()
		edge_list.append((i["properties"]["start"],i["properties"]["end"], i["frequency"]))

	# edge_list = set()
	# for i in edges:
	# 	edge_list.add((i["properties"]["start"],i["properties"]["end"], i["frequency"]))
	# edge_list = sorted(list(edge_list))

	# Step - 1
	for i in edge_list:
		if i[0] and i[1] in degree.keys():
			degree[i[0]]["out"].append(int(i[2])) # += degree[i[0]]["out"] + int(i[2])
			degree[i[1]]["in"].append(int(i[2])) #+= degree[i[1]]["in"] + int(i[2])
		else:
			print ("Check this case", i)
			exit()

	for i in degree.keys():
		degree[i]["out_sum"] = len(degree[i]["out"])
		degree[i]["in_sum"] = len(degree[i]["in"])

	sinks = [] #This for only identifying sinks
	sinks_id = []
	for i in degree.keys():
		if degree[i]["out_sum"] == 0:
			sinks.append((find_node(i),{"in-degree":degree[i]["in_sum"]}, {"out-degree":degree[i]["out_sum"]}))
			sinks_id.append(i)
			degree[i]["out_sum"] = 1

	ratio = []
	for i in degree.keys():
		if not degree[i]["out_sum"] == 0:
			ratio.append(degree[i]["in_sum"]/degree[i]["out_sum"])
	average = sum(ratio)/len(ratio)

	util_candidate = []
	util_candidate_id = []
	for i in degree.keys():
		if not degree[i]["out_sum"] == 0:
			if degree[i]["in_sum"]/degree[i]["out_sum"] > average:
				if find_node(i) not in filter_for_util_detection:
					util_candidate.append((i,find_node(i),{"in-degree":degree[i]["in_sum"]}, {"out-degree":degree[i]["out_sum"]},degree[i]["in_sum"]/degree[i]["out_sum"]))
					util_candidate_id.append(i)
		if degree[i]["out_sum"] == 0:
			print ("There is a problem")
			print (degree[i])
			exit()
	# print ("-------- Average ratio", average)


	in_degree_avg = []
	for i in util_candidate:
		in_degree_avg.append(i[2]["in-degree"])

	avg_indegree = sum(in_degree_avg)/len(in_degree_avg)
	# print (len(util_candidate))
	# print (len(in_degree_avg))

	variance_list = []
	for i in util_candidate:
		if i[2]["in-degree"]-avg_indegree > 0:
			variance_list.append(i[2]["in-degree"]-avg_indegree)
		else:
			variance_list.append(-1*(i[2]["in-degree"]-avg_indegree))
	avg_variance = sum(variance_list)/len(variance_list)

	in_degree_avg = sorted(in_degree_avg)
	median, median_indices = find_median(in_degree_avg)
	Q3, Q3_indices = find_median(in_degree_avg[median_indices[-1] + 1:])

	# Step - 2
	remove_list = []
	for i_iter,i in enumerate(util_candidate):
		if i[3]['out-degree'] > 2:
			remove_list.append(i)
	for i in remove_list:
		util_candidate.remove(i)

	#Step - 3

	"""#Variance based
	remove_list = []
	for i in util_candidate:
		if i[2]["in-degree"]-avg_indegree <= avg_variance:
			remove_list.append(i)
	for i in remove_list:
		util_candidate.remove(i)"""

	#Quartile based
	remove_list = []
	for i in util_candidate:
		if i[2]["in-degree"] < Q3:
			remove_list.append(i)
	for i in remove_list:
		util_candidate.remove(i)

	print ("----------- After Step 3 -----------")
	for i in util_candidate:
		print (i)
		print ()

	# Step - 4
	# This is for finding out the further connections
	util_candidate_new = []	
	for i in util_candidate:
		j = i[1]
		if not ("entity" in j.lower() or "entities" in j.lower() or "model" in j.lower() or "jpa" in j.lower()):
			util_candidate_new.append(i[0])

	# for i in degree.keys():
	# 	if i == '95':
	# 		print (degree[i], find_node(i))
	# 		exit()

	links_check = {}
	for i in node_list:
		if i["entity_type"] == 'class':
			links_check[i["id"]] = {"in":[],"out":[]}

	for i in edge_list:
		if i[0] and i[1] in links_check.keys():
			links_check[i[0]]["out"].append(i[1]) # += degree[i[0]]["out"] + int(i[2])
			links_check[i[1]]["in"].append(i[0]) #+= degree[i[1]]["in"] + int(i[2])
		else:
			print ("Check this case", i)
			exit()

	util_through_links = []
	for i in links_check.keys():
		# print (set(links_check[i]["out"]).issubset(util_candidate_new),links_check[i]["out"],util_candidate_new)
		if len(links_check[i]["in"]) == 0 and set(links_check[i]["out"]).issubset(util_candidate_new) and links_check[i]["out"] :
			util_through_links.append((i,find_node(i),{"in-degree":len(links_check[i]["in"])}, {"out-degree":len(links_check[i]["out"])}))


	
	"""
	Utility Detection over

	Adding to properties
	Creating utility cluster
	"""
	count_properties = 0
	coverage_seen = []
	utility_cluster = set()
	node_list = data["nodes"]
	for i in dead_code:
		for j in i:
			for k in node_list:
				if find_node_id(j,data) == k["id"]:
					k['properties']['dead_code'] = "1"
					coverage_seen.append(k["id"])
					count_properties += 1
	for i in group:
		for j in i:
			for k in node_list:
				if find_node_id(j,data) == k["id"]:
					if not ("entity" in j.lower() or "entities" in j.lower() or "model" in j.lower() or "jpa" in j.lower()):
						k['properties']['utility_type'] = "utility"
						count_properties += 1
						coverage_seen.append(k["id"])
						utility_cluster.add(j)
					else:
						print (k)

	for i in util_candidate:
		for k in node_list:
			if i[0] == k["id"]:
				if not ("entity" in i[1].lower() or "entities" in i[1].lower() or "model" in i[1].lower() or "jpa" in i[1].lower()):
					k['properties']['utility_type'] = "shared_util"
					coverage_seen.append(i[0])
					count_properties += 1
					utility_cluster.add(i[1])
				else:
					print (k)

	for i in util_through_links:
		for k in node_list:
			if i[0] == k["id"]:
				if not ("entity" in i[1].lower() or "entities" in i[1].lower() or "model" in i[1].lower() or "jpa" in i[1].lower()):
					k['properties']['utility_type'] = "shared_view"
					count_properties += 1
					coverage_seen.append(i[0])
					utility_cluster.add(i[1])
				else:
					print (k)

	utility_cluster = list(utility_cluster)

	for i in utility_cluster: #Updating unassigned group to remove utilities from it
		if i in final_community:
			final_community.remove(i)

	for j in dead_code: #Updating unassigned group to remove utilities from it
		for i in j:
			if i in final_community:
				final_community.remove(i)

	for i in utility_cluster: #Updating microservice clusters to remove utilities from it
		for j in save_communities: 
			if i in j:
				j.remove(i)

	for i in final_community:
		if "util" in i.lower():
			utility_cluster.append(i)
			# print (i)

	utility_cluster = list(set(utility_cluster))

	for i in utility_cluster: #Updating unassigned group to remove utilities from it
		if i in final_community:
			final_community.remove(i)

	"""
	[Processing 4] Refactoring code detection
	"""
	# refactor = set()
	# for j in save_communities: 
	# 	for k in save_communities:
	# 		if j != k:
	# 			intersection = set.intersection(set(j), set(k)) 
	# 			if intersection:
	# 				# print (intersection)
	# 				# print (save_communities.index(j) == save_communities.index(k))
	# 				# print ("****************")
	# 				for i in intersection:
	# 					refactor.add(i)
	# print ("88888888888")
	# print (len(refactor))
	
	refactor_first_filter = {}
	for j_eval,j in enumerate(save_communities): 
		for k_eval,k in enumerate(save_communities):
			if j != k:
				intersection = set.intersection(set(j), set(k)) 
				if intersection:
					# print (intersection)
					# print (save_communities.index(j) == save_communities.index(k))
					# print ("****************")
					for i in intersection:
						if i not in refactor_first_filter.keys():
							refactor_first_filter[i] = set()
						refactor_first_filter[i].add(j_eval)
						refactor_first_filter[i].add(k_eval)

	refactor = set()
	refactor_second_filter = set()

	for i in refactor_first_filter.keys():
		if len(refactor_first_filter[i]) > len(save_communities)/2:
			refactor.add(i)
		else:
			refactor_second_filter.add(i)
	print ("This")
	print (refactor)

	print ("step-2")
	print (refactor_second_filter)
	
	refactor_candidate = []
	for i in refactor:
		refactor_candidate.append((i, refactor_first_filter[i]))

	for i_iter in refactor_candidate:
		i = i_iter[0]
		for j in save_communities:
			if i in j:
				j.remove(i)

	degree_refactor = {}
	# refactor_second_filter = [find_node_id(x) for x in refactor_second_filter]
	for i in refactor_second_filter:
		degree_refactor[find_node_id(i,data)] = {}

	for i in refactor_first_filter.keys():
		if i in refactor_second_filter:
			degree_refactor[find_node_id(i,data)]['clusters'] = {}
			for j in refactor_first_filter[i]:
				degree_refactor[find_node_id(i,data)]['clusters'][j] = {'in':[], 'out':[]}

	# print (refactor_second_filter)
	for i_id in degree_refactor.keys():
		i = find_node(i_id)
		for j in degree_refactor[i_id]['clusters'].keys():
			curr_cluster = save_communities[j]

			for j_iter in curr_cluster:
				j_id = find_node_id(j_iter,data)
				for k in edge_list:
					if k[0] == i_id and k[1] == j_id:
						degree_refactor[i_id]['clusters'][j]['out'].append(int(k[2]))
					elif k[1] == i_id and k[0] == j_id:
					 	degree_refactor[i_id]['clusters'][j]['in'].append(int(k[2]))

	for i in degree_refactor.keys():
		max_sum = -1
		for j in degree_refactor[i]['clusters'].keys():
			sum_val = sum(degree_refactor[i]['clusters'][j]['in']) + sum(degree_refactor[i]['clusters'][j]['out'])
			degree_refactor[i]['clusters'][j]['sum'] = sum_val
		
	cluster_decision = []
	cluster_decision_property = []
	refactor = set()
	for i in degree_refactor.keys():
		sums = []
		for j in degree_refactor[i]['clusters'].keys():
			sums.append(degree_refactor[i]['clusters'][j]['sum'])
		max_val = max(sums)
		# print (i,degree_refactor[i],max_val,sums)
		if sums.count(max_val) > 1:
			refactor.add(i)
		else:#No duplicate
			for j in degree_refactor[i]['clusters'].keys():
				if degree_refactor[i]['clusters'][j]['sum'] == max_val: #remove from other groups
					cluster_decision.append((i,j))
					cluster_decision_property.append((find_node(i),refactor_first_filter[find_node(i)]))
	
	for i in refactor:
		refactor_candidate.append((find_node(i), refactor_first_filter[find_node(i)]))
	print (refactor_candidate)
	# exit()
	# print (refactor_candidate)


	for i_iter in cluster_decision:
		i = i_iter[0]
		i2 = i_iter[1]
		for j2,j in enumerate(save_communities):
			if j2 != i2 and find_node(i) in j:
				j.remove(find_node(i))
	

	for i_iter in refactor_candidate:
		i = i_iter[0]
		for j in save_communities:
			if i in j:
				j.remove(i)
	
	# print (refactor_candidate)
	# print ("cluster_decision")
	# for i in cluster_decision:
	# 	print ("++++++++++")
	# 	print (find_node(i[0]))
	# 	print (save_communities[i[1]])
	# print ("=========")

	# Checking for refactoring classes in unassigned

	refactor_unassigned = {}
	for i in final_community:
		for j in edge_list:
			if j[0] == find_node_id(i,data):
				check = find_node(j[1])
				for k_eval,k in enumerate(save_communities):
					if check in k:
						if i not in refactor_unassigned.keys():
							refactor_unassigned[i] = set()
						refactor_unassigned[i].add(k_eval)

	refactor = set()
	for i in refactor_unassigned.keys():
		if len(refactor_unassigned[i]) > len(save_communities)/2:
			refactor.add(i)

	for i in refactor:
		refactor_candidate.append((i,-1))
		if i in final_community:
			final_community.remove(i)
	
	
	count_now = len(final_community)
	count_now += len(utility_cluster)
	count_now += len(refactor_candidate)
	for i in save_communities:
		count_now += len(i)
	for i in dead_code:
		count_now += len(i)

	for j_data in refactor_candidate:
		j = j_data[0]
		if j_data[1] != -1:
			cluster_info = ["cluster."+str(x) for x in j_data[1]]
		else:
			cluster_info = ['unassigned_group']

		for k in node_list:
			if find_node_id(j,data) == k["id"]:
				k['properties']['original_cluster_assignment'] = cluster_info

	for j_data in cluster_decision_property:
		j = j_data[0]
		cluster_info = ["cluster."+str(x) for x in j_data[1]]
		for k in node_list:
			if find_node_id(j,data) == k["id"]:
				k['properties']['original_cluster_assignment'] = cluster_info

	print ("Properties found for",count_properties)
	coverage_seen_old = []
	for i in save_communities:
		for j in i:
			coverage_seen_old.append(find_node_id(j,data))

	# for i in final_community:
	# 	print (i)
	# exit()
	for i in save_communities:
		for j in i:
			coverage_seen.append(find_node_id(j,data))

	print ("New Coverage Metric before",len(list(set(coverage_seen_old)))/ len(G.nodes()))		
	print ("New Coverage Metric",len(list(set(coverage_seen)))/ len(G.nodes()))

	"""
	Adding community data
	"""
	data["clusters"] = []
	counter = 0
	for i in save_communities:
		new_temp_dict = {}
		new_temp_dict["label"] = "cluster"+str(counter)
		new_temp_dict["id"] = str(counter)
		counter += 1
		new_temp_dict["type"] = "microservices_group"
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
		for j in i:
			# print (find_node_id(j,data))
			new_temp_dict["nodes"].append(find_node_id(j,data))
		data["clusters"].append(new_temp_dict)

	# Adding final_community
	new_temp_dict = {}
	new_temp_dict["label"] = "unassigned_group"
	new_temp_dict["id"] = str(counter)
	counter += 1
	new_temp_dict["type"] = "unassigned_group"
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
	for j in final_community:
		# print (find_node_id(j,data))
		new_temp_dict["nodes"].append(find_node_id(j,data))

	# Adding table nodes to the unassigned group
	for i in data["nodes"]:
		if i["entity_type"] == "table":
			new_temp_dict["nodes"].append(i["id"])
	data["clusters"].append(new_temp_dict)

	# Adding utility cluster
	new_temp_dict = {}
	new_temp_dict["label"] = "utility_cluster"
	new_temp_dict["id"] = str(counter)
	counter += 1
	new_temp_dict["type"] = "utility_cluster"
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
	for j in utility_cluster:
		# print (find_node_id(j,data))
		new_temp_dict["nodes"].append(find_node_id(j,data))
	data["clusters"].append(new_temp_dict)

	# Adding refactor cluster
	new_temp_dict = {}
	new_temp_dict["label"] = "refactor_candidates_cluster"
	new_temp_dict["id"] = str(counter)
	counter += 1
	new_temp_dict["type"] = "refactor_candidates_cluster"
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
	for j_data in refactor_candidate:
		# print (find_node_id(j,data))
		j = j_data[0]
		new_temp_dict["nodes"].append(find_node_id(j,data))
	data["clusters"].append(new_temp_dict)

	# Adding dead code
	new_temp_dict = {}
	new_temp_dict["label"] = "unreachable_classes"
	new_temp_dict["id"] = str(counter)
	counter += 1
	new_temp_dict["type"] = "unreachable_classes"
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
	for i in dead_code:
		for j in i:
			new_temp_dict["nodes"].append(find_node_id(j,data))
	data["clusters"].append(new_temp_dict)

	
	print ("******************************")

	count_check_2 = len(final_community)
	count_check_2 += len(utility_cluster)
	count_check_2 += len(refactor_candidate)

	for i in save_communities:
		count_check_2 += len(i)
	for i in dead_code:
		count_check_2 += len(i)

	print ("Coverage",(count_check_2-len(final_community))/count_check_2,count_check_2)

	print (len(save_communities))

	save_communities.append([x for x in final_community])
	save_communities.append([x for x in utility_cluster])
	save_communities.append([x[0] for x in refactor_candidate])
	for x in dead_code:
		save_communities.append(x)

	print (count_check, count_check_2)
	print (len(actual_clusters),len(save_communities))

	with open(args.outPutFilePath, 'w') as f:
		json.dump(data, f)
	print ("Saved")

	"""
	For visualisation
	"""
	new_communities = []
	for i in save_communities:
		temp = []
		for j in i:
			temp.append(j.split('.')[-1])
		new_communities.append(temp)


	# for i in new_communities:
	# 	print (i)
	# 	print (len(i))
	# 	print ('\n')
	# 	print ('\n')
	# 	print ('\n')
	# print (len(G.nodes()))
	# exit()
# -------

	counter = 0
	cluster_mapping = {}
	temp_dict={}

	with open(args.outPutFilePath) as json_file:
		data = json.load(json_file)

	for i in data["edges"]:
		if i["type"] == "inter_class_connections":
			data_new = i["relationship"]
			break

	for i in new_communities:
		for j in i:
			cluster_mapping[j] = counter
			name = "cluster"+str(counter)+"."+str(j)
			if name in temp_dict.keys():
				print ("Change")
				exit()
			temp_dict[name] = {}

			temp_dict[name]['size'] = len(i)-1
			
			imports_list = []
			for k in i:
				if k != j:
					for check_i in data_new:
						# print (k,j)
						# print (find_node(check_i["properties"]["start"]).split('.')[-1].encode('UTF8'),find_node(check_i["properties"]["end"]).split('.')[-1].encode('UTF8'),k,j)
						# exit()
						if find_node(check_i["properties"]["start"]).split('.')[-1] == j and find_node(check_i["properties"]["end"]).split('.')[-1] ==k:
							# print ("Hereeeee")
							imports_list.append(str("cluster"+str(counter)+"."+str(k)))
							break

			temp_dict[name]['imports'] = imports_list
			# print(temp_dict)
			#universal.append(temp_dict)
		# exit()
		# print (temp_dict)
		# exit()
		counter = counter + 1

	with open(args.tempFilePath) as json_file:
		data = json.load(json_file)

	# t = cluster_mapping.keys()
	# # print(t[:5])
	# # exit()
	#Adding remaining links
	for i in data.keys():
		# print ("---------------")
		# print (i)
		# print (data[i])
		if data[i]['type'] == 'sink' or data[i]['type'] == 'both':
			# g.add_edge(data[i]['name'], list(data[i]['usedClassesToCount'].keys())[0])
			# i = i.split('.')[-1].encode('UTF8')
			# i = i.encode('UTF8')
			
			if not regexp.search(data[i]['name']) and not regexp.search(list(data[i]['usedClassesToCount'].keys())[0]):
				node_1 = "cluster"+str(cluster_mapping[data[i]['name'].split('.')[-1]])+"."+data[i]['name'].split('.')[-1]
				node_2 = "cluster"+str(cluster_mapping[list(data[i]['usedClassesToCount'].keys())[0].split('.')[-1]])+"."+list(data[i]['usedClassesToCount'].keys())[0].split('.')[-1]
				if node_2 not in temp_dict[node_1]['imports']:
					temp_dict[node_1]['imports'].append(node_2)
					temp_dict[node_1]['size'] += 1
					
				if node_1 not in temp_dict[node_2]['imports']:
					temp_dict[node_2]['imports'].append(node_1)
					temp_dict[node_2]['size'] += 1

			# exit()

		if data[i]['type'] == 'source' or data[i]['type'] == 'both':
			# g.add_edge(list(data[i]['usedByClassesToCount'].keys())[0],data[i]['name'])
			if not regexp.search(data[i]['name']) and not regexp.search(list(data[i]['usedByClassesToCount'].keys())[0]):
				node_1 = "cluster"+str(cluster_mapping[list(data[i]['usedByClassesToCount'].keys())[0].split('.')[-1]])+"."+list(data[i]['usedByClassesToCount'].keys())[0].split('.')[-1]
				node_2 = "cluster"+str(cluster_mapping[data[i]['name'].split('.')[-1]])+"."+data[i]['name'].split('.')[-1]
				
				if node_2 not in temp_dict[node_1]['imports']:
					temp_dict[node_1]['imports'].append(node_2)
					temp_dict[node_1]['size'] += 1
					# print (temp_dict[node_1])
					
				if node_1 not in temp_dict[node_2]['imports']:
					temp_dict[node_2]['imports'].append(node_1)
					temp_dict[node_2]['size'] += 1

	#Mading json structure

	universal=[]


	for i in temp_dict.keys():
		new_temp_dict = {}
		new_temp_dict['name'] = i
		new_temp_dict['size'] = temp_dict[i]['size']
		new_temp_dict['imports'] = temp_dict[i]['imports']
		universal.append(new_temp_dict)

	# print (universal)
	# print (sorted(map(sorted, next_level_communities)))
	with open(args.visFilePath, 'w') as f:
		json.dump(universal, f)

	# except Exception as error:
	# 	print("ERR: "+repr(error))  
	# 	sys.exit(-1)