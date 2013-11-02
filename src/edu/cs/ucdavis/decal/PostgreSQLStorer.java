package edu.cs.ucdavis.decal;

import java.lang.reflect.Field;
import java.sql.BatchUpdateException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.antlr.JavaLexer;
import org.eclipse.jdt.core.dom.ASTNode;

/**
 * Database logic
 * @author jcwu
 *
 */
public class PostgreSQLStorer {
	private boolean ready;
	private Connection conn;
	private String url;
	private Logger logger;

	private String username;
	private String password;

	public static int tokenBase = 100;   // token index starts from 100

	private static Map<String /* index name */, String /* SQL */> indexToCreate;
	static {
		indexToCreate = new HashMap<String, String>();  // order doesn't matter
		indexToCreate.put("cross_ref_key_cross_ref_key_idx",
						  "CREATE INDEX cross_ref_key_cross_ref_key_idx ON cross_ref_key(cross_ref_key);");
		indexToCreate.put("cross_ref_key_entity_id_idx",
				  		  "CREATE INDEX cross_ref_key_entity_id_idx ON cross_ref_key(entity_id, project_id);");

		indexToCreate.put("entity_start_pos_idx",
				  		  "CREATE INDEX entity_start_pos_idx ON entity(start_pos);");
		indexToCreate.put("entity_length_idx",
		  		  		  "CREATE INDEX entity_length_idx ON entity(length);");
		indexToCreate.put("entity_file_id_idx",
		  		  		  "CREATE INDEX entity_file_id_idx ON entity(file_id);");
		indexToCreate.put("entity_parent_id_idx",
		  		  		  "CREATE INDEX entity_parent_id_idx ON entity(parent_id, project_id);");

		indexToCreate.put("method_entity_id_idx",
		  		  		  "CREATE INDEX method_entity_id_idx ON method(entity_id, project_id);");

		indexToCreate.put("cross_ref_declared_id_idx",
		  		  		  "CREATE INDEX cross_ref_declared_id_idx ON cross_ref(declared_id, project_id);");
		indexToCreate.put("cross_ref_reference_id_idx",
		  		  		  "CREATE INDEX cross_ref_reference_id_idx ON cross_ref(reference_id, project_id);");
	}

