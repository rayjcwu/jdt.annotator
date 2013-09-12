package edu.cs.ucdavis.decal;

import org.hibernate.SessionFactory;

public class HibernateClient {
	SessionFactory sessionFactory;
	
	public HibernateClient() {
		sessionFactory = HibernateUtil.getSessionFactory();
	}
	
	public void save() {
		
	}
	
	public void load() {
		
	}
	
	public void run() {
		
	}
}
