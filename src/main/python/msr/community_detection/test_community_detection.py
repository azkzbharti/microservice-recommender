import unittest
import json
from community_detection import find_node, find_node_id

with open('graph.json') as json_file:
    data = json.load(json_file)

for i in data["edges"]:
	if i["type"] == "inter_class_connections":
		data_read = i["relationship"]
		break


class TestFindNode(unittest.TestCase):
	"""Test functionality of find_node
	Input: Node id
	Output: Node name
	"""
	def test_find_node(self):
		self.assertAlmostEqual(find_node("0"),"com.ibm.websphere.samples.daytrader.beans.RunStatsDataBean")

class TestFindID(unittest.TestCase):
	"""Test functionality of find_node
	Input: Node name
	Output: Node id
	"""
	def test_find_node(self):
		self.assertAlmostEqual(find_node_id("com.ibm.websphere.samples.daytrader.beans.RunStatsDataBean"),"0")