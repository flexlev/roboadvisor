package com.roboadvisor.stockapi;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Pattern;

public final class StockHelper {

	public StockHelper() {}
	
	public double handleDouble(String x) {
		Double y;
		if (Pattern.matches("N/A", x)) {  
			y = 0.00;   
		} else { 
			y = Double.parseDouble(x);  
		}  
		return y;
	}
	
	public int handleInt(String x) {
		int y;
		if (Pattern.matches("N/A", x)) {  
			y = 0;   
		} else { 
			y = Integer.parseInt(x);  
		} 
		return y;
	}

	public static double findNominalMarketCap(String marketCap) {
		double mC = 0;
		try { 
			if(marketCap.contains("B")) {
				mC =  Double.valueOf(marketCap.substring(2, marketCap.length()-2)) * 1000000000;
			} else if (marketCap.contains("M")) {
				mC = Double.valueOf(marketCap.substring(2, marketCap.length()-2)) * 1000000;
			} else {
				mC = Double.valueOf(marketCap);
			}
		} catch (NumberFormatException | StringIndexOutOfBoundsException e) {}
		
		return mC;
	}
	
	public static Date createDate(String date) {
	     try {
	    	 if (date.contains("-"))
	         	return new SimpleDateFormat("yyyy-MM-dd").parse(date);
	    	 else
	 			return new SimpleDateFormat("dd/MM/yyyy").parse(date);
	     } catch (ParseException e) {
	        return null;
	     }
	}
	
}
