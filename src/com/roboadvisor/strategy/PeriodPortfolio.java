package com.roboadvisor.strategy;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.stream.Stream;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.stat.correlation.Covariance;
import org.apache.commons.math3.stat.regression.OLSMultipleLinearRegression;

import com.roboadvisor.stockapi.Stock;
import com.roboadvisor.stockapi.StockHelper;

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
	private ArrayList<String> tickers;
	
	private ArrayList<Stock> econFactorsUS;
	private ArrayList<Double> portfolioValueMonthlyUS;
	
	private ArrayList<Stock> econFactorsCAD;
	private ArrayList<Double> portfolioValueMonthlyCAD;
	private ArrayList<Date> dateFactors;
	private double[] betaEconFactorsUS;
	private double[] betaEconFactorsCAD;
	
	private double feeCollected;
	
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
		populateTimeSeries(new GregorianCalendar(2003, 05, 02).getTime(),this.dates.get(0));
		
		//Covariance Matrix of Asssets
		this.cov = new Covariance(timeSeries).getCovarianceMatrix();
	}
	
	public PeriodPortfolio(ArrayList<String> tickers, ArrayList<Double> weights, ArrayList<Date> dates, ArrayList<Double> initialValue, ArrayList<Stock> stocks) {
		this.tickers = new ArrayList<String>(tickers);
		this.weights =  new ArrayList<Double>(weights);
		this.dates = new ArrayList<Date>(dates);
		this.value = new ArrayList<Double>(initialValue);
		this.stocks = new ArrayList<Stock>(stocks);
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

	public double optimize(double initialValue, int levelRisk) {
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
				for(int i = 0; i<m-2 ; i++) {
					con[i] = x[i];
					sumWeight += x[i];
				}
				
				//last 6 ETFs max weight of 20 % of whole portfolio
				con[m-2] = 0.4 - (x[n-1] + x[n-2] + x[n-3] + x[n-4] + x[n-5] + x[n-6])/sumWeight;
				con[m-1] = (x[n-1] + x[n-2] + x[n-3] + x[n-4] + x[n-5] + x[n-6])/sumWeight-0.4;
				
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
		this.weights = Cobyla.FindMinimum(calcfc, this.stocks.size(), this.stocks.size()+2, xO, 25000, 100, 1, 10000);
		System.out.println(Arrays.toString(this.weights.toArray()));
		return setValueThroughDates(initialValue, levelRisk);
	}
	
	private void getNumberOfStocks() {
		for(int i = 0; i < this.weights.size(); i++) {
			this.stocksNumber.add(this.initialValue*this.weights.get(i));
		}
	}

	private double setValueThroughDates(double initialValue, int levelRisk) {
		this.value = new ArrayList<Double>();
		this.initialValue = initialValue;
		int index = 0;
		
		double initialValueIterative = this.initialValue;
		double valueBeg = initialValueIterative;
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
			System.out.println("Working on : " + dates.get(i));
		}
		
		if(initialValueIterative>valueBeg) {
			if(levelRisk == 0) {
				this.feeCollected = (initialValueIterative-valueBeg)*0.015 + initialValueIterative*0.005;
			}
			if(levelRisk == 1) {
				this.feeCollected = (initialValueIterative-valueBeg)*0.03 + initialValueIterative*0.005;
			}
			if(levelRisk == 2) {
				this.feeCollected = (initialValueIterative-valueBeg)*0.045 + initialValueIterative*0.005;
			}
		} else {
			if(levelRisk == 0) {
				this.feeCollected = initialValueIterative*0.005;
			}
			if(levelRisk == 1) {
				this.feeCollected = initialValueIterative*0.005;
			}
			if(levelRisk == 2) {
				this.feeCollected = initialValueIterative*0.005;
			}
		}
		
		return initialValueIterative - this.feeCollected;
		
	}
	

	private double dotProduct(ArrayList<Double> stockValue, ArrayList<Double> weights) {
		double dotP = 0;
		for(int i=0; i<stockValue.size(); i++) {
			dotP += stockValue.get(i).doubleValue()*weights.get(i).doubleValue();
		}
		return dotP;
	}
	
	private double dotProduct(double[] v, double[] w) {
		double dotP = 0;
		
		if(v.length != w.length)
			return 0;
		
		for(int i=0; i<v.length; i++) {
			dotP += v[i]*w[i];
		}
		return dotP;
	}
	
	public void fitToEconomicFactors(Date beg, Date end, String portfolio) {
		this.econFactorsUS = new ArrayList<Stock>();
		this.econFactorsCAD = new ArrayList<Stock>();
		
		populateEconomicFactors("Unemployment Rate US", "US", 1, beg, end);
		populateEconomicFactors("Short Interest Rate US", "US", 3, beg, end);
//		populateEconomicFactors("CPI US", "US", 7, beg, end);
		populateEconomicFactors("Long Term Interest rate US", "US", 9, beg, end);
		populateEconomicFactors("Crude Oil", "US", 5, beg, end);
		populateEconomicFactors("S&P 500", "US", 6, beg, end);
		populateEconomicFactors("Exchange Rate", "US", 11, beg, end);
		
		
		populateEconomicFactors("Unemployment Rate Canada", "CAD", 2, beg, end);
		populateEconomicFactors("Short Interest Rate Canada", "CAD", 4, beg, end);
//		populateEconomicFactors("CPI Canada", "CAD", 8, beg, end);
		populateEconomicFactors("Long Term interest rate Canada", "CAD", 10, beg, end);
		populateEconomicFactors("Crude Oil", "CAD", 5, beg, end);
		populateEconomicFactors("TSX", "CAD", 12, beg, end);
		populateEconomicFactors("Exchange Rate", "CAD", 11, beg, end);
		
		createDateFactors();
		fitFactorsLoaded();
		getBetas();
//		createScenarios(0.98, 0.99, 0.99, 1.02, 1.02, 1, "UP"+portfolio);
//		createScenarios(1.02, 1.01, 1.01, 0.98, 0.98, 1, "DOWN"+portfolio);
//		createScenarios(1, 1, 1, 1, 1, 1, "BASE"+portfolio);
		
		createScenarios(1, 1, 1, 1, 1, 0.98, "UP"+portfolio);
		createScenarios(1, 1, 1,1 , 1, 1.02, "DOWN"+portfolio);
		createScenarios(1, 1, 1, 1, 1, 1, "BASE"+portfolio);
	}
	
	private void fitFactorsLoaded() {
		
		this.portfolioValueMonthlyUS = new ArrayList<Double>();
		this.portfolioValueMonthlyCAD = new ArrayList<Double>();
		ArrayList<Double> stockValueUS = new ArrayList<Double>();
		ArrayList<Double> stocksNumberUS = new ArrayList<Double>();
		ArrayList<Double> stockValueCAD = new ArrayList<Double>();
		ArrayList<Double> stocksNumberCAD = new ArrayList<Double>();
		
		int index = 0;
		double initialValueIterativeUS = 1000000;
		double initialValueIterativeCAD = 1000000;
		double stockValue = 0;
		
		System.out.println(Arrays.toString(this.weights.toArray()));
		for(int i = 0; i< this.dateFactors.size(); i++) {
			for(int j =0; j<this.stocks.size(); j++) {
				index = Arrays.asList(stocks.get(j).getDateTS()).indexOf(this.dateFactors.get(i));
				
				//not Factoring Transaction Cost
				if(this.stocks.get(j).getCountry().equals("US")) {
					stockValue = this.stocks.get(j).getAdjustedCloseTS()[index];
					stockValueUS.add(stockValue);
					if(i ==0)
						stocksNumberUS.add(initialValueIterativeUS*this.weights.get(j).doubleValue()/stockValue);
				} else if(this.stocks.get(j).getCountry().equals("CAD")) {
					stockValue = this.stocks.get(j).getAdjustedCloseTS()[index];
					stockValueCAD.add(stockValue);
					if(i ==0)
						stocksNumberCAD.add(initialValueIterativeCAD*this.weights.get(j).doubleValue()/stockValue);
				}
			}
			
			initialValueIterativeUS = dotProduct(stockValueUS,stocksNumberUS);
			initialValueIterativeCAD = dotProduct(stockValueCAD,stocksNumberCAD);
			
			this.portfolioValueMonthlyUS.add(initialValueIterativeUS);
			this.portfolioValueMonthlyCAD.add(initialValueIterativeCAD);
			
			stockValueUS.clear();
			stockValueCAD.clear();
			
//			System.out.println("Working on US: " + this.dateFactors.get(i) + " with Value: " + this.portfolioValueMonthlyUS.get(i));
//			System.out.println("Working on CAD: " + this.dateFactors.get(i) + " with Value: " + this.portfolioValueMonthlyCAD.get(i));
		}
		
	}
	
	private void getBetas() {
		
		this.betaEconFactorsUS = new double[this.econFactorsUS.size()];
		this.betaEconFactorsCAD = new double[this.econFactorsCAD.size()];
		
		double[] yUS = new double[this.portfolioValueMonthlyUS.size()];
		double[][] xUS = new double[this.portfolioValueMonthlyUS.size()][this.econFactorsUS.size()];
		
		double[] yCAD = new double[this.portfolioValueMonthlyCAD.size()];
		double[][] xCAD = new double[this.portfolioValueMonthlyCAD.size()][this.econFactorsCAD.size()];
		
		int index = 0;
		int previousIndex = -1;
		
		for(int i=0; i<this.portfolioValueMonthlyUS.size()-1; i++) {
			yUS[i] = (this.portfolioValueMonthlyUS.get(i+1)/this.portfolioValueMonthlyUS.get(i)-1)*10000;
			yCAD[i] = (this.portfolioValueMonthlyCAD.get(i+1)/this.portfolioValueMonthlyCAD.get(i)-1)*10000;
			
			for(int j=0; j<this.econFactorsUS.size() ; j++) {
				xUS[i][j] = this.econFactorsUS.get(j).getAdjustedCloseTS()[i+1];
				xCAD[i][j] = this.econFactorsCAD.get(j).getAdjustedCloseTS()[i+1];
			}
		} 
		
		OLSMultipleLinearRegression olsUS = new OLSMultipleLinearRegression();
		olsUS.newSampleData(yUS, xUS);
		this.betaEconFactorsUS = olsUS.estimateRegressionParameters();
		
		OLSMultipleLinearRegression olsCAD = new OLSMultipleLinearRegression();
		olsCAD.newSampleData(yCAD, xCAD);
		this.betaEconFactorsCAD = olsCAD.estimateRegressionParameters();
		
		System.out.println(Arrays.toString(this.betaEconFactorsUS));
		System.out.println(Arrays.toString(this.betaEconFactorsCAD));
		
	}
	
	private void createScenarios(double changeUnemploymentRate, double changeShortIR, double changeLongIR, double changeCrudeOil, double changeMarket, double changeExchangeRate, String scenarioOutlook) {
		double[] adjustement = new double[] {changeUnemploymentRate, changeShortIR, changeLongIR, changeCrudeOil, changeMarket, changeExchangeRate};
		
		double[][] scenarioCAD = new double[6][this.econFactorsCAD.size()+1];
		double[][] scenarioUS = new double[6][this.econFactorsUS.size()+1];
		for(int i = 0; i <6; i++) {
			for(int j =0; j<this.econFactorsCAD.size()+1; j++) {
				if(j ==0) {
					scenarioCAD[i][j]= 1;
					scenarioUS[i][j]= 1;
				} else {
					scenarioCAD[i][j]= Math.pow(adjustement[j-1], i)*this.econFactorsCAD.get(j-1).getAdjustedCloseTS()[this.econFactorsCAD.get(j-1).getAdjustedCloseTS().length-1];
					scenarioUS[i][j]= Math.pow(adjustement[j-1], i)*this.econFactorsUS.get(j-1).getAdjustedCloseTS()[this.econFactorsUS.get(j-1).getAdjustedCloseTS().length-1];
				}
				
				System.out.print(scenarioUS[i][j] + " ");
				if(j == this.econFactorsCAD.size())
					System.out.println();
			}
		}
		
		printScenarioOutcome(scenarioCAD, "CAD", scenarioOutlook);
		printScenarioOutcome(scenarioUS, "US", scenarioOutlook);
	}
	
	private void printScenarioOutcome(double[][] scenario, String country, String scenarioOutlook) {
		double[] factors = new double[this.econFactorsCAD.size()+1];
		double value = 0;
		
		java.io.File csv = new java.io.File("scenario"+ scenarioOutlook + country + ".csv");
	    java.io.PrintWriter outfile = null;
		try {
			outfile = new java.io.PrintWriter(csv);
		} catch (FileNotFoundException e) {}
		
		for(int i=0; i < scenario.length; i++) {
			for(int j =0; j<scenario[0].length; j++) {
				factors[j] = scenario[i][j];
			}
			if(country.equals("CAD"))
				value = dotProduct(factors, this.betaEconFactorsCAD);
			else
				value = dotProduct(factors, this.betaEconFactorsUS);
			
			outfile.write(value/10000 +"\n");
		}

	    outfile.close();
		
	}

	private void createDateFactors() {
		this.dateFactors = new ArrayList<Date>();
		Calendar cal = Calendar.getInstance();
		int currentMonth = -1;
		
		for(int i = 0; i<this.stocks.get(0).getDateTS().length; i++) {
			cal.setTime(this.stocks.get(0).getDateTS()[i]);
			if(currentMonth != cal.get(Calendar.MONTH)) {
				currentMonth = cal.get(Calendar.MONTH);
				this.dateFactors.add(this.stocks.get(0).getDateTS()[i]);
			}
		}
	}

	private void populateEconomicFactors(String factorName, String country, int indexToExtract, Date beg, Date end) {
		String asset = "assets/econFactor.csv";
		BufferedReader br;
		
		
		
		ArrayList<Double> prices= new ArrayList<Double>();
		ArrayList<Date> dates= new ArrayList<Date>();
		Date temp = null;
		String price = null;
		
		try {
			br = new BufferedReader(new FileReader(asset));
			Stream<String> lines = br.lines();
			String[] stringArray = lines.toArray(String[]::new);
			
			for(int i =1; i<stringArray.length ;i++ ) {
				String[] split = stringArray[i].split(",");
				temp = StockHelper.createDate(split[0]);
				if(temp.after(beg) && temp.before(end)) {
					price = split[indexToExtract].replaceAll("\"", "");
					prices.add(Double.parseDouble(price));
					dates.add(temp);
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		Date[] datesArray = new Date[dates.size()];
		double[] pricesArray = null;
		
		datesArray = dates.toArray(datesArray);
		pricesArray = ArrayUtils.toPrimitive(prices.toArray(new Double[prices.size()]));
		
		if(country.equals("US"))
			this.econFactorsUS.add(new Stock(factorName, country, pricesArray, datesArray));
		else if(country.equals("CAD"))
			this.econFactorsCAD.add(new Stock(factorName, country, pricesArray, datesArray));
	}

	public String toString() {
		String str = "Tickers : " + Arrays.toString(this.tickers.toArray())  + "\n" +
					"Weights : " + Arrays.toString(this.weights.toArray()) + "\n" + 
					"Dates : " + Arrays.toString(this.dates.toArray()) + "\n" + 
					"Initial Value : " + Arrays.toString(this.value.toArray()) ;
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

	public ArrayList<Stock> getStockAssetsCAD() {
		return stockAssetsCAD;
	}

	public void setStockAssetsCAD(ArrayList<Stock> stockAssetsCAD) {
		this.stockAssetsCAD = stockAssetsCAD;
	}

	public ArrayList<Stock> getStockAssetsUS() {
		return stockAssetsUS;
	}

	public void setStockAssetsUS(ArrayList<Stock> stockAssetsUS) {
		this.stockAssetsUS = stockAssetsUS;
	}

	public ArrayList<Stock> getMandatoryStockAssets() {
		return mandatoryStockAssets;
	}

	public void setMandatoryStockAssets(ArrayList<Stock> mandatoryStockAssets) {
		this.mandatoryStockAssets = mandatoryStockAssets;
	}

	public double[][] getTimeSeries() {
		return timeSeries;
	}

	public void setTimeSeries(double[][] timeSeries) {
		this.timeSeries = timeSeries;
	}

	public RealMatrix getCov() {
		return cov;
	}

	public void setCov(RealMatrix cov) {
		this.cov = cov;
	}

	public double getInitialValue() {
		return initialValue;
	}

	public void setInitialValue(double initialValue) {
		this.initialValue = initialValue;
	}

	public ArrayList<Double> getStocksNumber() {
		return stocksNumber;
	}

	public void setStocksNumber(ArrayList<Double> stocksNumber) {
		this.stocksNumber = stocksNumber;
	}

	public ArrayList<String> getTickers() {
		return tickers;
	}

	public void setTickers(ArrayList<String> tickers) {
		this.tickers = tickers;
	}

	public double getFeeCollected() {
		return feeCollected;
	}

	public void setFeeCollected(double feeCollected) {
		this.feeCollected = feeCollected;
	}
	

}
