package com.ibm.research.msr.db;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ibm.research.msr.utils.Util;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoDatabase;

public class DatabaseConnection {
	
	private static MongoClient database_client = null;
	private static MongoDatabase database = null;
	private Logger logger;

	private String DBNAME = null;

	public DatabaseConnection() {
		this.logger = LoggerFactory.getLogger("log_" + DatabaseConnection.class + "_" + new Date().toString());

		String dburl = null;
		
		DBNAME = Util.getProperty("DB_NAME");

		if( Util.getProperty("DB_PASSWORD") != null && !Util.getProperty("DB_PASSWORD").equalsIgnoreCase("") ) {
			dburl = "mongodb://" + Util.getProperty("DB_USER") + ":" + Util.getProperty("DB_PASSWORD") + "@"
				+ Util.getProperty("DB_HOST") + ":" + Util.getProperty("DB_PORT") + "/" + DBNAME + "?authSource=admin";
		}else {
			dburl = "mongodb://" + Util.getProperty("DB_HOST") + ":" + Util.getProperty("DB_PORT") + "/" + DBNAME + "?authSource=admin";
		}
		logger.info("DB URL " + dburl);
		

		if (database_client == null) {
			try {
				// MongoClientOptions settings =
				// MongoClientOptions.builder().codecRegistry(com.mongodb.MongoClient.getDefaultCodecRegistry()).build();
				database_client = new MongoClient(new MongoClientURI(dburl));
				database = database_client.getDatabase(DBNAME);
				if (database_client != null)
					logger.info("Database connection established.");
			} catch (Exception e) {
				logger.error("Unable to establish database connection.");
				e.printStackTrace();
			}
		}
	}
	

	public static MongoDatabase getDatabase() {
//		database = database_client.getDatabase(DBNAME);
		if (database == null) {
			DatabaseConnection dbconn = new DatabaseConnection();
		}
		return database;
	}

	public void close() {
		try {
			database_client.close();
		} catch (Exception e) {
			logger.error("Unable to close database connection.");
			e.printStackTrace();
		}
	}

	public Logger getLogger() {
		return this.logger;
	}
}