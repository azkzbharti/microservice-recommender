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

import numpy as np
import distance
import networkx as nx
from networkx.algorithms import community
from utils import find_node_type, find_node, find_node_id, check_community
from utils import get_clusters_and_edges, compute_cohesion_coupling

class Metrics(object):

	@staticmethod
	def get_conceptual_independence(data):
		comm = []
		count = 0
		comm_data = data["clusters"]
		for i in comm_data:
			temp = []
			for j in i["nodes"]:
				if find_node_type(data, j):
					name = find_node(data, j)
					name = name.split(".")[-1]
					temp.append(name)
			comm.append(temp)

		community_score = []
		for i in comm:
			score = 0
			words = np.asarray(i)
			for j in words:
				for k in words:
					# print (distance.levenshtein(j, k)/max(len(j), len(k)))
					score += 1 - distance.levenshtein(j, k) / max(len(j), len(k))
			community_score.append(score/len(i)**2)

		total_score = 0
		for i in community_score:
			total_score += i

		total_score = total_score / len(community_score)

		return total_score

	@staticmethod
	def get_coverage(data):
		comm = []
		count = 0
		comm_data = data["clusters"]
		size1 = 0
		size2 = 0
		set1 = set()
		set2 = set()
		for i in comm_data:
			#print (i["type"])
			if i["type"] != "unassigned_group":
				for j in i["nodes"]:
					if find_node_type(data, j):
						set1.add(j)
						set2.add(j)
			for j in i["nodes"]:
				if find_node_type(data, j):
					set2.add(j)


		total_score = len(set1) / len(set2)
		return total_score

	@staticmethod
	def get_modularity(data):
		for i in data["edges"]:
			if i["type"] == "inter_class_connections":
				data_read = i["relationship"]
				break

		g = nx.DiGraph() #Directed Graph
		for i in data_read:
			g.add_edge(find_node(data, i["properties"]["start"]), find_node(data, i["properties"]["end"]))


		B = nx.directed_modularity_matrix(g)
		node_list = g.nodes()

		comm = []
		count = 0
		comm_data = data["clusters"]
		for i in comm_data:
			temp = []
			for j in i["nodes"]:
				if find_node_type(data, j):
					temp.append(find_node(data, j))
			comm.append(temp)
			count += len(temp)

		""" Calculating modularity score """
		modularity_score = 0
		for i,i_node in enumerate(node_list):
			for j,j_node in enumerate(node_list):
				value = check_community(i_node,j_node, comm)
				if value == 1:
					modularity_score += B.item((i,j))

		number_of_edges = g.number_of_edges()
		score = modularity_score/number_of_edges

		return score

	@staticmethod
	def get_structural_modularity(data):
		clusters, edge = get_clusters_and_edges(data)
		cohesion, coupling = compute_cohesion_coupling(clusters, edge)

		return cohesion

	@staticmethod
	def get_structural_cohesivity(data):
		clusters, edge = get_clusters_and_edges(data)
		cohesion, coupling = compute_cohesion_coupling(clusters, edge)

		return cohesion-coupling

	@staticmethod
	def get_ned(data, low_lim=5, high_lim=100):
		total_length = 0
		for i in data["nodes"]:
			if i["entity_type"] == 'class':
				total_length += 1
				
		valid_len = 0
		comm = []
		count = 0
		comm_data = data["clusters"]
		for i in comm_data:
			if len(i["nodes"]) >= low_lim and len(i["nodes"]) <= high_lim:
				valid_len += len(i["nodes"])

		total_score = valid_len / total_length
		return total_score
