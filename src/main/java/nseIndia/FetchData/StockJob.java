package nseIndia.FetchData;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Date;
import java.util.HashMap;
import java.util.TimerTask;
import java.util.stream.Stream;

import javax.swing.JOptionPane;

import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class StockJob extends TimerTask {

	public StockJob() {
	}

//	public static String url = "https://www.nseindia.com/live_market/dynaContent/live_watch/get_quote/GetQuote.jsp?symbol=";
	public static String url = "https://query1.finance.yahoo.com/v7/finance/quote?symbols=%s.NS";

	public static String fileInput = "/Users/marpr04/Desktop/stockInput.txt";

	private static void processInputFile(String fileName) {
		try {
			Stream<String> lines = Files.lines(Paths.get(fileName)).filter(line -> !line.trim().isEmpty());
			lines.forEach(line -> calculateStockPrice(line));
			lines.close();
		} catch (IOException io) {
			io.printStackTrace();
		}
	}

	private static void calculateStockPrice(String stockDetails) {

		String[] inputArr = stockDetails.split(",");
		String stockSymbol = null;
		double stopLoss = 0;
		double stopProfit = 0;
		StringBuilder Message = new StringBuilder();

		HashMap<String, String> Stock = new HashMap<String, String>();

		for (String input : inputArr) {
			String[] stockDtl = input.split(":");
			Stock.put(stockDtl[0], stockDtl[1]);
		}
		stockSymbol = Stock.get("symbol");
		stopLoss = Double.parseDouble(Stock.get("stopLoss").trim());
		stopProfit = Double.parseDouble(Stock.get("stopProfit").trim());
		url = String.format(url, stockSymbol);

		Document doc = null;
		try {

			doc = Jsoup.connect(url).ignoreContentType(true).get();
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}

		JSONObject json = new JSONObject(doc.text());
		JSONObject jsonResult = json.getJSONObject("quoteResponse");
		JSONArray responseData = jsonResult.getJSONArray("result");
		JSONObject priceData = responseData.getJSONObject(0);
		double lastPrice = priceData.getDouble("regularMarketPrice");
		String companyName = priceData.getString("shortName").trim();

		if (lastPrice >= stopProfit) {
			Message.append(companyName);
			Message.append(" Has crossed profit Price:: ");
			Message.append(" Price:: ");
			Message.append(lastPrice);
		} else if (lastPrice <= stopLoss) {
			Message.append(companyName);
			Message.append(" Has crossed Loss Price:: ");
			Message.append(" Price:: ");
			Message.append(lastPrice);
		}
		System.out.println(new Date() + " - " + companyName + " price:: " + lastPrice);
		if (Message.length() > 0) {
			notifier(Message.toString());
		}
	}

	@Override
	public void run() {
		processInputFile(fileInput);
	}

	public static void notifier(String msg) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				EmailUtil.mailNotification(msg, "Stock Notificaton");
			}
		}).start();
		
		JOptionPane.showMessageDialog(null, msg);

	}
}