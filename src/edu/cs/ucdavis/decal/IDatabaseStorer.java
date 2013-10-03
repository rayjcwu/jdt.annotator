package edu.cs.ucdavis.decal;

public interface IDatabaseStorer {

	public void init();
	public void connect();
	public void createTableIfNotExist();
	public void createViewIfNotExist();

	public void close();
	public int retrieveFileId(String fileName, int projectId);
	public int retrieveProjectId(String projectName, String sourcePath);
	public void saveAstNodeInfo(int start_pos, int length, int line_number, int nodetype_id, String binding_key, String string, int file_id);
	public void saveForeignAstNode(int start_pos, int length, int nodetype_id, String binding_key, int file_id);

	public boolean isReady();
}