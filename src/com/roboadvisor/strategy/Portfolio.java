package com.roboadvisor.strategy;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
import org.apache.commons.math3.linear.SingularValueDecomposition;
import org.apache.commons.math3.stat.correlation.Covariance;

import com.joptimizer.functions.ConvexMultivariateRealFunction;
import com.roboadvisor.stockapi.Stock;

@SuppressWarnings("deprecation")
public class Portfolio {
	
	private double[][] holdings;
	
	private ArrayList<PeriodPortfolio> periodPortfolio;
	
	private ArrayList<Stock> elligibleStockAssetsCAD;
	private ArrayList<Stock> elligibleStockAssetsUS;
	private ArrayList<Stock> mandatoryStockAssets;
	
	private ArrayList<RealMatrix> covs;
	private ArrayList<Date> rebalanceDates;
	
	private Cobyla cobyla;
	
	private ArrayList<Double> monthlyReturns;
	
	public Portfolio(ArrayList<Stock> stocks) {
		this.periodPortfolio = new ArrayList<PeriodPortfolio>();
		this.elligibleStockAssetsCAD = new ArrayList<Stock>();
		this.elligibleStockAssetsUS = new ArrayList<Stock>();
		this.mandatoryStockAssets = new ArrayList<Stock>();
		
		//initialize hioldings
		for(int i = 0; i< stocks.size(); i++) {
			if(stocks.get(i).getSector().contains("Fixed Income ETF"))
				this.mandatoryStockAssets.add(stocks.get(i));
			else if(stocks.get(i).getCountry().equals("US")) {
				this.elligibleStockAssetsUS.add(stocks.get(i));
			} else {
				this.elligibleStockAssetsCAD.add(stocks.get(i));
			}
				
		}
	}
	
	public void printInfo() {
		for(int i = 0; i<this.periodPortfolio.size(); i++) {
			for(int j=0; j<this.periodPortfolio.get(i).getStocks().size(); j++) {
				System.out.println(this.periodPortfolio.get(i).getStocks().get(j).getSector());
			}
			System.out.println(Arrays.toString(this.periodPortfolio.get(i).getDates().toArray()));
		}
	}
	
	//create for every month cov matrixes, stocks assets by looking at performance from all the possible Stocks
	public void populateSeries() {
		//get the rebalance Dates stating at 
		this.rebalanceDates = getRebalanceDates();
		
		for(int i = 0 ; i< this.rebalanceDates.size()-7 ; i++) {
			periodPortfolio.add(new PeriodPortfolio(
					getBestPrevious6Months(this.rebalanceDates.get(i), this.rebalanceDates.get(i+6),"CAD"),
					getBestPrevious6Months(this.rebalanceDates.get(i), this.rebalanceDates.get(i+6),"US"),
					this.mandatoryStockAssets,
					getInBetweenDates(this.rebalanceDates.get(i+6), this.rebalanceDates.get(i+7))));
			
//			if(i ==0) {
//				System.out.println("Dates in between : " + Arrays.toString(getInBetweenDates(this.rebalanceDates.get(i+6), this.rebalanceDates.get(i+7)).toArray()));
//				System.out.println();
//				//				System.out.println("Best stocks : " + Arrays.toString(getBestPrevious6Months(this.rebalanceDates.get(i), this.rebalanceDates.get(i+6),"US").toArray()));
//			}
				
				
		}
		
	}

	private ArrayList<Date> getInBetweenDates(Date beg, Date end) {
		ArrayList<Date> dates = new ArrayList<Date>();
		for(int i =0; i<this.mandatoryStockAssets.get(0).getDateTS().length; i++) {
			if(this.mandatoryStockAssets.get(0).getDateTS()[i].before(beg) || this.mandatoryStockAssets.get(0).getDateTS()[i].after(end))
				;
			else
				dates.add(this.mandatoryStockAssets.get(0).getDateTS()[i]);
		}
		return dates;
	}

	private ArrayList<Stock> getBestPrevious6Months(Date beg, Date end, String specificity) {
		
		ArrayList<Stock> inventory = null;
		if(specificity.equals("US"))
			inventory = this.elligibleStockAssetsUS;
		if(specificity.equals("CAD"))
			inventory = this.elligibleStockAssetsCAD;
		
		ArrayList<Stock> bestStocks = new ArrayList<Stock>();
		//best CAD
		StockPair[] performance = new StockPair[inventory.size()];
		for(int i =0; i< inventory.size(); i++) {
			performance[i] = new StockPair(inventory.get(i),
											getPreviousPeriodPerformanceStock(inventory.get(i), beg, end));
		}
		
		//get best CAD % performer
		Arrays.sort(performance);
		//Threshold by Industry
		
		return getTopStocks(performance,3);
	}

