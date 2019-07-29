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
1. /Users/shreya/git/digdeep src/main/output false kMeans  4 // (k)
2. /Users/shreya/git/digdeep src/main/output true  DBSCAN  0.0003 2  //(epislon neighbours)
3. /Users/shreya/git/digdeep src/main/output false NAIVE onlyMerge
4. /Users/shreya/git/digdeep src/main/output false NAIVETFIDF  euclidiean onlyMerge
 

3rd argument is to set ignoreNone as true or false.  
In Naive and NAIVETFIDF, 
there can be two comibining startergiey- onlyMerge and split

or to compute on pre-computed measure file: 
 src/main/resources/tf_idf-analy.csv algorithm other-options-as-above

