# microservice-recommender
Algorithms supported: 
1. kMeans
2. DBSCAN
3. NAIVETFIDF - distance: cosine, euclidiean
4. NAIVE 


 
Run time argumnets: 
"path to app/ measurefile" algorithm 
for eg
1. /Users/shreya/git/digdeep kMeans 4 // (k)
2. /Users/shreya/git/digdeep DBSCAN 0.0003 2  //(epislon neighbours)
3. /Users/shreya/git/digdeep NAIVE
4. /Users/shreya/git/digdeep NAIVETFIDF euclidiean  // or cosine 

or to compute on pre-computed measure file: 
 src/main/resources/tf_idf-analy.csv algorithm othersoptions

