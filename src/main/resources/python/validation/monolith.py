"""*******************************************************************************
* Licensed Materials - Property of IBM
* (c) Copyright IBM Corporation ${year}. All Rights Reserved.
*
* Note to U.S. Government Users Restricted Rights:
* Use, duplication or disclosure restricted by GSA ADP Schedule
* Contract with IBM Corp.
*******************************************************************************"""

"""
Author: Utkarsh Desai
Maintenance: Utkarsh Desai
"""

import json
import numpy as np
from graph import CallGraph

class Monolith(object):
    """
    Represents the monolith application. Stores and processes all relevant static analysis results
    Static analysis is not done here and is expected to be performed elsewhere
    """
	def __init__(self, icu_fie, callgraph_file, entrypoint_file):

		self.icu = []
		self.implements = []
		self.filtered_classes = []
		self.num_icu_classes = 0

		self.icuclass2idx = {}
		self.idx2icuclass = {}

		print("parsing ICU ..")
		self.inter_class_usage = self.parse_inter_class_usage(icu_fie)

		print("parsing entrypoints ..")
		self.entrypoints = self.parse_entrypoints(entrypoint_file)

		print("parsing call-graph ..")
		self.graph = CallGraph(callgraph_file, self.filtered_classes, self.entrypoints)

	def parse_entrypoints(self, entrypoint_file):
        """Parse the entrypoint file and extract relations"""
        
		with open(entrypoint_file, 'r') as f:
			data = json.load(f)

		entrypoints = {}
		for item in data:
			name = item["service_entry_name"]
			# this has tobedone because of the new format
			if "{" in name:
				name = name.split(",")[1].split()[1]
			classes = item["class_method_name"]
			entrypoints[name] = classes

		return entrypoints

	def parse_inter_class_usage(self, icu_file):
		"""
		Parse the interclass usage file. Generates multiple data structures.
		"""
		print("Parsing interclass usage ..")
		with open(icu_file, "r") as fp:
			icu_json = json.load(fp)

		self.all_icu_classes = icu_json.keys()
		self.num_icu_classes = len(self.all_icu_classes)
		print("Found", self.num_icu_classes, "classes in inter class usage file.")

		# set them as filtered classes, will be updated later if we use filters
		self.filtered_classes = self.all_icu_classes

		for i,classname in enumerate(self.all_icu_classes):
			self.icuclass2idx[classname] = i
			self.idx2icuclass[i] = classname

		self.icu = np.zeros((self.num_icu_classes, self.num_icu_classes), dtype=np.int8)
		self.implements = np.zeros((self.num_icu_classes, self.num_icu_classes), dtype=np.int8)

		# Populate the ICU matrix
		for classname in self.all_icu_classes:
			classdata = icu_json[classname]

			used_class_dict = classdata["usedClassesToCount"]
			usedby_class_dict = classdata["usedByClassesToCount"]

			# Outgoing edges
			row = self.icuclass2idx[classname]
			for k,v in used_class_dict.items():
				col = self.icuclass2idx[k]
				self.icu[row][col] = v

			# Incoming edges
			col = self.icuclass2idx[classname]
			for k,v in usedby_class_dict.items():
				row = self.icuclass2idx[k]
				self.icu[row][col] = v

			# Parent classes and interfaces
			c_idx = self.icuclass2idx[classname]
			parent = classdata.get("superClass")
			interfaces = classdata.get("implementedInterfaces")
			if parent is not None and parent[:4] != "java":
				parentidx = self.icuclass2idx[parent]
				self.implements[c_idx][parentidx] = 1
			if interfaces is not None:
				for interface_name in interfaces:
					if interface_name[:4] != "java":
						interface_idx = self.icuclass2idx[interface_name]
						self.implements[c_idx][interface_idx] = 1
