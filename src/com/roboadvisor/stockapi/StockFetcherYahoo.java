package com.roboadvisor.stockapi;

import java.io.BufferedReader;
import java.io. InputStreamReader;
import java.lang.reflect.Array;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.temporal.ChronoUnit;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.BufferedOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;

import org.apache.http.client.HttpClient;
import org.apache.http.client.CookieStore;
import org.apache.http.client.utils.HttpClientUtils;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.client.methods.HttpGet;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringEscapeUtils;



public class StockFetcherYahoo {

	HttpClient client = HttpClientBuilder.create().build();
    HttpClientContext context = HttpClientContext.create();
    
    private static Stock exchangeRate;
    
    public StockFetcherYahoo(Date beg, Date end) {
        CookieStore cookieStore = new BasicCookieStore();
        client = HttpClientBuilder.create().build();
        context = HttpClientContext.create();
        context.setCookieStore(cookieStore);
        exchangeRate = getExchangeRate(beg, end);
    }

    public String getPage(String symbol) {
    	String rtn = null;
        try {
			String url = String.format("https://finance.yahoo.com/quote/%s/?p=%s", symbol, symbol);
			HttpGet request = new HttpGet(url);
			System.out.println(url);
			
			request.addHeader("User-Agent", "Mozilla/5.0 (X11; U; Linux x86_64; en-US; rv:1.9.2.13) Gecko/20101206 Ubuntu/10.10 (maverick) Firefox/3.6.13");
			HttpResponse response = client.execute(request, context);
			System.out.println("Response Code : " + response.getStatusLine().getStatusCode());
			
			BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
			StringBuffer result = new StringBuffer();
			String line = "";
			while ((line = rd.readLine()) != null) {
			    result.append(line);
			}
			rtn = result.toString();
			HttpClientUtils.closeQuietly(response);
        } catch (Exception ex) {
            System.out.println("Exception for " + symbol);
        }
        System.out.println("returning from getPage");
        return rtn;
    }

    public List<String> splitPageData(String page) {
        // Return the page as a list of string using } to split the page
    	if (page == null)
    		return null;
    	
        return Arrays.asList(page.split("}"));
    }

    public String findCrumb(List<String> lines) {
    	if(lines == null)
    		return null;
    	
        String crumb = "";
        String rtn = "";
        for (String l : lines) {
            if (l.indexOf("CrumbStore") > -1) {
                rtn = l;
                break;
            }
        }
        // ,"CrumbStore":{"crumb":"OKSUqghoLs8"        
        if (rtn != null && !rtn.isEmpty()) {
            String[] vals = rtn.split(":");                 // get third item
            crumb = vals[2].replace("\"", "");              // strip quotes
            crumb = StringEscapeUtils.unescapeJava(crumb);  // unescape escaped values (particularly, \u002f
            }
        return crumb;
    }

    public String getCrumb(String symbol) {
        return findCrumb(splitPageData(getPage(symbol)));
    }
                  

    public void downloadData(String symbol, long startDate, long endDate, String crumb) {
        String filename = String.format("assets/%s.csv", symbol);
        String url = String.format("https://query1.finance.yahoo.com/v7/finance/download/%s?period1=%s&period2=%s&interval=1wk&events=history&crumb=%s", symbol, startDate, endDate, crumb);
        HttpGet request = new HttpGet(url);
        System.out.println(url);

        request.addHeader("User-Agent", "Mozilla/5.0 (X11; U; Linux x86_64; en-US; rv:1.9.2.13) Gecko/20101206 Ubuntu/10.10 (maverick) Firefox/3.6.13");
        try {
            HttpResponse response = client.execute(request, context);
            System.out.println("Response Code : " + response.getStatusLine().getStatusCode());
            HttpEntity entity = response.getEntity();

            String reasonPhrase = response.getStatusLine().getReasonPhrase();
            int statusCode = response.getStatusLine().getStatusCode();
            
            System.out.println(String.format("statusCode: %d", statusCode));
            System.out.println(String.format("reasonPhrase: %s", reasonPhrase));

            if (entity != null) {
                BufferedInputStream bis = new BufferedInputStream(entity.getContent());
                BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(new File(filename)));
                int inByte;
                while((inByte = bis.read()) != -1) 
                    bos.write(inByte);
                bis.close();
                bos.close();
            }
            HttpClientUtils.closeQuietly(response);

        } catch (Exception ex) {
            System.out.println("Exception");
            System.out.println(ex);
        }
    }
    
    public static Stock getStockTimeSerie(Stock ticker, Date beg, Date end) throws Exception{
    	String filename = String.format("assets/%s.csv", ticker.getSymbol());
		BufferedReader br;
		List<Double> prices= new ArrayList<Double>();
		List<Date> dates= new ArrayList<Date>();
		double exchangeRate = 1;
		
		br = new BufferedReader(new FileReader(filename));
		Stream<String> lines = br.lines();
		Stream<String> data = lines.skip(1);
		String[] stringArray = data.toArray(String[]::new);
		
		Date temp =null;
		for(int i =0; i<stringArray.length ;i++ ) {
			String[] split = stringArray[i].split(",");
			temp = createDate(split[0]);
			if(temp.after(beg) && temp.before(end)) {
				if(ticker.getCountry() == "US")
					exchangeRate = getExchangeRate(temp.getMonth(), temp.getYear());
				prices.add(Double.parseDouble(split[5])*exchangeRate);
				dates.add(temp);
			}
		}
		
		Date[] datesArray = new Date[dates.size()];
		double[] pricesArray = null;
		
		datesArray = dates.toArray(datesArray);
		pricesArray = ArrayUtils.toPrimitive(prices.toArray(new Double[prices.size()]));
		
		if(datesArray.length != 782 ) {
			throw new Exception();
		}
		return new Stock(ticker, pricesArray, datesArray);
	}
    
    @SuppressWarnings("deprecation")
	private static double getExchangeRate(int month, int year) {
    	double exRate = 0;
    	for(int i =0 ; i< exchangeRate.getDateTS().length ; i++) {
    		if(exchangeRate.getDateTS()[i].getMonth() == month && exchangeRate.getDateTS()[i].getYear() == year) {
    			exRate = exchangeRate.getAdjustedCloseTS()[i];
    		}
    	}
		return exRate;
	}

	private static Date createDate(String date) {
	     try {
	    	 if (date.contains("-"))
	         	return new SimpleDateFormat("yyyy-MM-dd").parse(date);
	    	 else
	 			return new SimpleDateFormat("dd/MM/yyyy").parse(date);
	     } catch (ParseException e) {
	        return null;
	     }
	}
    
