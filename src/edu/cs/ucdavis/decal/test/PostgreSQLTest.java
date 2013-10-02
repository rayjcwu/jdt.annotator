package edu.cs.ucdavis.decal.test;

import org.junit.Test;
import org.junit.Assert;

import edu.cs.ucdavis.decal.PostgreSQLStorer;

public class PostgreSQLTest {
	@Test
	public void testCreateTable() {
		PostgreSQLStorer db = new PostgreSQLStorer("jdbc:postgresql://127.0.0.1:5432/annotation");
		db.init();
	}
	
	
	@Test
	public void testRetrieveFileId() {
		PostgreSQLStorer db = new PostgreSQLStorer("jdbc:postgresql://127.0.0.1:5432/annotation");
		db.init();
		
		String filename = "/Users/jcwu/test.c";
		int i = db.retrieveIdFrom("file", "name", filename);
		Assert.assertTrue("create new one", i >= 0);
		int j = db.retrieveIdFrom("file", "name", filename);
		Assert.assertTrue("create new one", i == j);
	}
}
