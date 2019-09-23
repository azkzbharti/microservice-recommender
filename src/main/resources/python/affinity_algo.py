import sys
import argparse
import numpy as np
import sklearn.cluster
import distance

# words = "BackOrder Help1 Catalog2 Account1 OrderInfo Order Util BackOrder2 ValidatorUtils Account3 Admin3 OrderItem Image3 ValidatePassword ShoppingCart1 Shopping1 LogInfo Suppliers1 PopulateDB1 ShoppingItem Customer2 Customer ResetDB1 MailAction Mailer1 Inventory Supplier".split(
#     " ")  # Replace this line
def process_data(words, file):
    words = np.asarray(words)  # So that indexing with a list will work
    lev_similarity = -1*np.array([[distance.levenshtein(w1, w2)
                                for w1 in words] for w2 in words])

    affprop = sklearn.cluster.AffinityPropagation(affinity="precomputed", damping=0.5)
    affprop.fit(lev_similarity)
    outF = open(file, "w")
    for cluster_id in np.unique(affprop.labels_):
        exemplar = words[affprop.cluster_centers_indices_[cluster_id]]
        cluster = np.unique(words[np.nonzero(affprop.labels_ == cluster_id)])
        cluster_str = ",".join(cluster)
        print(" - *%s:* %s" % (exemplar, cluster_str))
        outF.write("%s=%s" % (exemplar, cluster_str))
        outF.write("\n")

    # write line to output file
    
   
    outF.close()


# For unit testing
if __name__ == "__main__":
    parser = argparse.ArgumentParser()
    parser.add_argument('--inputArray', nargs='+', help="list of names")
    parser.add_argument("--outPutFilePath", help="path of output file",type=str)
    args = parser.parse_args()

    try: 
      process_data(args.inputArray ,args.outPutFilePath)
    except Exception as error:
        print("ERR: "+repr(error))  
        sys.exit(-1)