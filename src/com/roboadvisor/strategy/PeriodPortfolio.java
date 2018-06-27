package com.roboadvisor.strategy;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.stat.correlation.Covariance;

import com.roboadvisor.stockapi.Stock;

public class PeriodPortfolio {
	
	private ArrayList<Stock> stocks;
	
	private ArrayList<Stock> stockAssetsCAD;
	private ArrayList<Stock> stockAssetsUS;
	private ArrayList<Stock> mandatoryStockAssets;
	
	private double[][] timeSeries;
	private RealMatrix cov;
	private ArrayList<Date> dates;
	
	private double initialValue;
	private ArrayList<Double> weights;
	private ArrayList<Double> stocksNumber;
	private ArrayList<Double> value;
	
	public PeriodPortfolio(ArrayList<Stock> stockAssetsCAD, ArrayList<Stock> stockAssetsUS, ArrayList<Stock> mandatoryStockAssets, ArrayList<Date> dates) {
		this.stockAssetsCAD = stockAssetsCAD;
		this.stockAssetsUS = stockAssetsUS;
		this.mandatoryStockAssets = mandatoryStockAssets;
		this.dates = dates;
		
		this.stocks = new ArrayList<Stock>();
		this.stocks.addAll(this.stockAssetsCAD);
		this.stocks.addAll(this.stockAssetsUS);
		this.stocks.addAll(this.mandatoryStockAssets);
		
		//populate timeSeries for Covariance Matrix by Dates
		populateTimeSeries(this.dates.get(0),this.dates.get(this.dates.size()-1));
		
		//Covariance Matrix of Asssets
		this.cov = new Covariance(timeSeries).getCovarianceMatrix();
	}
	
