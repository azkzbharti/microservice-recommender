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
	
	public static final String UNUSED_CLUSTER_NAME = "Unused/Entry Points";

	
	public static final String TYPE_SRC = "src"; //files
	public static final String TYPE_BIN = "bin"; //binary
	public static final String JAVA_LANG = "java";
	
	public static final String SOURCE_GIT = "git";
	public static final String SOURCE_FILE = "source"; //i.e local upload
	
	// POM download
    public static final int CONNECTION_TIME_OUT=60*1000;
	public static final int READ_TIME_OUT=60*1000;
	public static final String URL_BASE="https://repo.maven.apache.org/maven2/";
	
	public static final String TRANSACTIONS="transactions";
	
	// Constants used in Analysis Types
	public static final String INTER_CLASS_USAGE="inter_class_usage";
	public static final String JAR_USAGE="jar_api";
	public static final String MEASURE="measure";
	public static final String CLASS_DETAILS="classDetails";
	public static final String MAVEN_ANALYSIS="MavenMeta";
	public static final String BAR_DATA="BarData";
	
	//Constants for clustering types
	public static final String AFFINITY_CLUSTERING="AFFINITY_CLUSTERING";
	public static final String API_CLUSTERING="API_CLUSTERING";
	public static final String COMMUNITY_CLUSTERING="COMMUNITY_CLUSTERING";
	
	//Keys for clustering
	public static final String CLUSTER_COHESION_AFFINITY="cohesion-affinity";
	public static final String CLUSTER_COHESION_API="cohesion-clustering";
	public static final String CLUSTER_API="clusterAPI";
	public static final String CLUSTER_AFFINITY="cluster-affinity";
	public static final String CLUSTER_COMMUNITY="microservice";
	public static final String COMMUNITY_CLUSTER_JSON="graph_clustering";
	public static final String SOURCE_USER="user";
	public static final String SOURCE_ALGO="algo";
	
//	public static final String COMMUNITY_CLUSTER_JSON="graph_clustering";
//	clusterType
	
	public static final String MSR_PROPERTIES = "msr.properties";
	public static final String STOP_WORDS_FILE = "stop_words.txt";
	public static final String SEEDS_FILE = "seeds.txt";
	
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