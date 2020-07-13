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

import os
import utils
import numpy as np
import pickle

class CallGraph(object):
    """
    Represents a call graph at both method level and class level
    Contains structures to hold graph information as well as
    additional information about the nodes
    """

    def __init__(self, callgraph_file, filtered_classes, entrypoints):
        self.allnodes = {}
        self.allclassnodes = {}
        self.filtered_classes = filtered_classes
        self.entrypoints = entrypoints

        self.build_callgraph(callgraph_file, entrypoints)
        self.build_class_callgraph()
        self.build_class_cooccurence_matrix(True)

    def build_callgraph(self, callgraph_file, entrypoints):
        with open(callgraph_file, 'r') as f:
            count = 0
            for line in f:
                if not line[0] == "\"":
                    continue
                nodefrom, nodeto = utils.parse_callgraph_line(line.strip())
                # Check if we got valid nodes from parsing
                if nodefrom is None or nodeto is None:
                    print("Invalid node returned from parsing. Skipping")
                    continue

                # If we have not seen these nodes before, add them to node list
                # If we have seen them, make sure their new annotations are saved
                for node in (nodefrom, nodeto):
                    existingnode = self.allnodes.get(node.get_name())
                    if existingnode is None:
                        self.allnodes[node.get_name()] = node
                    else:
                        for anno in node.get_annotations():
                            existingnode.add_annotation(anno)

                # Get the saved versions of the nodes
                nodefrom = self.allnodes[nodefrom.get_name()]
                nodeto = self.allnodes[nodeto.get_name()]

                # Make the link
                nodefrom.add_outgoing_link(nodeto)
                count += 1
                if count % 1000000 == 0:
                    print("Parsed", count, "lines so far ..")

            print("Parsed", count, "lines from the file:", callgraph_file)
            print("The graph has", len(self.allnodes), "nodes.")

    def build_class_callgraph(self):
        """
        Takes the method level call graph and generates a class level call graph
        """
        # Create entries in the list for each node
        for nodename in self.allnodes:
            node = self.allnodes[nodename]
            classname = node.get_classname()

            if classname not in self.filtered_classes:
                continue

            classnode = self.allclassnodes.get(classname)
            if classnode is None:
                classnode = utils.ClassNode(classname)
                self.allclassnodes[classname] = classnode

        # Now add links and annotations since all possible nodes have been saved
        for nodename in self.allnodes:
            node = self.allnodes[nodename]
            if node.get_classname() not in self.filtered_classes:
                continue

            classnode = self.allclassnodes[node.get_classname()]

            outlinks = node.get_outgoing_links()
            for outnodename in outlinks.keys():
                outnode = outlinks[outnodename]
                outclassname = outnode.get_classname()
                if outclassname not in self.filtered_classes:
                    continue
                if node.get_classname() == outclassname:
                    continue
                outclassnode = self.allclassnodes[outclassname]

                classnode.add_outgoing_link(outclassnode)

            for anno in node.get_annotations():
                classnode.add_annotation(anno)

        print("The class graph has", len(self.allclassnodes), "nodes.")
        # for cc in self.filtered_classes:
        #     if cc not in self.allclassnodes:
        #         print("Nodes in ICU but not in ClassCallgraph:", cc)
        # for cc in self.allclassnodes:
        #     if cc not in self.filtered_classes:
        #         print("Nodes in ClassCallgraph but not in ICU:", cc)


    def get_outgoing_classnodes(self, classname):
        classnode = self.allclassnodes[classname]
        return classnode.get_outgoing_links()

    def build_class_cooccurence_matrix(self, bidirectional=False):
        # init counts and dicts
        self.class_forward_counts = {}
        self.class_base_counts = {}
        self.visited = {}
        self.uniquepath_count = 0
        self.allclassesfrommethods = []

        print("Generating base counts ..")

        for classname in self.filtered_classes:
            self.class_forward_counts[classname] = {}
            self.class_base_counts[classname] = 0
            for secondclass in self.filtered_classes:
                self.class_forward_counts[classname][secondclass] = 0

        print("Populating actual counts ..")

        for entrypoint_name, methods in self.entrypoints.items():
            for ep_methodname in methods:
                #print("epmeth", ep_methodname)
                current_class = self.allnodes[ep_methodname].get_classname()
                if current_class in self.filtered_classes:
                    pathsofar = [ep_methodname]
                    self.dfs_traverse_new(ep_methodname, entrypoint_name, pathsofar, bidirectional)

        print("Total unique paths:", self.uniquepath_count)

        nk = len(self.filtered_classes)
        matrix = np.zeros((nk,nk))
        for i,r in enumerate(self.class_forward_counts.keys()):
            for j,c in enumerate(self.class_forward_counts[r].keys()):
                matrix[i][j] = self.class_forward_counts[r][c]

        print("Matrix norm:", np.mean(matrix), np.max(matrix))

    def dfs_traverse_new(self, methodname, entrypoint_name, pathsofar, bidirectional):
        currentnode = self.allnodes[methodname]
        neighbors = currentnode.get_outgoing_links().keys()
        unvisited_nodes = []
        basepath = (currentnode, [methodname])

        for n in neighbors:
            neighbor_node = self.allnodes[n]
            neighborclass = neighbor_node.get_classname()

            if entrypoint_name in neighbor_node.get_annotations() and neighborclass in self.filtered_classes:
                unvisited_nodes.append((neighbor_node, basepath[1]+[n]))

        while len(unvisited_nodes) > 0:
            (nextnode, pathsofar) = unvisited_nodes[-1]
            unvisited_nodes = unvisited_nodes[:-1]

            neighbors = nextnode.get_outgoing_links().keys()
            valid_count = 0
            for n in neighbors:
                neighbor_node = self.allnodes[n]
                neighborclass = neighbor_node.get_classname()
                if entrypoint_name in neighbor_node.get_annotations() and neighborclass in self.filtered_classes:
                    if n not in pathsofar:
                        unvisited_nodes.append((neighbor_node, pathsofar+[n]))
                        valid_count += 1

            if valid_count == 0:
                self.process_path(pathsofar, bidirectional)
            if len(pathsofar) > 20:
                continue

    def process_path(self, path, bidirectional):
        pathclassnames = []
        for methodname in path:
            classname = self.allnodes[methodname].get_classname()
            if classname not in pathclassnames: # new class name not seen so far in the path
                pathclassnames.append(classname)
            else:
                if classname != pathclassnames[-1]: # not a new class but seen at least 2 hops ago
                    pathclassnames.append(classname)

        for classname in pathclassnames:
            self.class_base_counts[classname] += 1

        maxidx = len(pathclassnames)
        if bidirectional:
            for i in range(maxidx):
                for j in range(maxidx):
                    firstclass = pathclassnames[i]
                    secondclass = pathclassnames[j]
                    countdict = self.class_forward_counts[firstclass]
                    countdict[secondclass] += 1
                    self.class_forward_counts[firstclass] = countdict
        else:
            for i in range(maxidx):
                for j in range(i,maxidx):
                    firstclass = pathclassnames[i]
                    secondclass = pathclassnames[j]
                    countdict = self.class_forward_counts[firstclass]
                    countdict[secondclass] += 1
                    self.class_forward_counts[firstclass] = countdict

    def construct_the_matrix(self, class2idx, normalize=False):
        """
        self.allclassnodes and self.class_forward_counts has only some nodes
        construct the matrix in terms of all filtered classes
        """

        num_classes = len(self.filtered_classes)
        matrix = np.zeros((num_classes, num_classes))

        for row in self.class_forward_counts.keys():
            for col in self.class_forward_counts[row].keys():
                value = self.class_forward_counts[row][col]
                r_idx = class2idx[row]
                c_idx = class2idx[col]
                if normalize:
                    if self.class_base_counts[row] > 0:
                        matrix[r_idx,c_idx] = value / float(self.class_base_counts[row])
                else:
                    matrix[r_idx,c_idx] = value

        self.cooccurence_matrix = matrix
        return matrix
