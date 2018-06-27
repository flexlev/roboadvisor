package com.roboadvisor.strategy;

import com.roboadvisor.stockapi.Stock;

public class StockPair implements Comparable<StockPair>{

	public final Stock stock;
    public final double value;
    public double weight;
    public double variance;
    public boolean varianceSorted;

    public void setWeight(double weight) {
		this.weight = weight;
	}

	public StockPair(Stock stock, double value, double variance) {
        this.stock = stock;
        this.value = value;
        this.weight = 0;
        this.variance = variance;
        this.varianceSorted = false;
    }

    @Override
    public int compareTo(StockPair other) {
        //multiplied to -1 as the author need descending sort order
    	if (this.varianceSorted)
    		return -1*Double.valueOf(this.value).compareTo(other.value);
    	else
    		return Double.valueOf(this.variance).compareTo(other.variance);
    		
    }
    
    public String toString() {
    	String str = "Stock : " + this.stock + " Value : " + this.value;
		return str;
    }
    
}
