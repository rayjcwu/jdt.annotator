package edu.cs.ucdavis.decal.test;

import org.junit.Test;

import edu.cs.ucdavis.decal.PostgreSQLStorer;

public class PostgreSQLTest {
	@Test
	public void testCreateTable() {
		PostgreSQLStorer db = new PostgreSQLStorer("jdbc:postgresql://127.0.0.1:5432/annotation");
		db.init();
	}
}
