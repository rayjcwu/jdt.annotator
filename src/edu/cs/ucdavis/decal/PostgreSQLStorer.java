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
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.antlr.JavaLexer;
import org.eclipse.jdt.core.dom.ASTNode;

public class PostgreSQLStorer {
	private boolean ready;
	private Connection conn;
	private String url;
	private Logger logger;

	private OmniController controller;
	private String username;
	private String password;

	public static int tokenBase = 100;

	public void register(OmniController controller) {
		this.controller = controller;
		controller.setDatabase(this);
	}

	public PostgreSQLStorer(String url) {
		this(url, null, null);
	}

	public PostgreSQLStorer(String url, String username, String password) {
		this.ready = false;
		this.url = url;
		this.username = username;
		this.password = password;
		this.logger = Logger.getLogger("annotation");

		connect();
		createTableIfNotExist();
		createViewIfNotExist();
		this.ready = true;
	}

	public void connect() {
		try {
			Properties props = new Properties();
			if (username != null) {
				props.setProperty("user", username);
			}
			if (password != null) {
				props.setProperty("password", password);
			}

			Class.forName("org.postgresql.Driver");
			conn = DriverManager.getConnection(url, props);
			if (conn == null || (conn != null && conn.isClosed())) {
				logger.log(Level.SEVERE, "database is not connected");
			}
		} catch (ClassNotFoundException e) {
			logger.log(Level.SEVERE, e.getMessage(), e);
			throw new IllegalStateException();
		} catch (SQLException e) {
			logger.log(Level.SEVERE, e.getMessage(), e);
			throw new IllegalStateException();
		}
	}


	public void createTableIfNotExist() {
		Statement stmt = null;
		Collection<String> indexes = collectIndexNames();
		try {
			stmt = conn.createStatement();

			String createNodetypeTable = "CREATE TABLE IF NOT EXISTS nodetype (id int PRIMARY KEY, name text, token text); "
					                  + "CREATE OR REPLACE RULE ignore_duplicate AS ON INSERT TO nodetype "
					                  + "WHERE (EXISTS (SELECT id FROM nodetype WHERE nodetype.id = NEW.id)) "
					                  + "DO INSTEAD NOTHING; ";

			String createProjectTable = "CREATE TABLE IF NOT EXISTS project (id serial PRIMARY KEY, name text, path text); ";

			String createFileTable = "CREATE TABLE IF NOT EXISTS file (id serial PRIMARY KEY, name text, project_id int references project(id)); ";

			String createASTNodeTable = "CREATE TABLE IF NOT EXISTS astnode (id serial PRIMARY KEY, "
									  + "start_pos int, "
									  + "length int, "  	  // end_pos = start_pos + length
									  + "line_number int, "   // #-th line in file
									  + "column_number int, "
								      + "nodetype_id int references nodetype(id), "   // foreign key
								      + "file_id int references file(id), "		  // foreign key
								      + "binding_key text, "  // only some simple name nodes will have this
								      + "string text, "	      // string representation, stripped version
								      + "raw text, "          // raw content of ast node
								      + "parent_astnode_id int, "
								      + "declared_at_astnode_id int); "  // binding information, will fill this in second round
								      ;
			//start_pos, length, line_number, nodetype_id, binding_key, string, file_id
			stmt.executeUpdate(createProjectTable);
			stmt.executeUpdate(createFileTable);
			stmt.executeUpdate(createNodetypeTable);
			stmt.executeUpdate(createASTNodeTable);

			String to_create = "astnode_binding_key_idx";
			if (!indexes.contains(to_create)) {
				String createIndex = "CREATE INDEX "+to_create+" ON astnode(binding_key);";
				stmt.executeUpdate(createIndex);
			}

			to_create = "astnode_start_pos_idx";
			if (!indexes.contains(to_create)) {
				String createIndex = "CREATE INDEX "+to_create+" ON astnode(start_pos);";
				stmt.executeUpdate(createIndex);
			}

			to_create = "astnode_length_idx";
			if (!indexes.contains(to_create)) {
				String createIndex = "CREATE INDEX "+to_create+" ON astnode(length);";
				stmt.executeUpdate(createIndex);
			}

			to_create = "astnode_file_id_idx";
			if (!indexes.contains(to_create)) {
				String createIndex = "CREATE INDEX "+to_create+" ON astnode(file_id);";
				stmt.executeUpdate(createIndex);
			}

			to_create = "astnode_parent_astnode_id_idx";
			if (!indexes.contains(to_create)) {
				String createIndex = "CREATE INDEX "+to_create+" ON astnode(parent_astnode_id);";
				stmt.executeUpdate(createIndex);
			}

			for (Field field: JavaLexer.class.getDeclaredFields()) {
				if (field.getType().equals(int.class)) {
					int id = field.getInt(null);
					String string = field.getName();
					String token = JavaLexer.tokenNames[id];
					if (token.startsWith("'")) {
						token = token.substring(1, token.length() - 1);
					}
					stmt.executeUpdate(String.format("INSERT INTO nodetype (id, name, token) VALUES (%d, '%s', '%s');",
							id + PostgreSQLStorer.tokenBase, string, token));
				}
			}
			// insert nodetype value
			// TODO: this will throw IllegalAccessException, don't know why
			for (Field field: ASTNode.class.getDeclaredFields()) {
				if (field.getType().equals(int.class)) {
					stmt.executeUpdate(String.format("INSERT INTO nodetype (id, name) VALUES (%d, '%s');", field.getInt(null), field.getName()));
				}
			}

		} catch (SQLException e) {
			logger.log(Level.SEVERE, "Create table exception", e);
		} catch (IllegalAccessException e) {
			;
		} finally {
			closeIt(stmt);
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
			logger.log(Level.SEVERE, "Collect view names exception", e);
		}
		return views;
	}

