package com.roboadvisor.specialstrategy;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;

import org.apache.commons.lang3.time.DateFormatUtils;

import com.roboadvisor.stockapi.Stock;
import com.roboadvisor.stockapi.StockFetcherYahoo;

public class WeightedAverage {
	
	private StockFetcherYahoo stockFetcherYahoo;

	private static ArrayList<Stock> stockAssets;
	
	private static ArrayList<Stock> tickers;
	double[] small = new double[10];
	double[] medium = new double[20];
	double[] large = new double[30];
	
	private static ArrayList<Double> value;
	private static ArrayList<String> holding;
	private static ArrayList<Date> dates;
	
	public static void main(String[] args) {
		new WeightedAverage();
	}
	
	public WeightedAverage() {
		Date beg = new GregorianCalendar(2006, 06, 10).getTime();
		Date end = new GregorianCalendar(2018, 05, 02).getTime();
		
		stockFetcherYahoo = new StockFetcherYahoo(beg, end);
		tickers = new ArrayList<Stock>();
//		tickers.add(new Stock("SSO","2XSP500","","",""));
//		tickers.add(new Stock("SDS","-2XSP500","","",""));
		
		tickers.add(new Stock("SPY","1XSP500","","",""));
		tickers.add(new Stock("SH","-1XSP500","","",""));
		
		
//		//Downloading data possible stocks from List
//		//Only done once
//		getAssets();
		
		//Populating time series
		populateStocks(beg, end);
		backtest();
		printCSV();
	}

	@SuppressWarnings("static-access")
	private void backtest() {
		double tenAverage = 0;
		double twentyAverage = 0;
		double thirtyAverage = 0;
		
		String currentHolding = null;
		double numberShares = 0.0;
		double cashFromSelling = 0.0;
		
		this.holding = new ArrayList<String>();
		this.value = new ArrayList<Double>();
		this.dates = new ArrayList<Date>();
		
		for(int i = 0; i< this.stockAssets.get(0).getDateTS().length; i++) {
			this.dates.add(this.stockAssets.get(0).getDateTS()[i]);
			
			System.out.println(this.stockAssets.get(0).getAdjustedCloseTS()[i]);
			tenAverage = (1.0-2.0/31.0)*tenAverage + 2.0/31.0*this.stockAssets.get(0).getAdjustedCloseTS()[i];
			twentyAverage =  (1.0-2.0/61.0)*twentyAverage + 2.0/61.0*this.stockAssets.get(0).getAdjustedCloseTS()[i];
			thirtyAverage = (1.0-2.0/91.0)*thirtyAverage + 2.0/91.0*this.stockAssets.get(0).getAdjustedCloseTS()[i];
			
			if(i == 0) {
				numberShares = 1000000.0/(1.005*this.stockAssets.get(0).getAdjustedCloseTS()[i]);
				this.value.add(numberShares*this.stockAssets.get(0).getAdjustedCloseTS()[i]);
				tenAverage = this.stockAssets.get(0).getAdjustedCloseTS()[i];
				twentyAverage = this.stockAssets.get(0).getAdjustedCloseTS()[i];
				thirtyAverage = this.stockAssets.get(0).getAdjustedCloseTS()[i];
				currentHolding = this.stockAssets.get(0).getSymbol();
				this.holding.add(currentHolding);
			}
			
			System.out.println(tenAverage + " " + twentyAverage + " " + thirtyAverage );
			
			if(tenAverage < twentyAverage && twentyAverage < thirtyAverage && currentHolding.equals(this.stockAssets.get(0).getSymbol())) {
				cashFromSelling = numberShares*this.stockAssets.get(0).getAdjustedCloseTS()[i]*0.995;
				numberShares = this.value.get(i)/(this.stockAssets.get(1).getAdjustedCloseTS()[i]*1.005);
				this.value.add(numberShares*this.stockAssets.get(1).getAdjustedCloseTS()[i]);
				currentHolding = this.stockAssets.get(1).getSymbol();
				this.holding.add(currentHolding);
			} else if(tenAverage > twentyAverage && twentyAverage > thirtyAverage && currentHolding.equals(this.stockAssets.get(1).getSymbol())) {
				System.out.println("hehfe");
				cashFromSelling = numberShares*this.stockAssets.get(1).getAdjustedCloseTS()[i]*0.995;
				numberShares = this.value.get(i)/(this.stockAssets.get(0).getAdjustedCloseTS()[i]*1.005);
				this.value.add(numberShares*this.stockAssets.get(0).getAdjustedCloseTS()[i]);
				currentHolding = this.stockAssets.get(0).getSymbol();
				this.holding.add(currentHolding);
			} else {
				if(currentHolding.equals(this.stockAssets.get(0).getSymbol())) {
					this.value.add(numberShares*this.stockAssets.get(0).getAdjustedCloseTS()[i]);
					this.holding.add(currentHolding);
				}
				else if(currentHolding.equals(this.stockAssets.get(1).getSymbol())) {
					this.value.add(numberShares*this.stockAssets.get(1).getAdjustedCloseTS()[i]);
					this.holding.add(currentHolding);
				}
					
			}
		}
	}
	
	private void printCSV() {
		
		java.io.File csv = new java.io.File("specialStrategy.csv");
	    java.io.PrintWriter outfile = null;
		try {
			outfile = new java.io.PrintWriter(csv);
		} catch (FileNotFoundException e) {}
		
		outfile.write("Date,Value,Holding");
		for(int i=0; i < this.dates.size(); i++) {
				outfile.write(DateFormatUtils.format(this.dates.get(i), "yyyy-MM-dd") + "," + this.value.get(i)  + "," + this.holding.get(i) + "\n");
		}

	    outfile.close();
	}

	private void getAssets(){
		try {
			for(Stock stock : tickers) {
				System.out.println(stock.getSymbol());
				String crumb = stockFetcherYahoo.getCrumb(stock.getSymbol());
				if (crumb != null && !crumb.isEmpty()) {
	                System.out.println(String.format("Downloading data to %s", stock.getSymbol()));
	                System.out.println("Crumb: " + crumb);
	        		double[] small = new double[10];
	        		double[] medium = new double[20];
	        		double[] large = new double[30];
	                stockFetcherYahoo.downloadData(stock.getSymbol(), 0, System.currentTimeMillis(),"1d",crumb);
	            } else {
	                System.out.println(String.format("Error retreiving data for %s", stock.getSymbol()));
	            }
				
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	private void populateStocks(Date beg, Date end) {
		stockAssets = new ArrayList<Stock>();
		for(int i = 0; i < tickers.size(); i++) {//stockAssetsName.length; i++) {
			try {
				stockAssets.add(stockFetcherYahoo.getStockTimeSerie(tickers.get(i), beg, end));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
}
