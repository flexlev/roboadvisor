package com.roboadvisor.strategy;

import java.util.Arrays;
import java.util.stream.IntStream;

import org.apache.commons.math3.linear.RealMatrix;

public class RiskParity implements Calcfc{
	
	private RealMatrix cov;
	private Cobyla cobyla;

	public RiskParity() {
		cobyla = new Cobyla();
	}
	
	@Override
	public double Compute(int n, int m, double[] x, double[] con) {
		double sum = 0;
		double[][] cov = this.cov.getData();
		double sigma = dot(multiply(x,cov),x);
		
		for(int i =0; i<n; i++) {
			sum += Math.pow(x[i] - Math.pow(sigma,2)/(matrixMultReturnIndex(cov,x,i)*n),2);
		}
		
		double sumWeight = 0;
		for(int i = 0; i<m ; i++) {
			con[i] = x[i];
			sumWeight += x[i];
		}
		con[m] = 1 - sumWeight;
		
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

	public RealMatrix getCov() {
		return cov;
	}

	public void setCov(RealMatrix cov) {
		this.cov = cov;
	}

	public Cobyla getCobyla() {
		return cobyla;
	}

	public void setCobyla(Cobyla cobyla) {
		this.cobyla = cobyla;
	}

}