	private ArrayList<Stock> getTopStocks(StockPair[] performance, int n) {
		ArrayList<Stock> tops = new ArrayList<Stock>();
		Map<String, Integer> sectors = new HashMap<String, Integer>();
		
		for(int i = 0; i< performance.length; i++) {
			sectors.put(performance[i].stock.getSector(), 0);
		}
		
		for(int j = 0; j <sectors.size(); j++) {
			for(int i = 0; i< performance.length; i++) {
				if((sectors.get(performance[i].stock.getSector()) < n) && (!tops.contains(performance[i].stock)) && (performance[i].stock.getMarketCap() > 1000000)) {
					tops.add(performance[i].stock);
					sectors.merge(performance[i].stock.getSector(), 1, Integer::sum);
				}
			}
		}
			
		return tops;
	}

	//by average performance
	private Double getPreviousPeriodPerformanceStock(Stock stock, Date beg, Date end) {
		int indexBeg = getArrayIndex(stock.getDateTS(), beg);
		int indexEnd = getArrayIndex(stock.getDateTS(), end);
		
		return stock.getAdjustedCloseTS()[indexEnd]/stock.getAdjustedCloseTS()[indexBeg] - 1;
	}
	
	private int getArrayIndex(Date[] dates,Date date) {
        int k=0;
        for(int i=0;i<dates.length;i++){
            if(dates[i].equals(date)){
                k=i;
                break;
            }
        }
        return k;
	}

	private ArrayList<Date> getRebalanceDates() {
		ArrayList<Date> dates = new ArrayList<Date>();
		
		Calendar cal = Calendar.getInstance();
		int month = cal.get(Calendar.MONTH);
		int currentMonth = 4;
		
		for(int i=0; i<this.mandatoryStockAssets.get(0).getDateTS().length; i++) {
			cal.setTime(this.mandatoryStockAssets.get(0).getDateTS()[i]);
			if(currentMonth != cal.get(Calendar.MONTH)) {
				dates.add(this.mandatoryStockAssets.get(0).getDateTS()[i]);
				currentMonth =  cal.get(Calendar.MONTH);
			}	
		}
		
		return dates;
	}

	public void optimizeWeight() {
		double initialValue = 1000000;
		for(int i = 0; i< this.periodPortfolio.size(); i++) {
			initialValue = this.periodPortfolio.get(i).optimize(initialValue);
		}
	}
	
	public void printCSV() {
		double value = 0;
		Date date = new Date();
		ArrayList<String> holdings = new ArrayList<String>();
		
		java.io.File csv = new java.io.File("portfolio.csv");
	    java.io.PrintWriter outfile = null;
		try {
			outfile = new java.io.PrintWriter(csv);
		} catch (FileNotFoundException e) {}
		
		for(int i=0; i < this.periodPortfolio.size(); i++) {
			for(int j =0; j<this.periodPortfolio.get(i).getDates().size(); j++) {
				date = this.periodPortfolio.get(i).getDates().get(j);
				value = this.periodPortfolio.get(i).getValue().get(j);
				for(int m = 0; m< this.periodPortfolio.get(i).getStocks().size(); m++) {
					holdings.add(this.periodPortfolio.get(i).getStocks().get(m).getSymbol());
				}
				outfile.write(date + "," + value + "," + Arrays.toString(holdings.toArray()) + "\n");
				holdings.clear();
			}
		}

	    outfile.close();
	}

	public double[][] getHoldings() {
		return holdings;
	}

	public void setHoldings(double[][] holdings) {
		this.holdings = holdings;
	}

	public ArrayList<Double> getMonthlyReturns() {
		return monthlyReturns;
	}

	public void setMonthlyReturns(ArrayList<Double> monthlyReturns) {
		this.monthlyReturns = monthlyReturns;
	}

	public Cobyla getCobyla() {
		return cobyla;
	}

	public void setCobyla(Cobyla cobyla) {
		this.cobyla = cobyla;
	}
	
}
