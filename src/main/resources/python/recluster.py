import numpy as np
import reclusterutils as utils
import sys
import json

WTS = True
debug = False

class Clustering(object):

    def __init__(self, outputfile=None, clusterjson=None, usagejson=None, project=None, cohesion_improv_threshold=0.05, cohesion_threshold=0.5):

        print("Loading data and processing ..")

        self._cohesion_improv_threshold = cohesion_improv_threshold
        self._cohesion_threshold = cohesion_threshold
        if project is not None:
            clustering = utils.get_clusters(project=project)
        elif clusterjson is not None:
            clustering = utils.get_clusters(clusterjson=clusterjson)
        else:
            print("Missing cluster json.")
            sys.exit(1)

        # self._clusters is a list of tuples (name, memberlist) for each cluster in the clustering
        self._clusters = [(utils.parse_cluster(c)) for c in clustering]
        self._clusters_map = None

        self._classnames = utils.get_unique_classnames(self._clusters)
        print("Unique classes found:", len(self._classnames))

        if project is not None:
            usage_map = utils.get_api_usage(project=project)
        elif usagejson is not None:
            usage_map = utils.get_api_usage(usagejson=usagejson)
        else:
            print("Missing usage json files.")
            sys.exit(1)
        self._usage_matrix, self._class2idx, self._idx2class = utils.get_usage_matrix(usage_map, self._classnames)

        self._clusterid2classid, self._classid2clusterid, self._max_clusterid = self._generate_ids()

        print("Finished parsing clusters.")

        self._clusterinfo = (self._clusters, self._classnames, self._clusterid2classid, self._classid2clusterid)
        self._usageinfo = (self._usage_matrix, self._class2idx, self._idx2class)

        if debug:
            print("Current Clustering:")
            for cid in self._clusterid2classid.keys():
                print("Cluster:", cid)
                for m in self._clusterid2classid[cid]:
                    print("\t", self._idx2class[m])

        self._outputfile = outputfile

    def _generate_ids(self):
        """ creates the maps to go from ids to class names and back """
        clusterid = 0

        cluster2class = {}
        class2cluster = {}
        clusters = {}

        for (name,members) in self._clusters:
            classes = [self._class2idx[m] for m in members]
            for c in classes:
                class2cluster[c] = clusterid
            #    print(c, clusterid)
            cluster2class[clusterid] = classes
            clusters[clusterid] = (name, classes)
            clusterid += 1
        self._clusters_map = clusters

        return cluster2class, class2cluster, clusterid-1

    def _sort_clusters_bysize(self):
        """returns a list of clusterids sorted by cluster size """
        idsize_map = {}
        for k in self._clusterid2classid.keys():
            idsize_map[k] = len(self._clusterid2classid[k])

        sortedlist = []
        for k in sorted(idsize_map, key=idsize_map.get, reverse=True):
            sortedlist.append(k)
        return sortedlist

    def _find_intercluster_prop(self, classid, apicalls, clusterclassids, ignorelist=[], weighted=True):
        """ computes the cohesion score for a single class in a cluster """

        if weighted is None:
            num = 0
            for i,c in enumerate(apicalls):
                if i in ignorelist:
                    continue
                if c > 0. and i in clusterclassids:
                    num += 1
            return num

        if weighted:
            den = 0.
            num = 0.
            for i,c in enumerate(apicalls):
                if i in ignorelist:
                    continue
                den += c
                if i in clusterclassids:
                    num += c
            #print(apicalls, classid, num, den)
            if den == 0:
                return 0.
            return num / den

        else:
            den = len(clusterclassids) - len(ignorelist)
            num = 0.
            for i,c in enumerate(apicalls):
                if i in ignorelist:
                    continue
                if c > 0. and i in clusterclassids:
                    num += 1.
            return num / den

    def _find_adhoc_cluster_cohesion(self, clustermembers):
        cluster_cohesion = 0.

        for classid in clustermembers:
            apicalls = self._usage_matrix[classid]
            class_intercluster_prop = self._find_intercluster_prop(classid, apicalls, clustermembers, weighted=WTS)
            self._class_intercluster_props[classid] = class_intercluster_prop
            cluster_cohesion += class_intercluster_prop

        if WTS is None:
            return cluster_cohesion
        else:
            return cluster_cohesion / len(clustermembers)

    def _find_cluster_cohesion(self, clusterid, ignorelist=[]):
        """ finds the cohesion score for a given cluster id """
        classids = self._clusterid2classid[clusterid]

        if len(classids) == 0:
            print("No classes in cluster", clusterid)
            return 0.

        cluster_cohesion = 0.

        for classid in classids:
            if classid in ignorelist:
                continue
            #get the row for this class
            apicalls = self._usage_matrix[classid]
            # if classid == 10:
            #     print("for clusterid:", clusterid, "classid", classid, apicalls)
            class_intercluster_prop = self._find_intercluster_prop(classid, apicalls, classids, ignorelist, weighted=WTS)
            self._class_intercluster_props[classid] = class_intercluster_prop
            cluster_cohesion += class_intercluster_prop

        if WTS is None:
            return cluster_cohesion
        else:
            return cluster_cohesion / (len(classids) - len(ignorelist))

    def analyze_coupling(self):
        class_scores = {}

        # find cohesion scores
        for cid in self._newclusters:
            (n,m,l) = self._newclusters[cid]
            cohesion = self._find_adhoc_cluster_cohesion(clustermembers=m)
            for mem in m:
                class_scores[mem] = cohesion
        for c in self._openclasses:
            if self._openclasses[c] != -1:
                class_scores[c] = 1

        # find coupling
        for classid in class_scores.keys():
            clusters_used_by = []
            for i,c in enumerate(self._usage_matrix[classid]):
                if c > 0 or self._usage_matrix[i,classid] > 0:
                    clusterid = self._classid2clusterid[i]
                    if clusterid == -1:
                        #clusterid = "nocluster"+str(classid)
                        continue
                    clusters_used_by.append(clusterid)

            clusters_used_by = list(set(clusters_used_by))
            #class_scores[c] = class_scores[c] / (1.0 * len(clusters_used_by))
            class_scores[classid] = len(clusters_used_by)


        for k in sorted(class_scores, key=class_scores.get, reverse=True):
            print("class:", self._idx2class[k], "score:", class_scores[k])

        return class_scores

    def recluster_cohesion(self):
        """ analyzes cohesion properties of the current clustering """
        self._class_intercluster_props = {}

        new_clusters = {}
        open_classes = {}

        if debug:
            print("High positive score means they should go, high negative score means the classes should stay.\n")

        sorted_clusterids = self._sort_clusters_bysize()
        for clusterid in sorted_clusterids:
            cohesion = self._find_cluster_cohesion(clusterid)
            cluster_name = self._clusters_map[clusterid][0]
            cluster_members = self._clusters_map[clusterid][1]

            if debug:
                print("Cluster:", cluster_name, "Cluster ID:", clusterid, "Mean Cohesion:", cohesion)
            removal_scores = {}
            for classid in self._clusterid2classid[clusterid]:
                new_cohesion = self._find_cluster_cohesion(clusterid=clusterid, ignorelist=[classid])
                removal_scores[classid] = new_cohesion

            for k in sorted(removal_scores, key=removal_scores.get, reverse=True):
                change =  removal_scores[k] - cohesion
                if debug:
                    print("\tID", k, "\t", self._idx2class[k], "\tScore:", change)

            if cohesion == 0.0 or len(cluster_members) < 3:
                for m in cluster_members:
                    open_classes[m] = clusterid
                    self._classid2clusterid[m] = -1
                #new_clusters[clusterid] = (cluster_name, [])
            else:
                sorted_idxs = sorted(removal_scores, key=removal_scores.get, reverse=False)
                open_classes[sorted_idxs[0]] = clusterid
                new_clusters[clusterid] = (cluster_name, sorted_idxs[1:], False)
                self._classid2clusterid[sorted_idxs[0]] = -1
            # print("\n--- update ---")
            # print(open_classes)
            # print(new_clusters)

        # fit the open classes now
        for c in open_classes.keys():
            original_clusterid = open_classes[c]

            best_change = 0.
            best_clusterid = -1
            for clusterid in new_clusters.keys():
                (cluster_name, members, locked) = new_clusters[clusterid]

                if locked:
                    continue

                curr_cluster_cohesion = self._find_adhoc_cluster_cohesion(clustermembers=members)
                new_cluster_cohesion = self._find_adhoc_cluster_cohesion(clustermembers=members+[c])

                #if new_cluster_cohesion < curr_cluster_cohesion:
                #    continue

                change = new_cluster_cohesion - curr_cluster_cohesion
                if change > best_change:
                    best_change = change
                    best_clusterid = clusterid

            if best_clusterid != -1:
                if original_clusterid == best_clusterid:
                    locked = True

                (bn, bm, bl) = new_clusters[best_clusterid]
                new_clusters[best_clusterid] = (bn, bm+[c], bl)
                open_classes[c] = -1
                if debug:
                    print("Assigned class:", c, "to cluster", best_clusterid)
                self._classid2clusterid[c] = best_clusterid
            else:
                print("Couldn't reassign class:", c, "to any cluster.")

        if debug:
            for cid in new_clusters:
                (n,m,l) = new_clusters[cid]
                print("Cluster:", cid, "Members:", m)

        self._newclusters = new_clusters
        self._openclasses = open_classes

        self._couplingscores = self.analyze_coupling()

        #self._write_clusters_simple(new_clusters, open_classes)
        self._write_clusters_d3json(new_clusters, open_classes, self._outputfile)

    def _write_clusters_simple(self, clusters, open_classes):
        print("\n")
        for cid in clusters:
            (n,m,l) = clusters[cid]
            print("Cluster:", cid)
            for mem in m:
                print("\t", self._idx2class[mem])
        print("Unassigned:")
        for c in open_classes:
            if open_classes[c] != -1:
                print("\t", self._idx2class[c])

    def _write_clusters_d3json(self, new_clusters, open_classes, outputfile):
        jsonobj = []

        prefixmap = {}
        for cid in new_clusters:
            (n,m,l) = new_clusters[cid]
            prefix = "cluster"+str(cid)+"."

            for mem in m:
                classname = self._idx2class[mem]
                if prefixmap.get(mem) is not None:
                    print ("Duplicate entry", mem)
                else:
                    prefixmap[mem] = prefix + self._idx2class[mem]

        for classid in open_classes:
            if open_classes[classid] != -1:
                if prefixmap.get(classid) is not None:
                    print ("Duplicate no clus entry", classid)
                else:
                    prefixmap[classid] = "nocluster"+str(classid)+"."+self._idx2class[classid]

        jsonobj = []
        for cid in new_clusters:
            (n,m,l) = new_clusters[cid]

            for member in m:
                dictobj = {}
                dictobj['name'] = prefixmap[member]
                imports = []
                for i,c in enumerate(self._usage_matrix[member]):
                    if c > 0:
                        if self._classid2clusterid[i] == -1:
                            imports.append(prefixmap[i])
                        else:
                            imports.append(prefixmap[i])
                        if debug:
                            print( "--", prefixmap[member], "-", prefixmap[i], "-", c)
                    # else:
                    #     if self._usage_matrix[i,member] > 0:
                    #         imports.append(prefixmap[i])
                dictobj['size'] = len(imports)
                dictobj['imports'] = imports
                #print(member, classname)
                jsonobj.append(dictobj)

        for classid in open_classes:
            if open_classes[classid] == -1:
                continue
            dictobj = {}
            dictobj['name'] = prefixmap[classid]

            imports = []
            for i,c in enumerate(self._usage_matrix[classid]):
                if c > 0:
                    imports.append(prefixmap[i])
                # else:
                #     imports.append(prefixmap[i])
                if debug:
                    print( "-- -open- --", prefixmap[classid], "-", prefixmap[i], "-", c)
                # else:
                #     if self._usage_matrix[i,classid] > 0:
                #         imports.append(prefixmap[i])

            dictobj['size'] = len(imports)
            dictobj['imports'] = imports
            jsonobj.append(dictobj)

            with open(outputfile, 'w') as fp:
                json.dump(jsonobj, fp)


    def get_class_info(self):

        print("*** Class information ***")
        for idx in self._idx2class.keys():
            print("ID:", idx, "Name:", self._idx2class[idx])
            apicalls = self._usage_matrix[idx,:]
            calledby = self._usage_matrix[:,idx]

            outputstr = ""
            callmap = {}
            for i,c in enumerate(apicalls):
                if c > 0:
                    if WTS:
                        callmap[i] = int(c)
                    else:
                        callmap[i] = 1
            for k in sorted(callmap, key=callmap.get,reverse=True):
                outputstr = outputstr + " " + self._idx2class[k]+":"+str(callmap[k])
            print("\tUses:",outputstr)

            outputstr = ""
            callmap = {}
            for i,c in enumerate(calledby):
                if c > 0:
                    if WTS:
                        callmap[i] = int(c)
                    else:
                        callmap[i] = 1
            for k in sorted(callmap, key=callmap.get,reverse=True):
                outputstr = outputstr + " " + self._idx2class[k]+":"+str(callmap[k])
            print("\tUsed by:",outputstr)