//    public static void main (String[] args) {
//    	String[] stockAssets = new String[]{"AAPL","MSFT"};
//    	StockFetcherYahoo c = new StockFetcherYahoo();
//        for (String symbol: stockAssets) {
//            String crumb = c.getCrumb(symbol);
//            if (crumb != null && !crumb.isEmpty()) {
//                System.out.println(String.format("Downloading data to %s", symbol));
//                System.out.println("Crumb: " + crumb);
//                c.downloadData(symbol, 0, System.currentTimeMillis(), crumb);
//            } else {
//                System.out.println(String.format("Error retreiving data for %s", symbol));
//            }
//        }
//    }

    public ArrayList<Stock> getAllTickers(){
    	ArrayList<Stock> stocks = new ArrayList<Stock>();
    	stocks.addAll(getTickersUS());
    	stocks.addAll(getTickersCan());
    	stocks.addAll(getETFUS());
    	stocks.addAll(getETFCAD());
    	
    	return stocks;
    }
	
    private ArrayList<Stock> getETFUS() {
    	String nyseTradedTickers = "tickers/fixedIncomeETFUS.csv";
		BufferedReader br;

		ArrayList<Stock> stocks = new ArrayList<Stock>();
		
		try {
			br = new BufferedReader(new FileReader(nyseTradedTickers));
			Stream<String> data = br.lines();
			String[] stringArray = data.toArray(String[]::new);
			
			for(int i =0; i<stringArray.length ;i++ ) {
				String[] split = stringArray[i].split(",");;
				stocks.add(new Stock(split[0], split[1], "", "", "US"));
			}
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		return stocks;
	}
    
    private ArrayList<Stock> getETFCAD() {
    	String nyseTradedTickers = "tickers/fixedIncomeETFCAD.csv";
		BufferedReader br;

		ArrayList<Stock> stocks = new ArrayList<Stock>();
		
		try {
			br = new BufferedReader(new FileReader(nyseTradedTickers));
			Stream<String> data = br.lines();
			String[] stringArray = data.toArray(String[]::new);
			for(int i =0; i<stringArray.length ;i++ ) {
				String[] split = stringArray[i].split(",");;
				stocks.add(new Stock(split[0], split[1], "", "", "CAD"));
			}
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return stocks;
	}
    
    private ArrayList<Stock> getTickersCan() {
    	String nyseTradedTickers = "tickers/tsxList.csv";
		BufferedReader br;

		ArrayList<Stock> stocks = new ArrayList<Stock>();
		
		try {
			br = new BufferedReader(new FileReader(nyseTradedTickers));
			Stream<String> lines = br.lines();
			Stream<String> data = lines.skip(0);
			String[] stringArray = data.toArray(String[]::new);
			
			for(int i =0; i<stringArray.length ;i++ ) {
				String[] split = stringArray[i].split(",");
				try {
					stocks.add(new Stock(split[1] + ".TO", split[2], "", split[3], "CAD"));
				} catch (ArrayIndexOutOfBoundsException e) {}
			}
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		return stocks;
	}

	private ArrayList<Stock> getTickersUS(){
    	String nyseTradedTickers = "tickers/nyseCompanyList.csv";
		BufferedReader br;

		ArrayList<Stock> stocks = new ArrayList<Stock>();
		
		try {
			br = new BufferedReader(new FileReader(nyseTradedTickers));
			Stream<String> lines = br.lines();
			Stream<String> data = lines.skip(1);
			String[] stringArray = data.toArray(String[]::new);
			
			for(int i =0; i<stringArray.length ;i++ ) {
				String[] split = stringArray[i].split(",");
				stocks.add(new Stock(split[0], split[5], split[6], split[4], "US"));
			}
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		return stocks;
	}

	public Stock getExchangeRate(Date beg, Date end) {
		String asset = "assets/USDCAD.csv";
		BufferedReader br;
		
		ArrayList<Double> prices= new ArrayList<Double>();
		ArrayList<Date> dates= new ArrayList<Date>();

		Date temp = null;
		
		try {
			br = new BufferedReader(new FileReader(asset));
			Stream<String> lines = br.lines();
			Stream<String> data = lines.skip(1);
			String[] stringArray = data.toArray(String[]::new);
			
			for(int i =0; i<stringArray.length ;i++ ) {
				String[] split = stringArray[i].split(",");
				temp = createDate(split[0]);
				if(temp.after(beg) && temp.before(end)) {
					prices.add(Double.parseDouble(split[1]));
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
		
		return new Stock("USDCAD", pricesArray, datesArray);
	}
    
}