	private static Map<String /* table name */, String /* SQL */> tableToCreate;
	static {
		tableToCreate = new LinkedHashMap<String, String>();  // need to preserve order of creation
		tableToCreate.put("nodetype",
						  "CREATE TABLE IF NOT EXISTS nodetype (nodetype_id int PRIMARY KEY, name text, token text); "
						+ "CREATE OR REPLACE RULE ignore_duplicate AS ON INSERT TO nodetype "
					    + "WHERE (EXISTS (SELECT nodetype.nodetype_id FROM nodetype WHERE nodetype.nodetype_id = NEW.nodetype_id)) "
					    + "DO INSTEAD NOTHING; ");

		tableToCreate.put("project",
						  "CREATE TABLE IF NOT EXISTS project ("
						+ "project_id serial PRIMARY KEY, "
						+ "project_type text, "
						+ "project_name text, "
						+ "description text, "
						+ "project_path text); ");

		tableToCreate.put("file",
				 		  "CREATE TABLE IF NOT EXISTS file ( "
						+ "file_id serial PRIMARY KEY, "
						+ "project_id int REFERENCES project(project_id) ON DELETE CASCADE ON UPDATE CASCADE NOT NULL, "
						+ "file_type text, "
						+ "file_name text, "
						+ "file_path text); ");

		tableToCreate.put("entity",
						  "CREATE TABLE IF NOT EXISTS entity ("
						+ "entity_id bigint NOT NULL, "
						+ "file_id int REFERENCES file (file_id) ON DELETE CASCADE ON UPDATE CASCADE NOT NULL, "		  // foreign key
					    + "project_id int REFERENCES project (project_id) ON DELETE CASCADE ON UPDATE CASCADE NOT NULL, "
						+ "start_pos int NOT NULL, "
						+ "length int NOT NULL, "  	  // end_pos = start_pos + length
						+ "start_line_number int NOT NULL, "   // #-th line in file
						+ "start_column_number int NOT NULL, "
						+ "end_line_number int NOT NULL, "
						+ "end_column_number int NOT NULL, "
					    + "nodetype_id int REFERENCES nodetype (nodetype_id) ON DELETE CASCADE ON UPDATE CASCADE NOT NULL, "   // foreign key
					    + "string text, "	      // string representation, stripped version
					    + "raw text, "          // raw content of ast node
					    + "parent_id bigint, "
					    + "PRIMARY KEY (entity_id, project_id)); ");
						// don't reference to self otherwise parent_id = -1 will be invalid


		tableToCreate.put("cross_ref_key",
						  "CREATE TABLE IF NOT EXISTS cross_ref_key( "
						+ "entity_id bigint NOT NULL, "
						+ "project_id int NOT NULL, "
						+ "cross_ref_key text, "
						+ "FOREIGN KEY (entity_id, project_id) REFERENCES entity (entity_id, project_id) ON DELETE CASCADE ON UPDATE CASCADE); ");

		tableToCreate.put("cross_ref",
				 		  "CREATE TABLE IF NOT EXISTS cross_ref( "
						//+ "ref_id serial PRIMARY KEY, "
						+ "declared_id bigint NOT NULL, "
						+ "reference_id bigint NOT NULL, "
						+ "project_id int NOT NULL, "  // will reference within same project
						+ "FOREIGN KEY (declared_id, project_id) REFERENCES entity (entity_id, project_id) ON DELETE CASCADE ON UPDATE CASCADE, "
						+ "FOREIGN KEY (reference_id, project_id) REFERENCES entity (entity_id, project_id) ON DELETE CASCADE ON UPDATE CASCADE); ");

		tableToCreate.put("method",
						  "CREATE TABLE IF NOT EXISTS method( "
						+ "entity_id bigint NOT NULL, "
						+ "project_id int NOT NULL, "
						+ "method_name text, "
						+ "return_type text, "
						+ "argument_type text, "
						+ "full_signature text, "
						+ "is_declare boolean NOT NULL, "
						+ "FOREIGN KEY (entity_id, project_id) REFERENCES entity (entity_id, project_id) ON DELETE CASCADE ON UPDATE CASCADE); ");
	}

	private static Map <String /* view name */, String /* SQL */> viewToCreate;
	static {
		viewToCreate = new HashMap<String, String>();
		/// create view

		viewToCreate.put("project_file", "CREATE VIEW project_file AS "
				+ "SELECT project.project_id AS project_id, "
				+ "project.project_name AS project_name, "
				+ "project.project_path as project_path, "
				+ "file.file_id AS file_id, "
				+ "file.file_name AS file_name "

				+ "FROM project, file "

				+ "WHERE file.project_id = project.project_id;");

		viewToCreate.put("entity_cross_ref", "CREATE VIEW entity_cross_ref AS "
				+ "SELECT "
				+ "entity_id, "
				+ "file_id, "
				+ "entity.project_id AS project_id, "
				+ "start_pos, "
				+ "length, "
				+ "start_line_number, "
				+ "start_column_number, "
				+ "end_line_number, "
				+ "end_column_number, "
				+ "nodetype_id, "
				+ "string, "
				+ "raw, "
				+ "parent_id,"
				+ "declared_id "

				+ "FROM entity LEFT JOIN cross_ref "
				+ "ON entity.entity_id = cross_ref.reference_id "
				+ "AND entity.project_id = cross_ref.project_id; ");

		viewToCreate.put("entity_nodetype", "CREATE VIEW entity_nodetype AS "
				+ "SELECT entity_cross_ref.entity_id AS entity_id, "
				+ "start_pos, "
				+ "length, "
				+ "start_pos + length AS end_pos, "
				+ "start_line_number, "
				+ "start_column_number, "
				+ "end_line_number, "
				+ "end_column_number, "
				+ "entity_cross_ref.nodetype_id AS nodetype_id, "
				+ "nodetype.name AS nodetype, "

				+ "file_id, "
				+ "string, "
				+ "raw, "
				+ "parent_id, "
				+ "declared_id "

				+ "FROM entity_cross_ref, nodetype "
				+ "WHERE entity_cross_ref.nodetype_id = nodetype.nodetype_id;");

		viewToCreate.put("entity_all", "CREATE VIEW entity_all AS "
				+ "SELECT "
				+ "entity_cross_ref.entity_id AS entity_id, "
				+ "start_pos, "
				+ "length, "
				+ "start_pos + length AS end_pos, "
				+ "start_line_number, "
				+ "start_column_number, "
				+ "end_line_number, "
				+ "end_column_number, "

				+ "entity_cross_ref.nodetype_id AS nodetype_id, "
				+ "nodetype.name AS nodetype, "
				+ "entity_cross_ref.file_id AS file_id, "
				+ "file.file_name AS file_name, "
				+ "project.project_id AS project_id, "
				+ "project.project_name AS project_name, "
				+ "project.project_path AS project_path, "
				+ "string, "
				+ "raw, "
				+ "parent_id,"
				+ "declared_id "

				+ "FROM entity_cross_ref, nodetype, file, project "
				+ "WHERE entity_cross_ref.nodetype_id = nodetype.nodetype_id "
				+ "AND entity_cross_ref.file_id = file.file_id "
				+ "AND file.project_id = project.project_id; ");

	}

