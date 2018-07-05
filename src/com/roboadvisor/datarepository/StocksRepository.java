package com.roboadvisor.datarepository;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.roboadvisor.dao.MysqlConnect;
import com.roboadvisor.stockapi.Stock;

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

	public void insertStockByPortfolio(String symbol, double weight, String portfolioName) {
		conn = new MysqlConnect();
    	conn.connect();
    	try {
    		String sqlStatement = "INSERT INTO `holdings` (stock_ticker, weight, portfolio_name) VALUES (?, ?, ?)";
        	PreparedStatement preparedStatement = conn.getConnection().prepareStatement(sqlStatement);
			preparedStatement.setString(1, symbol);
			preparedStatement.setDouble(2, weight);
			preparedStatement.setString(3, portfolioName);
			preparedStatement.execute();
		}   catch (SQLException e) {
			e.printStackTrace();
		}	finally {
			conn.disconnect();
		}
	}

	public List<Holding> findHoldingsByPortfolio(String portfolioName) {
		conn = new MysqlConnect();
    	conn.connect();
    	List<Holding> holdings = new ArrayList<Holding>();
    	try {
    		String sqlStatement = "SELECT * FROM `holdings` WHERE portfolio_name = ?";
        	PreparedStatement preparedStatement = conn.getConnection().prepareStatement(sqlStatement);
			preparedStatement.setString(1, portfolioName);
			ResultSet rs = preparedStatement.executeQuery();
			
			while(rs.next()) {
				Holding holding = new Holding(rs.getString("stock_ticker"),rs.getString("portfolio_name"),rs.getDouble("weight"));
				holdings.add(holding);
			}
		}   catch (SQLException e) {
			e.printStackTrace();
		}	finally {
			conn.disconnect();
		}
		return holdings;
	}


}
