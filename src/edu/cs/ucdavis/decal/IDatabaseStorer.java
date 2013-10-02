package edu.cs.ucdavis.decal;

public interface IDatabaseStorer {
	
	public void init();
	public void connect();	
	public void createTableIfNotExist();	
	public void close();		
	public int retrieveIdFrom(String table, String column, String value);		
	public boolean isReady();	
}