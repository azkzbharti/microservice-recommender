package com.ibm.research.msr.db;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest({DatabaseConnection.class})
public class DatabaseConnectionTest {

	@Test
	public void databaseConnectionWithPassword() {
		System.setProperty("DB_PASSWORD", "pass");
		System.setProperty("DB_USER", "user");
		System.setProperty("DB_HOST", "host");
		System.setProperty("DB_PORT", "8080");
		System.setProperty("DB_NAME", "db_name");
		new DatabaseConnection();
		Assert.assertNotNull(DatabaseConnection.getDatabase());
	}
	
	@Test
	public void databaseConnectionWithoutPassword() {
		System.setProperty("DB_USER", "user");
		System.setProperty("DB_HOST", "host");
		System.setProperty("DB_PORT", "8080");
		System.setProperty("DB_NAME", "db_name");
		new DatabaseConnection();
		Assert.assertNotNull(DatabaseConnection.getDatabase());
	}

}
