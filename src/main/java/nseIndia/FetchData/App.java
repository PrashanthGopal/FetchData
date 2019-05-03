package nseIndia.FetchData;

import java.util.Timer;

public class App {

	public static void main(String[] args) {
		StockJob job = new StockJob();
		Timer t = new Timer();

		t.scheduleAtFixedRate(job, 0, 3 * 60 * 1000);

	}

}
