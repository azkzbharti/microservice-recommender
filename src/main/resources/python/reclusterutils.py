import numpy as np
import json

# Helper functions to load and process data

def get_clusters(project=None, clusterjson=None):

    if project is not None:
        filename = project + ".json"
    elif clusterjson is not None:
        filename = clusterjson
    with open(filename, 'r') as f:
        jsonobj = json.load(f)
        return jsonobj["children"]

def get_unique_classnames(clustering):
    classnames = []
    for (n,members) in clustering:
        for m in members:
            classnames.append(m)
    return list(set(classnames))

def parse_cluster(clusterobj):
    clustername = clusterobj["name"]
    clustermembers = clusterobj["children"]

    member_classes = []
    for m in clustermembers:
        member_classes.append(m["name"])

    return clustername, member_classes

def get_api_usage(project=None, usagejson=None, style='new'):
    usage_map = {}

    if style == "old": # giri's sysout style
        if project is not None:
            filename = project + ".usage"
        elif usagejson is not None:
            filename = usagejson
        with open(filename,'r') as f:
            for line in f:
                parts = line.rstrip().split()
                #TODO: add fully qualified names to clusterall otuput, till then remove package names from here
                usage_map[(parts[0].split(".")[-1], parts[1].split(".")[-1])] = int(parts[2])
        return usage_map
    else:
        with open(usagejson,'r') as fp:
            jsonobj = json.load(fp)
            for key in jsonobj:
                entry = jsonobj[key]
                print(entry)
                name = entry['name'].split(".")[-1]
                vals = entry['usedClassesToCount']
                for k in vals.keys():
                    classname = k.split(".")[-1]
                    count = vals[k]
                    usage_map[(name, classname)] = count
        return usage_map

def get_usage_matrix(usage_map, classnames):
    key2idx = {}
    idx2key = {}
    curr_idx = 0
    for key_part in classnames:
        key2idx[key_part] = curr_idx
        idx2key[curr_idx] = key_part
        curr_idx += 1

    usage_matrix = np.zeros((curr_idx, curr_idx))
    for k in usage_map:
        if key2idx.get(k[0]) is None:
            print("Class", k[0], "missing in clusterall.json")
            continue
        if key2idx.get(k[1]) is None:
            print("Class", k[1], "missing in clusterall.json")
            continue
        idx1 = key2idx[k[0]]
        idx2 = key2idx[k[1]]
        usage_matrix[idx1, idx2] = usage_map[k]

    return usage_matrix, key2idx, idx2key