	//TO VERIFY
	private void populateTimeSeries(Date beg, Date end) {
		int begIndex = getArrayIndex(this.stocks.get(0).getDateTS(),beg);
		int endIndex = getArrayIndex(this.stocks.get(0).getDateTS(),end);
		
		timeSeries = new double[endIndex-begIndex+1][this.stocks.size()];
		for (int col=0; col<this.stocks.size(); col++){
		    for (int row=begIndex; row<(endIndex+1); row++){
		        timeSeries[row-begIndex][col] = this.stocks.get(col).getWeeklyLogReturn()[row];
		    }
		}
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

	public double optimize(double initialValue) {
		Calcfc calcfc = new Calcfc() {
			
			private RealMatrix cov;
			//https://docs.mosek.com/MOSEKModelingCookbook-a4paper.pdf
			public double Compute(int n, int m, double[] x, double[] con) {
				double sum = 0;
				double[][] cova = cov.getData();
				double sigma = Math.sqrt(dot(multiply(x,cova),x));
				double c = 10;
				
//				for(int i =0; i<n; i++) {
//					sum += Math.pow(x[i] - Math.pow(sigma,2)/(matrixMultReturnIndex(cova,x,i)*n),2);
//				}
				
				for(int i =0; i<n; i++) {
					sum -= c*Math.log(x[i]);
				}
				sum += sigma;
				
				double sumWeight = 0;
				for(int i = 0; i<m-1 ; i++) {
					con[i] = x[i];
					sumWeight += x[i];
				}
				
				//last 6 ETFs max weight of 20 % of whole portfolio
				con[m-1] = 0.2 - (x[n-1] + x[n-2] + x[n-3] + x[n-4] + x[n-5] + x[n-6])/sumWeight;
				
				return sum;
			}

			// vector-matrix multiplication (y = x^T A)
		    public double[] multiply(double[] x, double[][] a) {
		        int m = a.length;
		        int n = a[0].length;
		        if (x.length != m) 
		        	throw new RuntimeException("Illegal matrix dimensions.");
		        
		        double[] y = new double[n];
		        for (int j = 0; j < n; j++)
		            for (int i = 0; i < m; i++)
		                y[j] += a[i][j] * x[i];
		        return y;
		    }
			
		    public double matrixMultReturnIndex(double[][] a, double[] x, int r) {
		        int m = a.length;
		        int n = a[0].length;
		        if (x.length != n) throw new RuntimeException("Illegal matrix dimensions.");
		        double[] y = new double[m];
		        for (int i = 0; i < m; i++)
		            for (int j = 0; j < n; j++)
		                y[i] += a[i][j] * x[j];
		        return y[r];
		    }
			
			private double dot(double[] x, double[] y) {
				if (x.length != y.length) 
					throw new RuntimeException("Illegal vector dimensions.");
				
		        double sum = 0.0;
		        for (int i = 0; i < x.length; i++)
		            sum += x[i] * y[i];
		        return sum;
		    }
			
			public void setCov(RealMatrix cov) {
				this.cov = cov;
			}
		};
		
		double[] xO = new double[this.stocks.size()];
		for(int i =0; i < this.stocks.size(); i++) {
			xO[i] = 1000000/this.stocks.size();
		}
		
		calcfc.setCov(this.cov);
		this.weights = Cobyla.FindMinimum(calcfc, this.stocks.size(), this.stocks.size()+1, xO, 100000, 100, 1, 10000);
//		System.out.println(Arrays.toString(this.weights.toArray()));
		return setValueThroughDates(initialValue);
	}
	
	private void getNumberOfStocks() {
		for(int i = 0; i < this.weights.size(); i++) {
			this.stocksNumber.add(this.initialValue*this.weights.get(i));
		}
	}

	private double setValueThroughDates(double initialValue) {
		this.value = new ArrayList<Double>();
		this.initialValue = initialValue;
		int index = 0;
		
		double initialValueIterative = this.initialValue;
		ArrayList<Double> stockValue = new ArrayList<Double>();
		ArrayList<Double> stocksNumber = new ArrayList<Double>();
		
		for(int i = 0; i< dates.size(); i++) {
			for(int j =0; j<stocks.size(); j++) {
				index = Arrays.asList(stocks.get(j).getDateTS()).indexOf(dates.get(i));
				//Factor Transaction Cost
				if(i==0)
					stockValue.add(stocks.get(j).getAdjustedCloseTS()[index]*1.005);
				else if (i == (dates.size()-1))
					stockValue.add(stocks.get(j).getAdjustedCloseTS()[index]*0.995);
				else	
					stockValue.add(stocks.get(j).getAdjustedCloseTS()[index]);
				if(i ==0)
					stocksNumber.add(initialValueIterative*this.weights.get(j).doubleValue()/stockValue.get(j));
			}
			
			initialValueIterative = dotProduct(stockValue,stocksNumber);
			this.value.add(initialValueIterative);
			stockValue.clear();
			System.out.println(initialValueIterative);
		}
		
		return initialValueIterative;
		
	}

	private double dotProduct(ArrayList<Double> stockValue, ArrayList<Double> weights) {
		double dotP = 0;
		for(int i=0; i<stockValue.size(); i++) {
			dotP += stockValue.get(i).doubleValue()*weights.get(i).doubleValue();
		}
		return dotP;
	}

	public String toString() {
		String str = "First Date is : " + this.dates.get(0);
		return str;
	}

	public ArrayList<Stock> getStocks() {
		return stocks;
	}

	public void setStocks(ArrayList<Stock> stocks) {
		this.stocks = stocks;
	}

	public ArrayList<Date> getDates() {
		return dates;
	}

	public void setDates(ArrayList<Date> dates) {
		this.dates = dates;
	}

	public ArrayList<Double> getWeights() {
		return weights;
	}

	public void setWeights(ArrayList<Double> weights) {
		this.weights = weights;
	}

	public ArrayList<Double> getValue() {
		return value;
	}

	public void setValue(ArrayList<Double> value) {
		this.value = value;
	}
	

}
