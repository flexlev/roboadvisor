package com.roboadvisor.strategy;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.stream.Stream;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.stat.correlation.Covariance;

import com.roboadvisor.stockapi.Stock;
import com.roboadvisor.stockapi.StockFetcherAlphaVantage;
import com.roboadvisor.stockapi.StockFetcherYahoo;

//import mosek.fusion.SolutionError;

public class PortfolioAllocation {
	
	// XLK - Technology
	// XLE - Energy
	// XLV - Healthcare
	// VNQ - Real Estate
	// RXI - Automotive
	// RGA - Agriculture
	// XLF - Banking
	
	private StockFetcherYahoo stockFetcherYahoo;
	private static ArrayList<Stock> stockAssets;
	
	private static ArrayList<Stock> tickers;
	
	private static Portfolio portfolio;
	
	public PortfolioAllocation() {
		stockFetcherYahoo = new StockFetcherYahoo();
		tickers = stockFetcherYahoo.getAllTickers();
		
		Date beg = new GregorianCalendar(2003, 05, 02).getTime();
		Date end = new GregorianCalendar(2018, 05, 02).getTime();
		
//		//Downloading data possible stocks from List
//		//Only done once
//		getAssets();
		
		//Remove tickers that couldn't be downloaded
		//Also Removes Stocks not with required time serie length
		removeUndownloadedTickers(beg, end);
		
		//Create Diversified Portfolio
		createDiversifiedPortfolio();
		
		//Populating time series
		populateStocks(beg, end);
		
		Portfolio portfolio = new Portfolio(stockAssets);
		portfolio.populateSeries();
		portfolio.optimizeWeight();
	}
	
	public static void main(String[] args) throws MalformedURLException, IOException {
		new PortfolioAllocation();
	}

	private void removeUndownloadedTickers(Date beg, Date end) {
		String filename = null;
		Iterator<Stock> it = tickers.iterator();
		Stock stock = null;
		Boolean toRemove = false;
		
		while(it.hasNext()) {
			stock = it.next();
			filename = String.format("assets/%s.csv", stock.getSymbol());
			File f = new File(filename);
			
			if(!f.exists()) {
	    		it.remove();
	    		continue;
	    	}
			
			try {
				stockFetcherYahoo.getStockTimeSerie(stock, beg, end);
			} catch (Exception e) {
				toRemove = true;
			}
			
			if(toRemove) {
	    		it.remove();
	    	}
			
			toRemove = false;
		}
	}
	
	private void createDiversifiedPortfolio() {
		//Industry
		String[] sectors = new String[]
				{"Technology", "Health Care", "Consumer Services", "Capital Goods", "Consumer Durables", "Finance", 
				"Miscellaneous", "Consumer Non-Durables", "Public Utilities", "Basic Industries", "Energy", "Transportation", "Fixed Income", 
				"Fixed Income ETF US", "Fixed Income ETF CAD"};
		String filename = null;
		
		int counter = 0;
		
		ArrayList<Stock> listDiversifiedStock = new ArrayList<Stock>();
		
		for(String sector : sectors) {
//			System.out.println("Finding Assets for : " + sector);
			counter = 0;
			for(Stock stock: tickers) {
				//Keeping 5 assets by Industry
				if(stock.getSector().equals(sector)) {
		    		counter++;
		    		listDiversifiedStock.add(stock);
		    	}
				
				if(counter >6)
					break;
			}
		}
		
		tickers = listDiversifiedStock;
	}

	private void populateStocks(Date beg, Date end) {
		
		stockAssets = new ArrayList<Stock>();
		for(int i = 0; i < tickers.size(); i++) {//stockAssetsName.length; i++) {
			try {
				stockAssets.add(stockFetcherYahoo.getStockTimeSerie(tickers.get(i), beg, end));
//				System.out.println(stockAssets.get(i).getWeeklyLogReturn().length + " lines for " + stockAssets.get(i).getSymbol());
			} catch (Exception e) {
				tickers.remove(i);
			}
		}
	}
	
	private void getAssets(){
		try {
			for(Stock stock : tickers) {
				System.out.println(stock.getSymbol());
				String crumb = stockFetcherYahoo.getCrumb(stock.getSymbol());
				if (crumb != null && !crumb.isEmpty()) {
	                System.out.println(String.format("Downloading data to %s", stock.getSymbol()));
	                System.out.println("Crumb: " + crumb);
	                stockFetcherYahoo.downloadData(stock.getSymbol(), 0, System.currentTimeMillis(), crumb);
	            } else {
	                System.out.println(String.format("Error retreiving data for %s", stock.getSymbol()));
	            }
				
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	
	
	
}
