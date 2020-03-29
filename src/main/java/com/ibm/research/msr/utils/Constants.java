package com.ibm.research.msr.utils;

public class Constants {

	// Constants used in Clustering ALGOs
	public static final String KMEANS = "kmeans";
	public static final String DBSCAN = "dbscan";
	public static final String ALL = "all";
	public static final String NAIVE = "naive";
	public static final String NAIVE_TFIDF = "naivetfidf";
	public static final String SPLIT = "split";
	public static final String COSINE = "cosine";
	public static final String ONLY_MERGE = "onlyMerge";
	public static final String EUCLIDIEAN = "euclidiean";
	
	public static final String unusedClusterName = "Unused/Entry Points";

	
	public static final String TYPE_SRC = "src"; //files
	public static final String TYPE_BIN = "bin"; //binary
	public static final String JAVA_LANG = "java";
	
	public static final String SOURCE_GIT = "git";
	public static final String SOURCE_FILE = "source"; //i.e local upload
	
	// POM download
    public static final int CONNECTION_TIME_OUT=60*1000;
	public static final int READ_TIME_OUT=60*1000;
	public static final String URL_BASE="https://repo.maven.apache.org/maven2/";
	
	
	// Constants used in Analysis Types
	public static final String interclassUsage="InterClassUsage";
	public static final String jarUsage="jar_api";
	public static final String measure="measure";
	public static final String classDetails="classDetails";
	public static final String mavenAnalysis="MavenMeta";
	public static final String barData="BarData";
	
	//Constants for clustering types
	public static final String affinityclustering="AFFINITY_CLUSTERING";
	public static final String apiclustering="API_CLUSTERING";
	public static final String communityclustering="COMMUNITY_CLUSTERING";
	
	
	//Keys for clustering
	public static final String clustercohesionAffinity="cohesion-affinity";
	public static final String clustercohesionAPI="cohesion-clustering";
	public static final String clusterAPI="clusterAPI";
	public static final String clusteraffinity="cluster-affinity";
	public static final String clustercommunity="vis_graph_clustering";
	
	public enum ProjectStatus {

		OK(200, "OK"),
		IN_PROGRESS(102, "Still In Progress"),
		INTERNAL_SERVER_ERROR(500, "Internal Error"),
		SERVICE_UNAVAILABLE(503, "Service Unavailable");

		private final int value;

		private final String reasonPhrase;


		private ProjectStatus(int value, String reasonPhrase) {
			this.value = value;
			this.reasonPhrase = reasonPhrase;
		}

		/**
		 * Return the integer value of this status code.
		 */
		public int value() {
			return this.value;
		}

		/**
		 * Return the reason phrase of this status code.
		 */
		public String getReasonPhrase() {
			return reasonPhrase;
		}
	}
	

}
