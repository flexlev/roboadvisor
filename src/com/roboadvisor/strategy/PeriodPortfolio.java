package com.roboadvisor.strategy;

import java.util.ArrayList;
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

	public void optimize() {
		Calcfc calcfc = new Calcfc() {
			
			private RealMatrix cov;

			public double Compute(int n, int m, double[] x, double[] con) {
				double sum = 0;
				double[][] cova = cov.getData();
				double sigma = Math.sqrt(dot(multiply(x,cova),x));
				
				for(int i =0; i<n; i++) {
					sum += Math.pow(x[i] - Math.pow(sigma,2)/(matrixMultReturnIndex(cova,x,i)*n),2);
				}
				
				double sumWeight = 0;
				for(int i = 0; i<m-1 ; i++) {
					con[i] = x[i];
					sumWeight += x[i];
				}
				con[m-1] = 1000000 - sumWeight;
				
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
		CobylaExitStatus result = Cobyla.FindMinimum(calcfc, this.stocks.size(), this.stocks.size()+1, xO, 100, 0.01, 1, 10000);
	}
	
	public String toString() {
		String str = "First Date is : " + this.dates.get(0);
		return str;
		
	}

	

}
