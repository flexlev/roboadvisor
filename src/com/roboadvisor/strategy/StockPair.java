package com.roboadvisor.strategy;

import com.roboadvisor.stockapi.Stock;

public class StockPair implements Comparable<StockPair>{

	public final Stock stock;
    public final double value;
    public double weight;

    public void setWeight(double weight) {
		this.weight = weight;
	}

	public StockPair(Stock stock, double value) {
        this.stock = stock;
        this.value = value;
        this.weight = 0;
    }

    @Override
    public int compareTo(StockPair other) {
        //multiplied to -1 as the author need descending sort order
    	return -1*Double.valueOf(this.value).compareTo(other.value);
    }
    
    public String toString() {
    	String str = "Stock : " + this.stock + " Value : " + this.value;
		return str;
    }
    
}
