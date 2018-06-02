package com.roboadvisor.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class MysqlConnect {

	// init database constants
		private static final String DATABASE_DRIVER = "com.mysql.jdbc.Driver";
		private static final String DATABASE_URL = "jdbc:mysql://localhost:3306/roboadvisor";
		private static final String USERNAME = "root";
		private static final String PASSWORD = "";
		
		private Connection connection;
		private Properties properties;
		
		public MysqlConnect() {
			properties = getProperties();
		}
		
		// create properties
	    private Properties getProperties() {
	        if (properties == null) {
	            properties = new Properties();
	            properties.setProperty("user", USERNAME);
	            properties.setProperty("password", PASSWORD);
	        }
	        return properties;
	    }
		
	    // connect database
	    public Connection connect() {
	        if (connection == null) {
	            try {
	                Class.forName(DATABASE_DRIVER);
	                connection = DriverManager.getConnection(DATABASE_URL, this.properties);
	            } catch (ClassNotFoundException | SQLException e) {
	                e.printStackTrace();
	            }
	        }
	        return connection;
	    }
	    
	    // disconnect database
	    public void disconnect() {
	        if (connection != null) {
	            try {
	                connection.close();
	                connection = null;
	            } catch (SQLException e) {
	                e.printStackTrace();
	            }
	        }
	    }
}
