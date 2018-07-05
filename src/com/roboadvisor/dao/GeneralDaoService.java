package com.roboadvisor.dao;

import java.util.List;

import com.roboadvisor.datarepository.Holding;
import com.roboadvisor.datarepository.StocksRepository;
import com.roboadvisor.stockapi.Stock;

public class GeneralDaoService {

	private StocksRepository stocksRepository;
	
	public GeneralDaoService() {
		stocksRepository = new StocksRepository();
	}
	
	public void saveStock(String symbol, double weight, String portfolioName) {
		stocksRepository.insertStockByPortfolio(symbol, weight, portfolioName);
	}
	
	public List<Holding> getHoldingsByPortfolio(String portfolioName){
		return stocksRepository.findHoldingsByPortfolio(portfolioName);
	}
}
