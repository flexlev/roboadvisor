package com.roboadvisor.stockapi;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

import org.apache.commons.math3.stat.regression.OLSMultipleLinearRegression;

public class Stock {
	
	private String symbol; 
	private String sector;
	private String industry;
	private String country;
	private double marketCap;
	
	private double[] adjustedCloseTS;
	private Date[] dateTS;
	private double[] weeklyLogReturn;
	
	private double historicalWeeklyVol;
	private double historicalWeeklyReturn;
	
	private double[] factorWeights;

	public Stock(String symbol, double[] adjustedCloseTS, Date[] dateTS) {
		this.symbol = symbol; 
		this.adjustedCloseTS = adjustedCloseTS;
		this.dateTS = dateTS;
		weeklyLogReturn = new double[adjustedCloseTS.length];
		for(int i = 0; i <adjustedCloseTS.length -1; i++ ) {
			weeklyLogReturn[i] = Math.log(adjustedCloseTS[i+1]/adjustedCloseTS[i]);
		}
	} 
	
	public Stock(String symbol, String sector, String industry, String marketCap, String country) {
		this.symbol = symbol.replace("\"","");
		this.sector = sector.replace("\"","");
		this.industry = industry.replace("\"","");
		this.marketCap = StockHelper.findNominalMarketCap(marketCap);
		this.country = country;
		
		this.adjustedCloseTS = null;
		this.dateTS = null;
		this.weeklyLogReturn = null;
	}

	public Stock(Stock ticker, double[] prices, Date[] dates) {
		this.symbol = ticker.symbol;
		this.sector = ticker.sector;
		this.industry = ticker.industry;
		this.marketCap = ticker.marketCap;
		this.country = ticker.country;
				
		this.adjustedCloseTS = prices;
		this.dateTS = dates;
		weeklyLogReturn = new double[adjustedCloseTS.length];
		for(int i = 0; i <adjustedCloseTS.length -1; i++ ) {
			weeklyLogReturn[i] = Math.log(adjustedCloseTS[i+1]/adjustedCloseTS[i]);
		}
		
		//Finding Volatility/Average Return
		double sum = 0;
		for(int i = 0; i <weeklyLogReturn.length; i++ ) {
			sum += weeklyLogReturn[i];
		}
		this.historicalWeeklyReturn = sum / weeklyLogReturn.length;
		
		sum = 0;
		for(int i = 0; i <weeklyLogReturn.length; i++ ) {
			sum += Math.pow(weeklyLogReturn[i]-this.historicalWeeklyReturn, 2);
		}
		this.historicalWeeklyVol = Math.sqrt(sum/ weeklyLogReturn.length);
	}

//	public void fitToFactors(Stock[] factorsStocks) {
//		double[][] x = new double[factorsStocks.length][factorsStocks[1].adjustedCloseTS.length];
//		for(int i =0; i < factorsStocks[1].adjustedCloseTS.length; i++) {
//			for(int j=0; j <factorsStocks.length ; j++) {
//				x[i][j] = factorsStocks[j].adjustedCloseTS[i];
//			}
//		}
//		OLSMultipleLinearRegression regression = new OLSMultipleLinearRegression();
//		//regression.newSampleData();
//	}
	
	public String getSymbol() { 
		return this.symbol;		
	} 
	
	public double[] getAdjustedCloseTS() {
		return adjustedCloseTS;
	}

	public Date[] getDateTS() {
		return dateTS;
	}

	public double[] getWeeklyLogReturn() {
		return weeklyLogReturn;
	}

	public void setWeeklyLogReturn(double[] weeklyLogReturn) {
		this.weeklyLogReturn = weeklyLogReturn;
	}

	public String getSector() {
		return sector;
	}

	public void setSector(String sector) {
		this.sector = sector;
	}

	public String getIndustry() {
		return industry;
	}

	public void setIndustry(String industry) {
		this.industry = industry;
	}

	public void setSymbol(String symbol) {
		this.symbol = symbol;
	}
	
	public void setMarketCap(double marketCap) {
		this.marketCap = marketCap;
	}

	public double getMarketCap() {
		return this.marketCap;
	}
	
	
	public double getHistoricalWeeklyVol() {
		return historicalWeeklyVol;
	}

	public void setHistoricalWeeklyVol(double historicalWeeklyVol) {
		this.historicalWeeklyVol = historicalWeeklyVol;
	}

	public double getHistoricalWeeklyReturn() {
		return historicalWeeklyReturn;
	}

	public void setHistoricalWeeklyReturn(double historicalWeeklyReturn) {
		this.historicalWeeklyReturn = historicalWeeklyReturn;
	}

	public double[] getFactorWeights() {
		return factorWeights;
	}

	public void setFactorWeights(double[] factorWeights) {
		this.factorWeights = factorWeights;
	}

	public void setAdjustedCloseTS(double[] adjustedCloseTS) {
		this.adjustedCloseTS = adjustedCloseTS;
	}

	public void setDateTS(Date[] dateTS) {
		this.dateTS = dateTS;
	}
	
	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	@Override
	public String toString() {
		String str = 
				"Symbol : " + this.symbol + " \n" + 
				"sector : " + this.sector + " \n" + 
				"industry : " + this.industry + " \n" + 
				"marketCap : " + this.marketCap + " \n" + 
				"country : " + this.country + " \n" +
				"Adjusted CLose : " + Arrays.toString(adjustedCloseTS) + " \n" +
				"Dates : " + Arrays.toString(dateTS) + " \n" +
				"Weekly Log Return : " + Arrays.toString(weeklyLogReturn) + " \n" +
				"Historical Return : " + this.historicalWeeklyReturn + " \n" + 
				"Historical Vol : " + this.historicalWeeklyVol + " \n";
		return str;
	}
	

	
}
