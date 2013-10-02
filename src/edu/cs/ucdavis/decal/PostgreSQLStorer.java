package edu.cs.ucdavis.decal;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.jdt.core.dom.ASTNode;

public class PostgreSQLStorer implements IDatabaseStorer {
	private boolean ready;
	private Connection conn;
	private String url;
	private Logger logger;
	
	private Statement stmt;
	
	private OmniController controller;
	
	public void register(OmniController controller) {
		this.controller = controller;
		controller.setDatabase(this);
	}
	
	public PostgreSQLStorer(String url) {
		this.ready = false;
		this.url = url;
		this.logger = Logger.getLogger("annotation");		
		this.stmt = null;
	}
	
	@Override
	public void init() {
		connect();
		createTableIfNotExist();
		ready = true;
	}
	
	@Override
	public void connect() {
		try {
			Class.forName("org.postgresql.Driver"); 
			conn = DriverManager.getConnection(url);
			if (conn == null || (conn != null && conn.isClosed())) {
				logger.log(Level.SEVERE, "database is not connected");
			}		
		} catch (ClassNotFoundException e) {
			logger.log(Level.SEVERE, e.getMessage(), e);
		} catch (SQLException e) {
			logger.log(Level.SEVERE, e.getMessage(), e);
		}
	}
	
	@Override
	public void createTableIfNotExist() {
		try {
			stmt = conn.createStatement();
			
			String createNodetypeTable = "CREATE TABLE IF NOT EXISTS nodetype (id int, name text); "  // add ignore duplicate insertion
					                  + "CREATE OR REPLACE RULE ignore_duplicate AS ON INSERT TO nodetype "
					                  + "WHERE (EXISTS (SELECT id FROM nodetype WHERE nodetype.id = NEW.id)) "
					                  + "DO INSTEAD NOTHING; ";
			String createFileTable = "CREATE TABLE IF NOT EXISTS file (id serial, name text, project_id int); ";
			String createProjectTable = "CREATE TABLE IF NOT EXISTS project (id serial, name text); ";
			String createASTNodeTable = "CREATE TABLE IF NOT EXISTS astnode (id serial, start_pos int, length int, "
								      + "nodetype_id int, file_id int, binding_key text); ";
			
			stmt.executeUpdate(createNodetypeTable);
			stmt.executeUpdate(createFileTable);
			stmt.executeUpdate(createProjectTable);
			stmt.executeUpdate(createASTNodeTable);
			
			for (Field field: ASTNode.class.getDeclaredFields()) {
				if (field.getType().equals(int.class)) {
					stmt.executeUpdate(String.format("INSERT INTO nodetype (id, name) VALUES (%d, '%s');", field.getInt(null), field.getName()));
				}
			}			
		} catch (SQLException e) {
			logger.log(Level.SEVERE, "Create table exception");
		} catch (IllegalAccessException e) {
			;
		} finally {
			try {
				if (stmt != null) {
					stmt.close();
				}
			} catch (SQLException e) {
				logger.log(Level.SEVERE, "Close statement exception");
			}
		}
	}
	
	@Override
	public void close() {
		try {
			this.conn.close();
		} catch (SQLException e) {
			logger.log(Level.SEVERE, "Cannot close");
		} finally {
			ready = false;
		}
	}
	
	@Override
	public int retrieveFileId(String fileName, int projectId) {
		initIfNot();
		
		try {
			stmt = conn.createStatement();
			String query = String.format("SELECT id FROM file WHERE name='%s' AND project_id = %d;", fileName, projectId);
			ResultSet result = stmt.executeQuery(query);
			if(result.next()) {
				return result.getInt("id");
			} else {
				stmt.executeUpdate(String.format("INSERT INTO file (name, project_id) VALUES ('%s', %s);", fileName, projectId));
				ResultSet result2 = stmt.executeQuery(query);
				if (result2.next()) {
					return result2.getInt("id");
				}
			}
		} catch (SQLException e) {
			logger.log(Level.SEVERE, String.format("Retrieve file=%s id", fileName));
		} finally {
			try {
				if (stmt != null) {
					stmt.close();
				}
			} catch (SQLException e) {
				logger.log(Level.SEVERE, "Close statement exception");
			}
		}		
		return -1; // should not happen
	}
	
	@Override
	public int retrieveProjectId(String projectName) {
		initIfNot();
		
		try {
			stmt = conn.createStatement();
			String query = String.format("SELECT id FROM project WHERE name='%s';", projectName);
			ResultSet result = stmt.executeQuery(query);
			if(result.next()) {
				return result.getInt("id");
			} else {
				stmt.executeUpdate(String.format("INSERT INTO project (name) VALUES ('%s');", projectName));
				ResultSet result2 = stmt.executeQuery(query);
				if (result2.next()) {
					return result2.getInt("id");
				}
			}
		} catch (SQLException e) {
			logger.log(Level.SEVERE, String.format("Retrieve project=%s id", projectName));
		} finally {
			try {
				if (stmt != null) {
					stmt.close();
				}
			} catch (SQLException e) {
				logger.log(Level.SEVERE, "Close statement exception");
			}
		}		
		return -1; // should not happen
	}
		
	@Override
	public boolean isReady() {
		return ready;
	}
	
	private void initIfNot() {
		if (!isReady()) {
			init();
		}
	}
}
