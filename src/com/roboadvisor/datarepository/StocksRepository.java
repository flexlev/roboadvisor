package com.roboadvisor.datarepository;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.roboadvisor.dao.MysqlConnect;

public class StocksRepository {
	
	private MysqlConnect conn;
	
	
	public StocksRepository() {
		
	}
	
	public void findClosedByDates() {
		conn = new MysqlConnect();
    	conn.connect();
    	try {
    		String sqlStatement = "SELECT * FROM `proshares_ultra_s&p500_sso` ";
        	PreparedStatement preparedStatement = conn.getConnection().prepareStatement(sqlStatement);
			ResultSet rs = preparedStatement.executeQuery();
			while (rs.next()) {
				System.out.println(rs.getString("adj_close"));
			}
		}   catch (SQLException e) {
			e.printStackTrace();
		}	finally {
			conn.disconnect();
		}

	}
	
	public static void main(String[] args) {
		StocksRepository sr = new StocksRepository();
		sr.findClosedByDates();
	}

}
