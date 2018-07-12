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
		Date beg = new GregorianCalendar(2003, 05, 02).getTime();
		Date end = new GregorianCalendar(2018, 05, 02).getTime();
		
		stockFetcherYahoo = new StockFetcherYahoo(beg, end);
		tickers = stockFetcherYahoo.getAllTickers();
		
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
		
	}
	
	public static void main(String[] args) throws MalformedURLException, IOException {
		new PortfolioAllocation();
		
		Portfolio portfolio1 = new Portfolio(stockAssets,1);
		portfolio1.populateSeries();
		portfolio1.optimizeWeight(0);
		portfolio1.printCSV();
		
		Portfolio portfolio2 = new Portfolio(stockAssets,2);
		portfolio2.populateSeries();
		portfolio2.optimizeWeight(1);
		portfolio2.printCSV();
		
		Portfolio portfolio3 = new Portfolio(stockAssets,3);
		portfolio3.populateSeries();
		portfolio3.optimizeWeight(2);
		portfolio3.printCSV();
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
				{"Technology US", "Health Care US", "Consumer Services US", "Capital Goods US", "Consumer Durables US", "Finance US", 
				"Consumer Non-Durables US", "Public Utilities US", "Basic Industries US", "Energy US", "Transportation US",
				"Clean Technology CAD","Consumer Products & Services CAD","Financial Services CAD","Technology CAD","Industrial Products & Services CAD",
				"Life Sciences CAD","Oil & Gas CAD","Real Estate CAD","Mining CAD","Utilities & Pipelines CAD","Comm & Media CAD",
				"Fixed Income ETF US", "Fixed Income ETF CAD"};
		String filename = null;
		
		int counter = 0;
		
		ArrayList<Stock> listDiversifiedStock = new ArrayList<Stock>();
		
		for(String sector : sectors) {
//			System.out.println("Finding Assets for : " + sector);
			counter = 0;
			for(Stock stock: tickers) {
				//By Sector
				if(stock.getSector().equals(sector)) {
		    		counter++;
		    		listDiversifiedStock.add(stock);
		    	}
			}
		}
		
		tickers = listDiversifiedStock;
	}

	private void populateStocks(Date beg, Date end) {
		stockAssets = new ArrayList<Stock>();
		for(int i = 0; i < tickers.size(); i++) {//stockAssetsName.length; i++) {
			try {
				stockAssets.add(stockFetcherYahoo.getStockTimeSerie(tickers.get(i), beg, end));
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
	                stockFetcherYahoo.downloadData(stock.getSymbol(), 0, System.currentTimeMillis(), "1wk", crumb);
	            } else {
	                System.out.println(String.format("Error retreiving data for %s", stock.getSymbol()));
	            }
				
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public StockFetcherYahoo getStockFetcherYahoo() {
		return stockFetcherYahoo;
	}

	public void setStockFetcherYahoo(StockFetcherYahoo stockFetcherYahoo) {
		this.stockFetcherYahoo = stockFetcherYahoo;
	}

	public static ArrayList<Stock> getStockAssets() {
		return stockAssets;
	}

	public static void setStockAssets(ArrayList<Stock> stockAssets) {
		PortfolioAllocation.stockAssets = stockAssets;
	}

	public static ArrayList<Stock> getTickers() {
		return tickers;
	}

	public static void setTickers(ArrayList<Stock> tickers) {
		PortfolioAllocation.tickers = tickers;
	}

	public static Portfolio getPortfolio() {
		return portfolio;
	}

	public static void setPortfolio(Portfolio portfolio) {
		PortfolioAllocation.portfolio = portfolio;
	}
	
	
}
