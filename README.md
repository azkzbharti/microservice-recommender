# microservice-recommender
Algorithms supported: 
1. kMeans
2. DBSCAN
3. NAIVETFIDF - distance: cosine, euclidiean
4. NAIVE 


 
Run time argumnets: 
Run all alorithms together: 
/Users/shreya/git/digdeep  src/main/output all

"path to app or measurefile" algorithm  IgnoreNoneCategory(true/false)
for eg
1. /Users/shreya/git/digdeep src/main/output  kMeans false 4 // (k)
2. /Users/shreya/git/digdeep src/main/output  DBSCAN true 0.0003 2  //(epislon neighbours)
3. /Users/shreya/git/digdeep src/main/output NAIVE false
4. /Users/shreya/git/digdeep src/main/output  NAIVETFIDF false euclidiean  // or cosine 

or to compute on pre-computed measure file: 
 src/main/resources/tf_idf-analy.csv algorithm other-options-as-above

