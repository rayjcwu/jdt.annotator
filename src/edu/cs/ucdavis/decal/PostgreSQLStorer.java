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
	private ResultSet rs;
	
	private OmniController controller;
	
	public void register(OmniController controller) {
		this.controller = controller;
		controller.setDatabase(this);
	}
	
	public PostgreSQLStorer(String url) {
		this.ready = false;
		this.url = url;
		this.logger = controller.getLogger();		
		this.stmt = null;
		this.rs = null;
	}
	
	public void init() {
		connect();
		createTableIfNotExist();
		ready = true;
	}
	
	public void connect() {
		try {
			Class.forName("org.postgresql.Driver"); 
			conn = DriverManager.getConnection(url);
			if(conn != null && !conn.isClosed()) {
				logger.log(Level.INFO, "database connected");
			}		
		} catch (ClassNotFoundException e) {
			logger.log(Level.SEVERE, e.getMessage(), e);
		} catch (SQLException e) {
			logger.log(Level.SEVERE, e.getMessage(), e);
		}
	}
	
	public void createTableIfNotExist() {
		try {
			stmt = conn.createStatement();
			
			String createNodetypeTable = "CREATE TABLE IF NOT EXISTS nodetype (id int, name text); "  // add ignore duplicate insertion
					                  + "CREATE OR REPLACE RULE ignore_duplicate AS ON INSERT TO nodetype "
					                  + "WHERE (EXISTS (SELECT id FROM nodetype WHERE nodetype.id = NEW.id)) "
					                  + "DO INSTEAD NOTHING; ";
			String createFileTable = "CREATE TABLE IF NOT EXISTS file (id serial, name text); ";
			String createProjectTable = "CREATE TABLE IF NOT EXISTS project (id serial, name text); ";
			String createASTNodeTable = "CREATE TABLE IF NOT EXISTS astnode (id serial, start_pos int, length int, "
								      + "nodetype_id int, file_id int, project_id int, binding_key text); ";
			
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
	
	public void close() {
		try {
			this.conn.close();
		} catch (SQLException e) {
			logger.log(Level.SEVERE, "Cannot close");
		} finally {
			ready = false;
		}
	}
	
	public boolean isReady() {
		return ready;
	}
}
