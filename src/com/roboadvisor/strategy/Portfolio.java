package com.roboadvisor.strategy;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
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
		periodPortfolio = new ArrayList<PeriodPortfolio>();
		elligibleStockAssetsCAD = new ArrayList<Stock>();
		elligibleStockAssetsUS = new ArrayList<Stock>();
		mandatoryStockAssets = new ArrayList<Stock>();
		
		//initialize hioldings
		for(int i = 0; i< stocks.size(); i++) {
			if(stocks.get(i).getSector().contains("Fixed Income ETF"))
				this.mandatoryStockAssets.add(stocks.get(i));
			else if(stocks.get(i).getCountry().equals("US"))
				this.elligibleStockAssetsUS.add(stocks.get(i));
			else
				this.elligibleStockAssetsCAD.add(stocks.get(i));
		}
	}
	
	//create for every month cov matrixes, stocks assets by looking at performance from all the possible Stocks
	public void populateSeries() {
		//get the rebalance Dates stating at 
		this.rebalanceDates = getRebalanceDates();
//		System.out.println(Arrays.toString(this.rebalanceDates.toArray()));
		
		for(int i = 0 ; i< this.rebalanceDates.size()-6 ; i++) {
			periodPortfolio.add(new PeriodPortfolio(
					getBestPrevious6Months(this.rebalanceDates.get(i), this.rebalanceDates.get(i+6),"CAD"),
					getBestPrevious6Months(this.rebalanceDates.get(i), this.rebalanceDates.get(i+6),"US"),
					this.mandatoryStockAssets,
					getInBetweenDates(this.rebalanceDates.get(i), this.rebalanceDates.get(i+6))));
		}
		
	}

	private ArrayList<Date> getInBetweenDates(Date beg, Date end) {
		ArrayList<Date> dates = new ArrayList<Date>();
		for(int i =0; i<this.rebalanceDates.size(); i++) {
			if(this.rebalanceDates.get(i).before(beg) || this.rebalanceDates.get(i).after(end))
				;
			else
				dates.add(this.rebalanceDates.get(i));
		}
		return dates;
	}

	private ArrayList<Stock> getBestPrevious6Months(Date beg, Date end, String specificity) {
		
		ArrayList<Stock> inventory;
		if(specificity.equals("US"))
			inventory = this.elligibleStockAssetsUS;
		if(specificity.equals("CAD"))
			inventory = this.elligibleStockAssetsUS;
		
		ArrayList<Stock> bestStocks = new ArrayList<Stock>();
		
		//best CAD
		StockPair[] performance = new StockPair[this.elligibleStockAssetsCAD.size()];
		for(int i =0; i< this.elligibleStockAssetsCAD.size(); i++) {
			performance[i] = new StockPair(this.elligibleStockAssetsCAD.get(i),
											getPreviousPeriodPerformanceStock(this.elligibleStockAssetsCAD.get(i), beg, end));
		}
		
		//get best CAD % performer
		Arrays.sort(performance);
		
		//Threshold %
		return getTopStocks(performance,30);
	}

	private ArrayList<Stock> getTopStocks(StockPair[] performance, int n) {
		ArrayList<Stock> tops = new ArrayList<Stock>();
		int max = performance.length > n ? n : performance.length;
		for(int i = 0; i< max; i++) {
			tops.add(performance[i].stock);
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
		for(int i = 0; i< this.periodPortfolio.size(); i++) {
			this.periodPortfolio.get(i).optimize();
		}
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
