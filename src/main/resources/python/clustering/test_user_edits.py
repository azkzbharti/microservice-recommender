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


os.system('python user_edits.py --originalFile test/graph.json --editFile test/graph_edit.json --seedFile test/seeds.txt --editSeed test/edit_seed.txt --editInfo test/edit.json')

with open('test/edit_seed_check.txt') as f:
	seeds = f.readlines()
	seeds = [x.strip() for x in seeds]
	seeds = [x.split(",") for x in seeds]
	seeds = [sorted([y.split(".")[-1]for y in x]) for x in seeds]
	seed_check = seeds

with open('test/edit_seed.txt') as f:
	seeds = f.readlines()
	seeds = [x.strip() for x in seeds]
	seeds = [x.split(",") for x in seeds]
	seeds = [sorted([y.split(".")[-1]for y in x]) for x in seeds]
	seed_created = seeds

with open('test/edit_check.json') as json_file:
    edit_check = json.load(json_file)

with open('test/edit.json') as json_file:
    edit_created = json.load(json_file)

class TestEditSeed(unittest.TestCase):
	"""Test functionality of find_node_id
	Input: True seed list after user edits
	Output: Generated seed list
	"""
	def test_edit_seed(self):
		print (seed_check)
		self.assertListEqual(seed_check, seed_created)

class TestEditSchema(unittest.TestCase):
	"""Test functionality of verifying unique class edges
	Input: inter-class-connection edge list
	"""
	def test_edit_schema(self):
		self.assertListEqual(sorted(edit_check['utility_group']),sorted(edit_created['utility_group']))
		self.assertListEqual(sorted(edit_check['refactor_candidates_group']),sorted(edit_created['refactor_candidates_group']))

# class TestTransactionEdges(unittest.TestCase):
# 	"""Test functionality of verifying unique class edges
# 	Input: inter-class-connection edge list
# 	"""
# 	def test_transaction_edges(self):
# 		self.assertAlmostEqual(len(edge_list_transaction),len(set(edge_list_transaction)))
