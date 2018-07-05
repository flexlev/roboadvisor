package com.roboadvisor.datarepository;

public class Holding {
	
	private String symbol;
	private String portfolioName;
	private double weight;
	
	public Holding(String symbol, String portfolioName, double weight) {
		this.symbol = symbol;
		this.portfolioName = portfolioName;
		this.weight = weight;
	}

	public String getSymbol() {
		return symbol;
	}

	public void setSymbol(String symbol) {
		this.symbol = symbol;
	}

	public String getPortfolioName() {
		return portfolioName;
	}

	public void setPortfolioName(String portfolioName) {
		this.portfolioName = portfolioName;
	}

	public double getWeight() {
		return weight;
	}

	public void setWeight(double weight) {
		this.weight = weight;
	}
	

}
