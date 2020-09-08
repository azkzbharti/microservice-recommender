package com.ibm.research.msr.api;

import static org.junit.Assert.*;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;
import static org.powermock.api.mockito.PowerMockito.whenNew;

import org.bson.types.ObjectId;
import org.json.simple.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.ibm.research.msr.db.DatabaseConnection;
import com.ibm.research.msr.db.queries.base.DeleteManyQuery;
import com.ibm.research.msr.utils.Constants.ProjectStatus;
import com.mongodb.client.MongoDatabase;

@RunWith(PowerMockRunner.class)
@PrepareForTest({DeleteService.class, DatabaseConnection.class})
public class DeleteServiceTest {

	@Test
	public void deleteProjsTest() throws Exception {
		mockStatic(DatabaseConnection.class);
		MongoDatabase mongoDatabase = mock(MongoDatabase.class);
		when(DatabaseConnection.getDatabase()).thenReturn(mongoDatabase);
		
		DeleteManyQuery deleteManyQuery = mock(DeleteManyQuery.class);
		whenNew(DeleteManyQuery.class).withAnyArguments().thenReturn(deleteManyQuery);
		
		DeleteService deleteService = new DeleteService();
		JSONObject jsonObject = deleteService.deleteProjs(new ObjectId().toHexString());
		
		assertEquals(ProjectStatus.OK, jsonObject.get("status"));
	}

}
