package edu.cs.ucdavis.decal;

public interface IDatabaseStorer {
	
	public void init();
	public void connect();	
	public void createTableIfNotExist();	
	public void close();		
	public int retrieveFileId(String fileName, int projectId);
	public int retrieveProjectId(String projectName);
	public boolean isReady();	
}