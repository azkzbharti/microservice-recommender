import sys
import os
from recluster import Clustering

if len(sys.argv) < 4:
    print("Usage: <cluster json file> <usage json file> <output file>")
    sys.exit(1)

clustering = Clustering(clusterjson=sys.argv[1], usagejson=sys.argv[2], outputfile=sys.argv[3])

clustering.recluster_cohesion()