	private Collection<String> collectIndexNames() {
		List <String> views = new ArrayList<String>();
		try {
			DatabaseMetaData meta = conn.getMetaData();
			ResultSet res = meta.getTables(null, null, null, new String[] {"INDEX"});
			while (res.next()) {
				views.add(res.getString("TABLE_NAME"));
			  }
		} catch (SQLException e) {
			Logger logger = Logger.getLogger("annotation");
			logger.log(Level.SEVERE, "Collect index names exception", e);
		}
		return views;
	}

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

			to_create = "astnode_type";
			if (!views.contains(to_create)) {
				String astnode_type = "CREATE VIEW astnode_type AS "
					+ "SELECT "
					+ "astnode.id AS astnode_id, "
					+ "start_pos, "
					+ "length, "
					+ "start_pos + length AS end_pos, "
					+ "line_number, "
					+ "column_number, "
					+ "nodetype_id, "
					+ "nodetype.name AS nodetype, "
					+ "file_id, "
					+ "string, "
					+ "raw, "
					+ "binding_key, "
					+ "parent_astnode_id, "
					+ "declared_at_astnode_id "

					+ "FROM astnode, nodetype "
					+ "WHERE astnode.nodetype_id = nodetype.id;"
					;
				stmt.executeUpdate(astnode_type);
			}

