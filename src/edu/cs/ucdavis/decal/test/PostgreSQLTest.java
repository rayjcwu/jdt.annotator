package edu.cs.ucdavis.decal.test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.junit.Assert;
import org.junit.Test;

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
		String sourcePath = "/Users/jcwu/";
		String sourcePath2 = "/Users/jcwu/src";
		
		int p1 = db.retrieveProjectId(projectName, sourcePath);
		Assert.assertTrue("create new project id", p1 >= 0);
		int p2 = db.retrieveProjectId(projectName, sourcePath);
		Assert.assertTrue("same project id", p1 == p2);
		int p3 = db.retrieveProjectId(projectName, sourcePath2);
		Assert.assertTrue("different project id", p3 != p2);		
		
		String filename = "/Users/jcwu/test.c";
		int f1 = db.retrieveFileId(filename, p2);
		Assert.assertTrue("create new file id", f1 >= 0);
		int f2 = db.retrieveFileId(filename, p2);
		Assert.assertTrue("same file id", f1 == f2);
		int f3 = db.retrieveFileId(filename, p2+1);
		Assert.assertTrue("create another new file id", f3 != f2);
	}
	
	@Test
	public void testPreparedStatement() {
		Logger logger = Logger.getLogger("test");
		PreparedStatement pre_stmt = null;
		Statement stmt = null;
		try {
			Class.forName("org.postgresql.Driver"); 
			Connection conn = DriverManager.getConnection("jdbc:postgresql://127.0.0.1:5432/annotation");
			if (conn == null || (conn != null && conn.isClosed())) {
				logger.log(Level.SEVERE, "database is not connected");
			}
			String sql = "INSERT INTO h (string) VALUES (?);";
			pre_stmt = conn.prepareStatement(sql);
			pre_stmt.setString(1, "'str' ' ' i' \"ng'");
			pre_stmt.executeUpdate();
			
			////
			String query = "SELECT * FROM h;";
			stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(query);
			while (rs.next()) {
				System.out.println(rs.getString("string"));
			}			
		} catch (ClassNotFoundException e) {
			logger.log(Level.SEVERE, e.getMessage(), e);
		} catch (SQLException e) {
			logger.log(Level.SEVERE, e.getMessage(), e);
		} finally {
			if (pre_stmt != null) {
				try {
					pre_stmt.close();
				} catch (SQLException e) {
					
				}
			}
		}
	}
}
