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
	public void testRetrieveProjectFileId() {
		PostgreSQLStorer db = new PostgreSQLStorer("jdbc:postgresql://127.0.0.1:5432/annotation");
		db.init();
		
		String projectName = "fake_project";
		int p1 = db.retrieveProjectId(projectName);
		Assert.assertTrue("create new project id", p1 >= 0);
		int p2 = db.retrieveProjectId(projectName);
		Assert.assertTrue("same project id", p1 == p2);
		
		
		String filename = "/Users/jcwu/test.c";
		int f1 = db.retrieveFileId(filename, p2);
		Assert.assertTrue("create new file id", f1 >= 0);
		int f2 = db.retrieveFileId(filename, p2);
		Assert.assertTrue("same file id", f1 == f2);
		int f3 = db.retrieveFileId(filename, p2+1);
		Assert.assertTrue("create another new file id", f3 != f2);
	}
	

}