			to_create = "astnode_all";
			if (!views.contains(to_create)) {
				String astnode_type = "CREATE VIEW astnode_all AS "
					+ "SELECT "
					+ "astnode.id AS astnode_id, "
					+ "start_pos, "
					+ "length, "
					+ "start_pos + length AS end_pos, "
					+ "line_number, "
					+ "column_number, "
					+ "nodetype_id, "
					+ "nodetype.name AS nodetype, "
					+ "file_id, "
					+ "file.name AS file_name, "
					+ "project.id AS project_id, "
					+ "project.name AS project_name, "
					+ "project.path AS project_path, "
					+ "string, "
					+ "raw, "
					+ "binding_key, "
					+ "parent_astnode_id, "
					+ "declared_at_astnode_id "

					+ "FROM astnode, nodetype, file, project "
					+ "WHERE astnode.nodetype_id = nodetype.id "
					+ "AND astnode.file_id = file.id "
					+ "AND file.project_id = project.id;"
					;
				stmt.executeUpdate(astnode_type);
			}

		} catch (SQLException e) {
			logger.log(Level.SEVERE, "Create views "+ to_create + " exception\n" + e.toString());
		} finally {
			closeIt(stmt);
		}

	}


	public void close() {
		try {
			this.conn.close();
		} catch (SQLException e) {
			logger.log(Level.SEVERE, "Cannot close", e);
		} finally {
			ready = false;
		}
	}


	public int retrieveFileId(String fileName, int projectId) {
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
			logger.log(Level.SEVERE, String.format("Retrieve file=%s id", fileName), e);
		} finally {
			closeIt(stmt);
		}
		return -1; // should not happen
	}


	public int retrieveProjectId(String projectName, String sourcePath) {
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
			logger.log(Level.SEVERE, String.format("Retrieve project=%s id", projectName), e);
		} finally {
			closeIt(stmt);
		}
		return -1; // should not happen
	}


	public void saveAstNodeInfo(int start_pos, int length, int line_number, int column_number, int nodetype_id, String binding_key, String string, int file_id, String currentFileRaw, int parent_id) {
		PreparedStatement pstmt = null;
		try {
			pstmt = conn.prepareStatement("INSERT INTO astnode (start_pos, length, line_number, column_number, nodetype_id, binding_key, string, file_id, raw, parent_astnode_id) "
					+ "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
			pstmt.setInt(1, start_pos);
			pstmt.setInt(2, length);
			pstmt.setInt(3, line_number);
			pstmt.setInt(4, column_number);
			pstmt.setInt(5, nodetype_id);
			pstmt.setString(6, binding_key);
			pstmt.setString(7, string);
			pstmt.setInt(8, file_id);
			pstmt.setString(9, currentFileRaw.substring(start_pos, start_pos+length));
			pstmt.setInt(10, parent_id);
			pstmt.executeUpdate();
		} catch (SQLException e) {
			logger.log(Level.SEVERE, String.format("Save ASTNode=%s", string), e);
		} finally {
			closeIt(pstmt);
		}
	}

	public void saveTokenInfo(int start_pos, int length, int line_number, int column_number, int nodetype_id, String string, int file_id, String currentFileRaw, int parent_id) {
		PreparedStatement pstmt = null;
		try {
			pstmt = conn.prepareStatement("INSERT INTO astnode (start_pos, length, line_number, column_number, nodetype_id, string, file_id, raw, parent_astnode_id) "
					+ "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)");
			pstmt.setInt(1, start_pos);
			pstmt.setInt(2, length);
			pstmt.setInt(3, line_number);
			pstmt.setInt(4, column_number);
			pstmt.setInt(5, nodetype_id);
			pstmt.setString(6, string);
			pstmt.setInt(7, file_id);
			pstmt.setString(8, currentFileRaw.substring(start_pos, start_pos+length));
			pstmt.setInt(9, parent_id);
			pstmt.executeUpdate();
		} catch (SQLException e) {
			logger.log(Level.SEVERE, String.format("Save Token=%s", string), e);
		} catch (Exception e) {
			logger.log(Level.SEVERE, String.format("Save Token=%s", string), e);
		} finally {
			closeIt(pstmt);
		}
	}

	public void saveForeignAstNode(int start_pos, int length, int nodetype_id, String binding_key, int file_id) {
		PreparedStatement stmt = null;
		try {
			// find foreign astnode
			String query = "SELECT id FROM astnode WHERE start_pos=? AND length=? AND nodetype_id=? AND file_id=?";
			stmt = conn.prepareStatement(query);
			stmt.setInt(1, start_pos);
			stmt.setInt(2, length);
			stmt.setInt(3, nodetype_id);
			stmt.setInt(4, file_id);
			ResultSet rs = stmt.executeQuery();
			int total = 0;
			int foreign_id = -1;
			while (rs.next()) {
				total ++;
				foreign_id = rs.getInt("id");
			}

			if (total > 1) {
				throw new IllegalStateException("resolve more than on astnode");
			}

			String update = "UPDATE astnode SET declared_at_astnode_id = ? WHERE binding_key = ?;";
			stmt = conn.prepareStatement(update);
			stmt.setInt(1, foreign_id);
			stmt.setString(2, binding_key);
			stmt.executeUpdate();

		} catch (SQLException e) {
			logger.log(Level.SEVERE, "Resolve foreign astnode exception", e);
		} finally {
			closeIt(stmt);
		}
	}

	public void clearProjectAstnode(int project_id) {
		Statement stmt = null;
		try {
			stmt = conn.createStatement();
			String clear = "DELETE FROM astnode WHERE astnode.id IN ("
					+ "SELECT astnode_id AS id "
					+ "FROM astnode_all "
					+ "WHERE project_id = " + project_id + ");"

					+ "DELETE FROM file WHERE project_id = " + project_id + ";";
			stmt.executeUpdate(clear);
		} catch (SQLException e) {
			logger.log(Level.SEVERE, "Clear project exception", e);
		} finally {
			closeIt(stmt);
		}

	}

	public PostgreSQLStorer resetDatabase() {
		Statement stmt = null;
		try {
			stmt = conn.createStatement();
			stmt.execute("DROP TABLE astnode CASCADE;"
					+ "DROP TABLE project CASCADE;"
					+ "DROP TABLE file CASCADE;"
					+ "DROP TABLE nodetype CASCADE;");
		} catch (SQLException e) {
			logger.log(Level.SEVERE, "Clear database error", e);
		} finally {
			closeIt(stmt);
		}
		createTableIfNotExist();
		createViewIfNotExist();
		return this;
	}

	public boolean isReady() {
		return ready;
	}

	public int queryAstNodeId(int start_pos, int length, int nodetype, int file_id) {
		PreparedStatement stmt = null;
		String query = "SELECT id FROM astnode WHERE start_pos=? AND length=? AND nodetype_id=? AND file_id=?;";
		int result = -1;
		try {
			stmt = conn.prepareStatement(query);
			stmt.setInt(1, start_pos);
			stmt.setInt(2, length);
			stmt.setInt(3, nodetype);
			stmt.setInt(4, file_id);
			ResultSet rs = stmt.executeQuery();

			int count = 0;
			while (rs.next()) {
				result = rs.getInt("id");
				count ++;
			}
			if (count > 1)
				throw new IllegalStateException("more than one astnode found while query");
		} catch (SQLException e) {
			logger.log(Level.SEVERE, "query parent id exception", e);
		} finally {
			closeIt(stmt);
		}
		return result;
	}

	private void closeIt(PreparedStatement stmt) {
		if (stmt != null) {
			try {
				stmt.close();
			} catch (SQLException e) {
				;
			}
		}
	}

	private void closeIt(Statement stmt) {
		if (stmt != null) {
			try {
				stmt.close();
			} catch (SQLException e) {
				;
			}
		}
	}
}
