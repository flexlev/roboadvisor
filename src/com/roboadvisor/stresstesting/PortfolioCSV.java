package com.roboadvisor.stresstesting;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.stream.Stream;

import org.apache.commons.lang3.ArrayUtils;

import com.roboadvisor.dao.GeneralDaoService;
import com.roboadvisor.stockapi.Stock;
import com.roboadvisor.stockapi.StockHelper;
import com.roboadvisor.strategy.PeriodPortfolio;
import com.roboadvisor.strategy.Portfolio;
import com.roboadvisor.strategy.PortfolioAllocation;

public class PortfolioCSV {
	
	private Portfolio portfolio;
	
	private PortfolioAllocation portfolioAllocation;
	
	private static GeneralDaoService generalDaoService;

	public static void main(String[] args) {
		PortfolioCSV portfolioCSV = new PortfolioCSV("portfolio1.csv");
//		portfolioCSV.printPortfolio();
		generalDaoService = new GeneralDaoService();
//		portfolioCSV.saveHoldings(portfolioCSV.portfolio.getPeriodPortfolio().size()-1, "growth");
		portfolioCSV.countAssets();
	}

	public PortfolioCSV(String portfolioCSV) {
		Date beg = new GregorianCalendar(2003, 05, 02).getTime();
		Date end = new GregorianCalendar(2018, 05, 02).getTime();
		
		this.portfolioAllocation = new PortfolioAllocation();
		this.portfolio = new Portfolio(this.portfolioAllocation.getStockAssets(),1);
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
						tickersTemp.add(split0[j].replace("[", "").replace("]", "").replaceAll(" ", ""));
					}
					
					String[] split1 = stringArray[i+1].split(",");
					for(int j = 2; j<split1.length ; j++) {
						weightsTemp.add(Double.parseDouble(split1[j].replace("[", "").replace("]", "")));
					}
					
					this.portfolio.getPeriodPortfolio().add(new PeriodPortfolio(tickersTemp, weightsTemp, datesTemp, initialValueTemp,getAssets(tickersTemp)));
					
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
	
	private ArrayList<Stock> getAssets(ArrayList<String> tickersTemp) {
		
		ArrayList<Stock> stocks = new ArrayList<Stock>();
		
		for(int j =0; j< this.portfolio.getElligibleStockAssetsCAD().size() ; j++) {
			for(int m = 0; m <tickersTemp.size(); m++) {
				if(this.portfolio.getElligibleStockAssetsCAD().get(j).getSymbol().equals(tickersTemp.get(m)))
					stocks.add(this.portfolio.getElligibleStockAssetsCAD().get(j));
			}
		}
		
		for(int j =0; j< this.portfolio.getElligibleStockAssetsUS().size() ; j++) {
			for(int m = 0; m <tickersTemp.size(); m++) {
				if(this.portfolio.getElligibleStockAssetsUS().get(j).getSymbol().equals(tickersTemp.get(m)))
					stocks.add(this.portfolio.getElligibleStockAssetsUS().get(j));
			}
		}
		
		for(int j =0; j< this.portfolio.getMandatoryStockAssets().size() ; j++) {
			for(int m = 0; m <tickersTemp.size(); m++) {
				if(this.portfolio.getMandatoryStockAssets().get(j).getSymbol().equals(tickersTemp.get(m)))
					stocks.add(this.portfolio.getMandatoryStockAssets().get(j));
			}
		}
		return stocks;
	}

	private void printPortfolio() {
		for(int i =0; i <this.portfolio.getPeriodPortfolio().size() ; i++) {
			System.out.println(this.portfolio.getPeriodPortfolio().get(i));
		}
	}
	
	private void saveHoldings(int index, String name) {
		double weight = 0.0;
		for(int i =0; i <this.portfolio.getPeriodPortfolio().get(index).getTickers().size() ; i++) {
			weight = Double.parseDouble(new DecimalFormat("##.####").format(this.portfolio.getPeriodPortfolio().get(index).getWeights().get(i)*100));
			generalDaoService.saveStock(this.portfolio.getPeriodPortfolio().get(index).getTickers().get(i), weight, name);
		}
	}
	
	private void countAssets() {
		ArrayList<String> tickUsed = new ArrayList<String>();
		int n = 0;
		for(int i =0; i <this.portfolio.getPeriodPortfolio().size() ; i++) {
			for(int j =0; j<this.portfolio.getPeriodPortfolio().get(i).getTickers().size(); j++) {
				if(tickUsed.contains(this.portfolio.getPeriodPortfolio().get(i).getTickers().get(j))) {
				} else {
					tickUsed.add(this.portfolio.getPeriodPortfolio().get(i).getTickers().get(j));
					n++;
				}
				
			}
		}
		System.out.println("Number of Unique Assets : " + n);
	}

	public Portfolio getPortfolio() {
		return portfolio;
	}

	public void setPortfolio(Portfolio portfolio) {
		this.portfolio = portfolio;
	}

	public PortfolioAllocation getPortfolioAllocation() {
		return portfolioAllocation;
	}

	public void setPortfolioAllocation(PortfolioAllocation portfolioAllocation) {
		this.portfolioAllocation = portfolioAllocation;
	}
	
	
}
