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


	

if __name__ == "__main__":


	

	parser = argparse.ArgumentParser()
	parser.add_argument('--inPutFilePath')
	parser.add_argument("--outPutFilePath")
	parser.add_argument('--seed_file')
	parser.add_argument('--tempFilePath')
	parser.add_argument('--visFilePath')
	

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
		seeds = [x.split() for x in seeds]
		print ("Seeds Loaded\n")
		

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

	# print ("------------------------------------------------")
	# print (final_community)
	
	# save_communities.append([x.encode('UTF8') for x in final_community])
	save_communities.append([x for x in final_community])
	# print (save_communities)
	# exit()
	print ("this is ",count)
	print ("Here",len(G.nodes()))
	data["clusters"] = []
	counter = 0
	for i in save_communities:
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
			print (find_node_id(j,data))
			new_temp_dict["nodes"].append(find_node_id(j,data))
		data["clusters"].append(new_temp_dict)

	with open(args.outPutFilePath, 'w') as f:
		json.dump(data, f)

	"""For visualisation"""
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
		print (counter)

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
			

			node_1 = "cluster"+str(cluster_mapping[data[i]['name'].split('.')[-1]])+"."+data[i]['name'].split('.')[-1]
			# print (node_1)
			node_2 = "cluster"+str(cluster_mapping[list(data[i]['usedClassesToCount'].keys())[0].split('.')[-1]])+"."+list(data[i]['usedClassesToCount'].keys())[0].split('.')[-1]
			
			print (node_1, str(cluster_mapping[data[i]['name'].split('.')[-1]]))
			print (node_2,str(cluster_mapping[list(data[i]['usedClassesToCount'].keys())[0].split('.')[-1]]))
			# exit()
			if node_2 not in temp_dict[node_1]['imports']:
				print ("added1--------------")
				print (node_1)
				print (node_2)
				# print (temp_dict[node_1]['imports'])
				# exit()
				print ("--------------")
				temp_dict[node_1]['imports'].append(node_2)
				temp_dict[node_1]['size'] += 1
				print (temp_dict[node_1])
				
			if node_1 not in temp_dict[node_2]['imports']:
				print ("added1--------------")
				print (node_1)
				print (node_2)
				print ("--------------")
				temp_dict[node_2]['imports'].append(node_1)
				temp_dict[node_2]['size'] += 1

			# exit()

		if data[i]['type'] == 'source' or data[i]['type'] == 'both':
			# g.add_edge(list(data[i]['usedByClassesToCount'].keys())[0],data[i]['name'])
			node_1 = "cluster"+str(cluster_mapping[list(data[i]['usedByClassesToCount'].keys())[0].split('.')[-1]])+"."+list(data[i]['usedByClassesToCount'].keys())[0].split('.')[-1]
			node_2 = "cluster"+str(cluster_mapping[data[i]['name'].split('.')[-1]])+"."+data[i]['name'].split('.')[-1]
			
			if node_2 not in temp_dict[node_1]['imports']:
				print ("added2")
				# print (node_1)
				# print (node_2)
				temp_dict[node_1]['imports'].append(node_2)
				temp_dict[node_1]['size'] += 1
				# print (temp_dict[node_1])
				
			if node_1 not in temp_dict[node_2]['imports']:
				print ("added")
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
