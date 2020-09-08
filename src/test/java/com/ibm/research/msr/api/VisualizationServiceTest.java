package com.ibm.research.msr.api;

import static org.junit.Assert.assertEquals;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;
import static org.powermock.api.mockito.PowerMockito.whenNew;

import java.util.Arrays;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.json.simple.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.ibm.research.msr.db.DatabaseConnection;
import com.ibm.research.msr.db.dto.Analysis;
import com.ibm.research.msr.db.dto.Partition;
import com.ibm.research.msr.db.queries.analysis.SelectAnalysisByProjectIdAndType;
import com.ibm.research.msr.db.queries.partition.SelectAllPartitionsByProjectId;
import com.ibm.research.msr.db.queries.project.SelectProjectByProjectId;
import com.ibm.research.msr.utils.Constants;
import com.mongodb.client.MongoDatabase;

@RunWith(PowerMockRunner.class)
@PrepareForTest({VisualizationService.class, DatabaseConnection.class})
public class VisualizationServiceTest {
	
	private static final String PROJECT_ID = new ObjectId().toHexString();
	
	VisualizationService visualizationService = new VisualizationService();
	
	@Test
	public void getPartitionsApiClusterTest() throws Exception {
		mockStatic(DatabaseConnection.class);
		MongoDatabase mongoDatabase = mock(MongoDatabase.class);
		when(DatabaseConnection.getDatabase()).thenReturn(mongoDatabase);
		
		SelectAllPartitionsByProjectId sp = mock(SelectAllPartitionsByProjectId.class);
		whenNew(SelectAllPartitionsByProjectId.class).withAnyArguments().thenReturn(sp);
		
		SelectAnalysisByProjectIdAndType sp2 = mock(SelectAnalysisByProjectIdAndType.class);
		whenNew(SelectAnalysisByProjectIdAndType.class).withAnyArguments().thenReturn(sp2);
		
		when(sp.getResultSize()).thenReturn(1);
		
		Document document = mock(Document.class);
		when(document.get(Mockito.any())).thenReturn("cluster");
		
		Partition partition = mock(Partition.class);
		when(partition.getPartitionResult()).thenReturn(document);
		when(partition.getPartitionType()).thenReturn(Constants.API_CLUSTERING);
		
		when(sp.getResult()).thenReturn(Arrays.asList(partition));
		
		JSONObject jsonObject = visualizationService.getPartitions(PROJECT_ID);
		
		JSONObject object = (JSONObject) jsonObject.get("Results");
		
		assertEquals("cluster", object.get(Constants.CLUSTER_API));
	}
	
	@Test
	public void getPartitionsAffinityClusterTest() throws Exception {
		mockStatic(DatabaseConnection.class);
		MongoDatabase mongoDatabase = mock(MongoDatabase.class);
		when(DatabaseConnection.getDatabase()).thenReturn(mongoDatabase);
		
		SelectAllPartitionsByProjectId sp = mock(SelectAllPartitionsByProjectId.class);
		whenNew(SelectAllPartitionsByProjectId.class).withAnyArguments().thenReturn(sp);
		
		SelectAnalysisByProjectIdAndType sp2 = mock(SelectAnalysisByProjectIdAndType.class);
		whenNew(SelectAnalysisByProjectIdAndType.class).withAnyArguments().thenReturn(sp2);
		
		when(sp.getResultSize()).thenReturn(1);
		
		Document document = mock(Document.class);
		when(document.get(Mockito.any())).thenReturn("cluster");
		
		Partition partition = mock(Partition.class);
		when(partition.getPartitionResult()).thenReturn(document);
		when(partition.getPartitionType()).thenReturn(Constants.AFFINITY_CLUSTERING);
		
		when(sp.getResult()).thenReturn(Arrays.asList(partition));
		
		JSONObject jsonObject = visualizationService.getPartitions(PROJECT_ID);
		
		JSONObject object = (JSONObject) jsonObject.get("Results");
		
		assertEquals("cluster", object.get(Constants.CLUSTER_AFFINITY));
	}
	
	@Test
	public void getPartitionsCommunityClusterTest() throws Exception {
		mockStatic(DatabaseConnection.class);
		MongoDatabase mongoDatabase = mock(MongoDatabase.class);
		when(DatabaseConnection.getDatabase()).thenReturn(mongoDatabase);
		
		SelectAllPartitionsByProjectId sp = mock(SelectAllPartitionsByProjectId.class);
		whenNew(SelectAllPartitionsByProjectId.class).withAnyArguments().thenReturn(sp);
		
		SelectAnalysisByProjectIdAndType sp2 = mock(SelectAnalysisByProjectIdAndType.class);
		whenNew(SelectAnalysisByProjectIdAndType.class).withAnyArguments().thenReturn(sp2);
		
		when(sp.getResultSize()).thenReturn(1);
		
		Document document = mock(Document.class);
		when(document.get(Mockito.any())).thenReturn("cluster");
		
		Partition partition = mock(Partition.class);
		when(partition.getPartitionResult()).thenReturn(document);
		when(partition.getPartitionType()).thenReturn(Constants.COMMUNITY_CLUSTERING);
		
		when(sp.getResult()).thenReturn(Arrays.asList(partition));
		
		JSONObject jsonObject = visualizationService.getPartitions(PROJECT_ID);
		
		JSONObject object = (JSONObject) jsonObject.get("Results");
		
		assertEquals("cluster", object.get(Constants.CLUSTER_COMMUNITY));
	}
	
	@Test
	public void getClassDetailsTest() throws Exception {
		mockStatic(DatabaseConnection.class);
		MongoDatabase mongoDatabase = mock(MongoDatabase.class);
		when(DatabaseConnection.getDatabase()).thenReturn(mongoDatabase);
		
		SelectProjectByProjectId sp = mock(SelectProjectByProjectId.class);
		whenNew(SelectProjectByProjectId.class).withAnyArguments().thenReturn(sp);
		
		SelectAnalysisByProjectIdAndType sp2 = mock(SelectAnalysisByProjectIdAndType.class);
		whenNew(SelectAnalysisByProjectIdAndType.class).withAnyArguments().thenReturn(sp2);
		
		Analysis analysis = mock(Analysis.class);
		when(analysis.getAnalysisResult()).thenReturn("Analyzed");
		
		when(sp2.getResult()).thenReturn(analysis);
		
		JSONObject jsonObject = visualizationService.getClassDetails(PROJECT_ID);
		
		assertEquals("Analyzed", jsonObject.get("Result"));
		
		
		
	}

}
