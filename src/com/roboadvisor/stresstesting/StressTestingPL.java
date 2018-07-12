package com.roboadvisor.stresstesting;

import java.util.Date;
import java.util.GregorianCalendar;

public class StressTestingPL {
	
	private PortfolioCSV portfolioExtractor;

	public static void main(String[] args) {
		Date beg = new GregorianCalendar(2003, 04, 02).getTime();
		Date end = new GregorianCalendar(2018, 05, 02).getTime();
		
		//On the high growth portfolio
		StressTestingPL stressTestingPL = new StressTestingPL("portfolio3.csv");
		stressTestingPL.stress(stressTestingPL.portfolioExtractor.getPortfolio().getPeriodPortfolio().size()-1, beg, end, "portfolio3FXStress");
	}
	
	public StressTestingPL (String csv) {
		portfolioExtractor = new PortfolioCSV(csv);
	}
	
	public void stress(int indexPortfolio,Date beg, Date end, String portfolio) {
		this.portfolioExtractor.getPortfolio().getPeriodPortfolio().get(indexPortfolio).fitToEconomicFactors(beg, end, portfolio);
	}
}
