


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
from creating_interclass_schema import node_used_to, node_used_by, find_node_id

with open('inter-class-usage.json') as json_file:
    data = json.load(json_file)

with open('graph.json') as json_file:
    schema = json.load(json_file)

test1 = data[list(data.keys())[1]]
test2 = data[list(data.keys())[0]]

class TestNodeUsedTo(unittest.TestCase):
	"""Test functionality of node_used_to
	Input: Node structure
	Output: Total number of nodes used by current node
	"""
	def test_used_to(self):
		self.assertAlmostEqual(node_used_to(test1),3)

class TestNodeUsedBy(unittest.TestCase):
	"""Test functionality of node_used_by
	Input: Node structure
	Output: Total number of times current node used by other nodes
	"""
	def test_used_by(self):
		self.assertAlmostEqual(node_used_by(test2),44)

test3 = schema["nodes"][0]
class TestFindNodeId(unittest.TestCase):
	"""Test functionality of find_node_id
	Input: Node name
	Output: Node id
	"""
	def test_find_node_id(self):
		self.assertAlmostEqual(int(find_node_id("com.ibm.websphere.samples.daytrader.beans.RunStatsDataBean")),0)
		