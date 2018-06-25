package com.roboadvisor.stockapi;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

public class StockFetcherAlphaVantage {
	/*
	* Returns a Stock Object that contains info about a specified stock.
	* @param 	symbol the company's stock symbol
	* @return 	a stock object containing info about the company's stock
	* @see Stock
	*XYQUX0URG1IBU4PA API key from alphavantage
	*/
	
	private static final String KEY = "XYQUX0URG1IBU4PA";
	
	public static void main(String[] args) throws MalformedURLException, IOException {
		StockFetcherAlphaVantage sf = new StockFetcherAlphaVantage();
		Stock stock = sf.getStockWeekly("XLK");
		System.out.println(stock.getSymbol());
		System.out.println(Arrays.toString(stock.getDateTS()));
		System.out.println(Arrays.toString(stock.getAdjustedCloseTS()));
	}
	
	public static Stock getStockWeekly(String symbol) throws MalformedURLException, IOException {  
		Stock stock= null;
		String baseurl = new String("https://www.alphavantage.co/query?function=TIME_SERIES_WEEKLY_ADJUSTED&symbol="+symbol+"&apikey="+KEY+"&datatype=csv");
		URL url = new URL(baseurl);
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		if (connection.getResponseCode() == 200) {
			InputStreamReader streamReader = new InputStreamReader(connection.getInputStream());
			BufferedReader br = new BufferedReader(streamReader);
			Stream<String> lines = br.lines();
			Stream<String> data = lines.skip(1);
			stock = getStockTimeSerie(data, symbol);
		}
		return stock;
	}
	
	private static Stock getStockTimeSerie(Stream<String> data, String symbol){
		String[] stringArray = data.toArray(String[]::new);
		double[] prices = new double[stringArray.length];
		Date[] dates = new Date[stringArray.length];
		for(int i =0; i<stringArray.length ;i++ ) {
			String[] split = stringArray[i].split(",");
			prices[i] = Double.parseDouble(split[5]);
			dates[i] = createDate(split[0]);
		}
		return new Stock(symbol, prices, dates);
	}

	private static Date createDate(String date) {
	     try {
	         return new SimpleDateFormat("yyyy-MM-dd").parse(date);
	     } catch (ParseException e) {
	         return null;
	     }
	}
	 
}
