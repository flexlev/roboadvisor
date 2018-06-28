package com.roboadvisor.stresstesting;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.stream.Stream;

import org.apache.commons.lang3.ArrayUtils;

import com.roboadvisor.stockapi.StockHelper;
import com.roboadvisor.strategy.PeriodPortfolio;
import com.roboadvisor.strategy.Portfolio;
import com.roboadvisor.strategy.PortfolioAllocation;

public class PortfolioCSV {
	
	private Portfolio portfolio;
	
	private PortfolioAllocation portfolioAllocation;
	

	public static void main(String[] args) {
		PortfolioCSV portfolioCSV = new PortfolioCSV("portfolio3.csv");
		portfolioCSV.printPortfolio();
	}

	public PortfolioCSV(String portfolioCSV) {
		Date beg = new GregorianCalendar(2003, 05, 02).getTime();
		Date end = new GregorianCalendar(2018, 05, 02).getTime();
		
		this.portfolioAllocation = new PortfolioAllocation();
		this.portfolio = new Portfolio(this.portfolioAllocation.getStockAssets(), 1);
		this.portfolio.setRebalanceDates(this.portfolio.getRebalanceDates());
		
		populatePeriodPortfolio(portfolioCSV);
	}

	private void populatePeriodPortfolio(String portfolioCSV) {
		//populate periodPortfolio keep in mind duplicates
		BufferedReader br;

		Date currentDate = null;
		Date nextDate = null;
		
		ArrayList<PeriodPortfolio> periodPortfolio = new ArrayList<PeriodPortfolio>();
		
		ArrayList<String> tickersTemp = new ArrayList<String>();
		ArrayList<Double> weightsTemp = new ArrayList<Double>();
		ArrayList<Double> initialValueTemp = new ArrayList<Double>();
		ArrayList<Date> datesTemp = new ArrayList<Date>();
		
		try {
			br = new BufferedReader(new FileReader(portfolioCSV));
			Stream<String> lines = br.lines();
			String[] stringArray = lines.toArray(String[]::new);
			
			for(int i =0; i<stringArray.length-1 ;i++ ) {
				String[] split0 = stringArray[i].split(",");
				currentDate = StockHelper.createDate(split0[0]);
				initialValueTemp.add(Double.parseDouble(split0[1]));
				
				datesTemp.add(currentDate);
						
				if(i != (stringArray.length-2)) {
					String[] split2 = stringArray[i+2].split(",");
					nextDate = StockHelper.createDate(split2[0]);
				}
				
				if(currentDate.equals(nextDate) || (i == (stringArray.length-2))) {
					for(int j = 2; j<split0.length ; j++) {
						tickersTemp.add(split0[j].replace("[", "").replace("]", ""));
					}
					
					String[] split1 = stringArray[i+1].split(",");
					for(int j = 2; j<split1.length ; j++) {
						weightsTemp.add(Double.parseDouble(split1[j].replace("[", "").replace("]", "")));
					}
					
					this.portfolio.getPeriodPortfolio().add(new PeriodPortfolio(tickersTemp, weightsTemp, datesTemp, initialValueTemp, 1));
					
					currentDate = null;
					nextDate = null;
					
					initialValueTemp.clear();
					weightsTemp.clear();
					tickersTemp.clear();
					datesTemp.clear();
				}
					
				i++;
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
	}
	
	private void printPortfolio() {
		for(int i =0; i <this.portfolio.getPeriodPortfolio().size() ; i++) {
			System.out.println(this.portfolio.getPeriodPortfolio().get(i));
		}
		
	}
}
