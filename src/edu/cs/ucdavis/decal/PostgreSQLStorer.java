package edu.cs.ucdavis.decal;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.jdt.core.dom.ASTNode;

public class PostgreSQLStorer implements IDatabaseStorer {
	private boolean ready;
	private Connection conn;
	private String url;
	private Logger logger;

	private OmniController controller;

	public void register(OmniController controller) {
		this.controller = controller;
		controller.setDatabase(this);
	}

	public PostgreSQLStorer(String url) {
		this.ready = false;
		this.url = url;
		this.logger = Logger.getLogger("annotation");
	}

	@Override
	public void init() {
		connect();
		createTableIfNotExist();
		createViewIfNotExist();
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
		Statement stmt = null;
		try {
			stmt = conn.createStatement();

			String createNodetypeTable = "CREATE TABLE IF NOT EXISTS nodetype (id int, name text); "
					                  + "CREATE OR REPLACE RULE ignore_duplicate AS ON INSERT TO nodetype "
					                  + "WHERE (EXISTS (SELECT id FROM nodetype WHERE nodetype.id = NEW.id)) "
					                  + "DO INSTEAD NOTHING; ";

			String createFileTable = "CREATE TABLE IF NOT EXISTS file (id serial, name text, project_id int); ";

			String createProjectTable = "CREATE TABLE IF NOT EXISTS project (id serial, name text, path text); ";

			String createASTNodeTable = "CREATE TABLE IF NOT EXISTS astnode (id serial, "
									  + "start_pos int, "
									  + "length int, "  	  // end_pos = start_pos + length
									  + "line_number int, "   // #-th line in file
								      + "nodetype_id int, "   // foreign key
								      + "file_id int, "		  // foreign key
								      + "binding_key text, "  // only some simple name nodes will have this
								      + "string text, "	      // string representation
								      + "declared_at_astnode_id int); "  // binding information, will fill this in second round
								      ;
			//start_pos, length, line_number, nodetype_id, binding_key, string, file_id

			stmt.executeUpdate(createNodetypeTable);
			stmt.executeUpdate(createFileTable);
			stmt.executeUpdate(createProjectTable);
			stmt.executeUpdate(createASTNodeTable);

			// insert nodetype value
			// TODO: this will throw IllegalAccessException, don't know why
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

	private Collection<String> collectViewNames() {
		List <String> views = new ArrayList<String>();
		try {
			DatabaseMetaData meta = conn.getMetaData();
			ResultSet res = meta.getTables(null, null, null, new String[] {"VIEW"});
			while (res.next()) {
				views.add(res.getString("TABLE_NAME"));
			  }
		} catch (SQLException e) {
			Logger logger = Logger.getLogger("annotation");
			logger.log(Level.SEVERE, "Collect view names exception");
		}
		return views;
	}

	@Override
	public void createViewIfNotExist() {
		Statement stmt = null;
		Collection <String> views = collectViewNames();
		String to_create = "";
		try {
			stmt = conn.createStatement();
			/// create view
			to_create = "project_file";
			if (!views.contains(to_create)) {
				String project_file = "CREATE VIEW project_file AS "
					+ "SELECT project.id AS project_id, "
					+ "project.name AS project_name, "
					+ "project.path as project_path, "
					+ "file.id AS file_id, "
					+ "file.name AS file_name "

					+ "FROM project, file "

					+ "WHERE file.project_id = project.id;";
				stmt.executeUpdate(project_file);
			}

			/* (id serial, "
									  + "start_pos int, "
									  + "length int, "  	  // end_pos = start_pos + length
									  + "line_number int, "   // #-th line in file
								      + "nodetype_id int, "   // foreign key
								      + "file_id int, "		  // foreign key
								      + "binding_key text, "  // only some simple name nodes will have this
								      + "string text, "	      // string representation
								      + "declared_at_astnode_id int); "  /
								      */
			to_create = "astnode_type";
			if (!views.contains(to_create)) {
				String astnode_type = "CREATE VIEW astnode_type AS "
					+ "SELECT "
					+ "astnode.id AS astnode_id, "
					+ "start_pos, "
					+ "length, "
					+ "line_number, "
					+ "nodetype_id, "
					+ "nodetype.name AS nodetype, "
					+ "file_id, "
					+ "binding_key, "
					+ "string, "
					+ "declared_at_astnode_id "

					+ "FROM astnode, nodetype "
					+ "WHERE astnode.nodetype_id = nodetype.id;"
					;
				stmt.executeUpdate(astnode_type);
			}
		} catch (SQLException e) {
			logger.log(Level.SEVERE, "Create views "+ to_create + " exception");
		} finally {
			if (stmt != null) {
				try {
					stmt.close();
				} catch (SQLException e) {
					;
				}
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
		Statement stmt = null;
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
	public int retrieveProjectId(String projectName, String sourcePath) {
		initIfNot();
		PreparedStatement stmt = null;
		ResultSet result = null;
		String query = "SELECT id FROM project WHERE name=? AND path=?;";
		try {
			stmt = conn.prepareStatement(query);
			stmt.setString(1, projectName);
			stmt.setString(2, sourcePath);
			result = stmt.executeQuery();
			if(result.next()) {
				return result.getInt("id");
			} else {
				PreparedStatement ins_stmt = conn.prepareStatement("INSERT INTO project (name, path) VALUES (?, ?);");
				ins_stmt.setString(1, projectName);
				ins_stmt.setString(2, sourcePath);
				ins_stmt.executeUpdate();
				result = stmt.executeQuery();
				if (result.next()) {
					return result.getInt("id");
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
	public void saveAstNodeInfo(int start_pos, int length, int line_number, int nodetype_id, String binding_key, String string, int file_id) {
		PreparedStatement pstmt = null;
		try {
			pstmt = conn.prepareStatement("INSERT INTO astnode (start_pos, length, line_number, nodetype_id, binding_key, string, file_id) "
					+ "VALUES (?, ?, ?, ?, ?, ?, ?)");
			pstmt.setInt(1, start_pos);
			pstmt.setInt(2, length);
			pstmt.setInt(3, line_number);
			pstmt.setInt(4, nodetype_id);
			pstmt.setString(5, binding_key);
			pstmt.setString(6, string);
			pstmt.setInt(7, file_id);
			pstmt.executeUpdate();
		} catch (SQLException e) {
			logger.log(Level.SEVERE, String.format("Save ASTNode=%s", string));
		} finally {
			try {
				if (pstmt != null) {
					pstmt.close();
				}
			} catch (SQLException e) {
				logger.log(Level.SEVERE, "Close statement exception");
			}
		}
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
