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

import unittest
import json
from community_cluster import find_node_id, find_node
import os
import re

os.system('python create_schema.py --inPutFilePath test/inter-class-usage.json --outPutFilePath test/graph.json --graphInputForSeed test/for_seeded --filterFilePath test/filter.txt --transactionName test/transaction.json')


with open('test/filter.txt') as f:
	filter_read = f.readlines()
	regexp = re.compile(filter_read[0].strip())
print (regexp)

with open('test/graph.json') as json_file:
    schema = json.load(json_file)

class TestFindNodeId(unittest.TestCase):
	"""Test functionality of find_node_id
	Input: Node name
	Output: Node id
	"""
	def test_find_node_id(self):
		self.assertAlmostEqual(int(find_node_id("com.ibm.websphere.samples.daytrader.beans.RunStatsDataBean", schema)),0)

for i in schema["edges"]:
	if i["type"] == "inter_class_connections":
		edges = i["relationship"]
edge_list_classes = []
for i in edges:
	edge_list_classes.append((i["properties"]["start"],i["properties"]["end"]))

for i in schema["edges"]:
	if i["type"] == "transaction_relatedeness":
		edges = i["relationship"]

edge_list_transaction = []
for i in edges:
	edge_list_transaction.append((i["properties"]["start"],i["properties"]["end"]))

class TestClassEdges(unittest.TestCase):
	"""Test functionality of verifying unique class edges
	Input: inter-class-connection edge list
	"""
	def test_class_edges(self):
		self.assertAlmostEqual(len(edge_list_classes),len(set(edge_list_classes)))

class TestTransactionEdges(unittest.TestCase):
	"""Test functionality of verifying unique class edges
	Input: inter-class-connection edge list
	"""
	def test_transaction_edges(self):
		self.assertAlmostEqual(len(edge_list_transaction),len(set(edge_list_transaction)))

os.system('python community_cluster.py --inPutFilePath test/for_seeded --tempFilePath test/inter-class-usage.json --outPutFilePath test/graph.json  --visFilePath test/vis.json --seed_file test/seeds.txt --filterFilePath test/filter.txt --serviceEntry test/service.json')		