	private Map <String, Long> entity_id_cache;

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
		this.ready = true;
		this.entity_id_cache = new HashMap<String, Long>();
	}

	/**
	 * Drop all tables in database and recreate them.
	 * @return
	 */
	public PostgreSQLStorer resetDatabase() {
		Statement stmt = null;
		try {
			stmt = conn.createStatement();
			for (Entry <String, String> entry: tableToCreate.entrySet()) {
				stmt.execute(String.format("DROP TABLE IF EXISTS %s CASCADE;", entry.getKey()));
			}
		} catch (SQLException e) {
			logger.log(Level.SEVERE, "reset database error", e);
		} finally {
			closeIt(stmt);
		}
		createTableIfNotExist();
		return this;
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

	/**
	 * Create table/view/index
	 */
	public void createTableIfNotExist() {
		Statement stmt = null;
		try {
			stmt = conn.createStatement();

			// create table
			for (Entry<String, String> entry: tableToCreate.entrySet()) {
				String createTableSql = entry.getValue();
				stmt.executeUpdate(createTableSql);
			}

			// create index
			Collection<String> indexes = collectMetaNames("INDEX");
			for (Entry <String, String> entry: indexToCreate.entrySet()) {
				String indexName = entry.getKey();
				String createIndexSql = entry.getValue();
				if (!indexes.contains(indexName)) {
					stmt.execute(createIndexSql);
				}
			}


			// create view
			Collection <String> views = collectMetaNames("VIEW");
			for (Entry <String, String> entry: viewToCreate.entrySet()) {
				String viewName = entry.getKey();
				String createViewSql = entry.getValue();
				if (!views.contains(viewName)) {
					stmt.executeUpdate(createViewSql);
				}
			}

			// insert nodetype of tokens
			for (Field field: JavaLexer.class.getDeclaredFields()) {
				if (field.getType().equals(int.class)) {
					int id = field.getInt(null);
					String string = field.getName();
					String token = JavaLexer.tokenNames[id];
					if (token.startsWith("'")) {
						token = token.substring(1, token.length() - 1);
					}
					stmt.executeUpdate(String.format("INSERT INTO nodetype (nodetype_id, name, token) VALUES (%d, '%s', '%s');",
							id + PostgreSQLStorer.tokenBase, string, token));
				}
			}
			// insert nodetype of ASTNode
			// TODO: this will throw IllegalAccessException, don't know why, that's why I put it in the end
			for (Field field: ASTNode.class.getDeclaredFields()) {
				if (field.getType().equals(int.class)) {
					stmt.executeUpdate(String.format("INSERT INTO nodetype (nodetype_id, name) VALUES (%d, '%s');", field.getInt(null), field.getName()));
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


	/**
	 * Collect defined view/index names
	 * @param metaName "VIEW" or "INDEX"
	 * @return
	 */
	private Collection<String> collectMetaNames(String metaName) {
		List <String> names = new ArrayList<String>();
		try {
			DatabaseMetaData meta = conn.getMetaData();
			ResultSet res = meta.getTables(null, null, null, new String[] {metaName});
			while (res.next()) {
				names.add(res.getString("TABLE_NAME"));
			  }
		} catch (SQLException e) {
			Logger logger = Logger.getLogger("annotation");
			logger.log(Level.SEVERE, String.format("Collect %s names exception", metaName), e);
		}
		return names;
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

	/**
	 * Use fileName and projectId to uniquely identify a file.
	 * @param fileName
	 * @param projectId
	 * @return Will return -1 if file doesn't exist
	 */
	public int retrieveFileId(String fileName, int projectId) {
		this.entity_id_cache.clear();
		Statement stmt = null;
		try {
			stmt = conn.createStatement();
			String query = String.format("SELECT file_id FROM file WHERE file_name='%s' AND project_id = %d;", fileName, projectId);
			ResultSet result = stmt.executeQuery(query);
			if(result.next()) {
				return result.getInt("file_id");
			} else {
				stmt.executeUpdate(String.format("INSERT INTO file (file_name, project_id) VALUES ('%s', %s);", fileName, projectId));
				ResultSet result2 = stmt.executeQuery(query);
				if (result2.next()) {
					return result2.getInt("file_id");
				}
			}
		} catch (SQLException e) {
			logger.log(Level.SEVERE, String.format("Retrieve file=%s id", fileName), e);
		} finally {
			closeIt(stmt);
		}
		return -1; // should not happen
	}

	/**
	 * Query project id in database. Use projectName and sourcePath to uniquely identify a project.
	 * @param projectName
	 * @param sourcePath
	 * @return Will return -1 if project doesn't exist
	 */
	public int retrieveProjectId(String projectName, String sourcePath) {
		PreparedStatement stmt = null;
		ResultSet result = null;
		String query = "SELECT project_id FROM project WHERE project_name=? AND project_path=?;";
		try {
			stmt = conn.prepareStatement(query);
			stmt.setString(1, projectName);
			stmt.setString(2, sourcePath);
			result = stmt.executeQuery();
			if(result.next()) {
				return result.getInt("project_id");
			} else {
				PreparedStatement ins_stmt = conn.prepareStatement("INSERT INTO project (project_name, project_type, project_path) VALUES (?, 'java', ?);");
				ins_stmt.setString(1, projectName);
				ins_stmt.setString(2, sourcePath);
				ins_stmt.executeUpdate();
				result = stmt.executeQuery();
				if (result.next()) {
					return result.getInt("project_id");
				}
			}
		} catch (SQLException e) {
			logger.log(Level.SEVERE, String.format("Retrieve project=%s id", projectName), e);
		} finally {
			closeIt(stmt);
		}
		return -1; // should not happen
	}

	public void saveCrossRefAstNode(int start_pos, int length, int nodetype_id, String cross_ref_key, int file_id, int project_id) {
		final long declare_id = queryAstNodeId(start_pos, length, nodetype_id, file_id, project_id);  // will throw illegal state exception if can't find such entity
		PreparedStatement stmt = null;
		ResultSet entitiesWithSameCrossRefKey = null;
		try {
			// find all references with same cross_ref_key
			String query_same_cross_ref_key = String.format("SELECT entity_id AS ref_id FROM cross_ref_key WHERE cross_ref_key=?;");
			stmt = conn.prepareStatement(query_same_cross_ref_key);
			stmt.setString(1, cross_ref_key);
			entitiesWithSameCrossRefKey = stmt.executeQuery();

			long ref_id = -1;
			String update = "INSERT INTO cross_ref (declared_id, reference_id, project_id) VALUES (?, ?, ?);";
			stmt = conn.prepareStatement(update);
			while (entitiesWithSameCrossRefKey.next()) {
				ref_id = entitiesWithSameCrossRefKey.getLong("ref_id");
				stmt.setLong(1, declare_id);
				stmt.setLong(2, ref_id);
				stmt.setInt(3, project_id);
				stmt.addBatch();
			}
			stmt.executeBatch();
		} catch (BatchUpdateException e) {
			logger.log(Level.SEVERE, "batch update exception", e.getNextException());
		} catch (SQLException e) {
			logger.log(Level.SEVERE, "Resolve foreign entity exception", e);
		} finally {
			closeIt(entitiesWithSameCrossRefKey);
			closeIt(stmt);
		}
	}

	/**
	 * Clear stored annotation information in database for given projectId.
	 * All entity are ON DELETE CASCADE, therefore deleting file for given projectID is enough.
	 * @param project_id
	 */
	public void clearProjectAstnode(int project_id) {
		Statement stmt = null;
		try {
			stmt = conn.createStatement();
			String clear = "DELETE FROM file WHERE project_id = " + project_id + ";";
			stmt.executeUpdate(clear);
		} catch (SQLException e) {
			logger.log(Level.SEVERE, "Clear project exception", e);
		} finally {
			closeIt(stmt);
		}
	}

	public boolean isReady() {
		return ready;
	}

	/**
	 * Use start_pos, length, nodetype, file_id to uniquely identify an entity in entity table.
	 * @param start_pos
	 * @param length
	 * @param nodetype
	 * @param file_id
	 * @return Will return -1 if such entity doesn't exist
	 */
	public long queryAstNodeId(int start_pos, int length, int nodetype, int file_id, int project_id) {
		String queryKey = String.format("%d:%d:%d:%d:%d", start_pos, length, nodetype, file_id, project_id);
		if (entity_id_cache.containsKey(queryKey)) {
			return entity_id_cache.get(queryKey);
		}

		PreparedStatement stmt = null;
		String query = "SELECT entity_id FROM entity WHERE start_pos=? AND length=? AND nodetype_id=? AND file_id=? AND project_id=?;";
		long result = -1;
		try {
			stmt = conn.prepareStatement(query);
			stmt.setInt(1, start_pos);
			stmt.setInt(2, length);
			stmt.setInt(3, nodetype);
			stmt.setInt(4, file_id);
			stmt.setInt(5, project_id);
			ResultSet rs = stmt.executeQuery();

			int count = 0;
			while (rs.next()) {
				result = rs.getLong("entity_id");
				count ++;
			}
			if (count > 1)
				throw new IllegalStateException("more than one entity found while query");
		} catch (SQLException e) {
			logger.log(Level.SEVERE, "query parent id exception", e);
		} finally {
			closeIt(stmt);
		}
		if (entity_id_cache.size() > 2048) {
			entity_id_cache.clear();
		}
		entity_id_cache.put(queryKey, result);
		return result;
	}

	public static void closeIt(PreparedStatement stmt) {
		if (stmt != null) {
			try {
				stmt.close();
			} catch (SQLException e) {
				;
			}
		}
	}

	public static void closeIt(Statement stmt) {
		if (stmt != null) {
			try {
				stmt.close();
			} catch (SQLException e) {
				;
			}
		}
	}

	public static void closeIt(ResultSet rs) {
		if (rs != null) {
			try {
				rs.close();
			} catch (SQLException e) {
				;
			}
		}
	}

	public Connection getConnection() {
		return conn;
	}

	/**
	 * Entity id is serial (auto increment in MySql).
	 * This method will retrieve an entity id greater then current biggest entity id.
	 * @param id_seq For table entity, the auto generated id_seq table name is entity_entity_id_seq
	 * @return
	 */
	public Long getNextSerial(String id_seq) {
		Long serialNum = 0L;
		Statement stmt = null;
		// get the postgresql serial field value with this query
		String query = String.format("SELECT nextval('%s');", id_seq);
		try {
			stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(query);
			if (rs.next()) {
				serialNum = rs.getLong(1);
			}
		} catch (SQLException e) {
			System.err.println("Should not happen");
			throw new IllegalStateException("query next serial error");
		} finally {
			closeIt(stmt);
		}
		return serialNum;
	}
}