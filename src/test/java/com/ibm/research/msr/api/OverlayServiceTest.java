package com.ibm.research.msr.api;

import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;
import static org.powermock.api.mockito.PowerMockito.whenNew;

import java.util.Arrays;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.json.simple.JSONObject;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.ibm.research.msr.db.DatabaseConnection;
import com.ibm.research.msr.db.dto.Overlays;
import com.ibm.research.msr.db.queries.overlays.SelectAllOverlaysByProjectId;
import com.mongodb.client.MongoDatabase;


@RunWith(PowerMockRunner.class)
@PrepareForTest({OverlayService.class, DatabaseConnection.class})
public class OverlayServiceTest {
	
	private static final String PROJECT_ID = new ObjectId().toHexString();
	
	OverlayService overlayService = new OverlayService();
	
	@Test
	public void getMicroservicesTest() throws Exception {
		mockStatic(DatabaseConnection.class);
		MongoDatabase mongoDatabase = mock(MongoDatabase.class);
		when(DatabaseConnection.getDatabase()).thenReturn(mongoDatabase);
		
		SelectAllOverlaysByProjectId selectAllOverlaysByProjectId = mock(SelectAllOverlaysByProjectId.class);
		whenNew(SelectAllOverlaysByProjectId.class).withAnyArguments().thenReturn(selectAllOverlaysByProjectId);
		
		Overlays overlays = mock(Overlays.class);
		Document document = mock(Document.class);
		
		when(overlays.getTransactionResult()).thenReturn(document);
		when(document.get(Mockito.any())).thenReturn("transaction");
		
		when(selectAllOverlaysByProjectId.getResultSize()).thenReturn(1);
		when(selectAllOverlaysByProjectId.getResult()).thenReturn(Arrays.asList(overlays));
		
		JSONObject jsonObject = overlayService.getMicroservices(PROJECT_ID);
		
		Assert.assertEquals("transaction", jsonObject.get("Result"));
	}
	
	@Test
	public void getMicroservicesNoTransactionTest() throws Exception {
		mockStatic(DatabaseConnection.class);
		MongoDatabase mongoDatabase = mock(MongoDatabase.class);
		when(DatabaseConnection.getDatabase()).thenReturn(mongoDatabase);
		
		SelectAllOverlaysByProjectId selectAllOverlaysByProjectId = mock(SelectAllOverlaysByProjectId.class);
		whenNew(SelectAllOverlaysByProjectId.class).withAnyArguments().thenReturn(selectAllOverlaysByProjectId);
		
		Overlays overlays = mock(Overlays.class);
		Document document = mock(Document.class);
		
		when(overlays.getTransactionResult()).thenReturn(document);
		when(document.get(Mockito.any())).thenReturn("transaction");
		
		when(selectAllOverlaysByProjectId.getResultSize()).thenReturn(0);
		when(selectAllOverlaysByProjectId.getResult()).thenReturn(Arrays.asList(overlays));
		
		JSONObject jsonObject = overlayService.getMicroservices(PROJECT_ID);
		
		Assert.assertEquals("No traansaction Data Available.", jsonObject.get("status"));
	}
	

}
