package com.ibm.research.msr.api;

import static org.junit.Assert.assertEquals;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;
import static org.powermock.api.mockito.PowerMockito.whenNew;

import java.util.Arrays;

import org.bson.types.ObjectId;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.ibm.research.msr.db.DatabaseConnection;
import com.ibm.research.msr.db.queries.base.DeleteManyQuery;
import com.ibm.research.msr.db.queries.notes.InsertIntoNotesQuery;
import com.ibm.research.msr.db.queries.notes.SelectAllNotesForProjectId;
import com.ibm.research.msr.db.queries.notes.UpdateNotesByNotesId;
import com.ibm.research.msr.model.Notes;
import com.ibm.research.msr.utils.Constants.ProjectStatus;
import com.mongodb.client.MongoDatabase;

@RunWith(PowerMockRunner.class)
@PrepareForTest({NotesService.class, DatabaseConnection.class})
public class NotesServiceTest {
	
	private static final String PROJECT_ID = new ObjectId().toHexString();
	
	NotesService notesService = new NotesService();

	@Test
	public void notesForProjectIdTest() throws Exception {
		mockStatic(DatabaseConnection.class);
		MongoDatabase mongoDatabase = mock(MongoDatabase.class);
		when(DatabaseConnection.getDatabase()).thenReturn(mongoDatabase);
		
		DeleteManyQuery deleteManyQuery = mock(DeleteManyQuery.class);
		whenNew(DeleteManyQuery.class).withAnyArguments().thenReturn(deleteManyQuery);
		
		JSONObject jsonObject = notesService.notesForProjectId(PROJECT_ID);
		
		assertEquals(ProjectStatus.OK, jsonObject.get("status"));
	}
	
	@Test
	public void notesTest() throws Exception {
		mockStatic(DatabaseConnection.class);
		MongoDatabase mongoDatabase = mock(MongoDatabase.class);
		when(DatabaseConnection.getDatabase()).thenReturn(mongoDatabase);
		
		DeleteManyQuery deleteManyQuery = mock(DeleteManyQuery.class);
		whenNew(DeleteManyQuery.class).withAnyArguments().thenReturn(deleteManyQuery);
		
		JSONObject jsonObject = notesService.notes(PROJECT_ID);
		
		assertEquals(ProjectStatus.OK, jsonObject.get("status"));
	}
	
	@Test
	public void getNotesForProjIdTest() throws Exception {
		mockStatic(DatabaseConnection.class);
		MongoDatabase mongoDatabase = mock(MongoDatabase.class);
		when(DatabaseConnection.getDatabase()).thenReturn(mongoDatabase);
		
		SelectAllNotesForProjectId selectAllNotesForProjectId = mock(SelectAllNotesForProjectId.class);
		whenNew(SelectAllNotesForProjectId.class).withAnyArguments().thenReturn(selectAllNotesForProjectId);
		
		when(selectAllNotesForProjectId.getResultSize()).thenReturn(1);
		
		Notes notes = new Notes();
		notes.setNotesId("123456");
		
		when(selectAllNotesForProjectId.getResult()).thenReturn(Arrays.asList(notes));
		
		JSONArray jsonArray = notesService.getNotesForProjId(PROJECT_ID);
		
		JSONObject jsonObject = (JSONObject) jsonArray.get(0);
		
		assertEquals("123456", jsonObject.get("notesId"));
	}
	
	@Test
	public void insertNotesTest() throws Exception {
		mockStatic(DatabaseConnection.class);
		MongoDatabase mongoDatabase = mock(MongoDatabase.class);
		when(DatabaseConnection.getDatabase()).thenReturn(mongoDatabase);
		
		InsertIntoNotesQuery insertIntoNotesQuery = mock(InsertIntoNotesQuery.class);
		whenNew(InsertIntoNotesQuery.class).withAnyArguments().thenReturn(insertIntoNotesQuery);
		
		Notes notes = new Notes();
		
		JSONObject jsonObject = notesService.notes(notes);
		
		assertEquals(ProjectStatus.OK, jsonObject.get("status"));
	}
	
	@Test
	public void updateNotesTest() throws Exception {
		mockStatic(DatabaseConnection.class);
		MongoDatabase mongoDatabase = mock(MongoDatabase.class);
		when(DatabaseConnection.getDatabase()).thenReturn(mongoDatabase);
		
		UpdateNotesByNotesId updateNotesByNotesId = mock(UpdateNotesByNotesId.class);
		whenNew(UpdateNotesByNotesId.class).withAnyArguments().thenReturn(updateNotesByNotesId);
		
		Notes notes = new Notes();
		
		JSONObject jsonObject = notesService.updateNotes(notes);
		
		assertEquals(ProjectStatus.OK, jsonObject.get("status"));
	}

}
