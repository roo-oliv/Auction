import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;

// servidor
// responsável por criar um leilão
public class Auctioneer {
	static final String HOST = "localhost";
	static final String DATE_PATTERN = "dd/MM/yyyy hh:mm:ss";

	Broker broker;
	Auction auction;
	
	public Auctioneer(Broker broker, Auction auction) {
		this.broker = broker;
		this.auction = auction;
	}

	public static void main(String[] args) throws Exception {
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		
		// recebe os valores para criar uma Auction
		System.out.println("Leiloar produto");
		System.out.printf("Nome do produto: ");
		String produto = br.readLine();
		System.out.printf("Lance inicial (R$): ");
		float startValue = Float.parseFloat(br.readLine());
		System.out.printf("Data de início (%s): ", DATE_PATTERN);
		Date startDate = new SimpleDateFormat(DATE_PATTERN).parse(br.readLine());
		System.out.printf("Prazo (minutos): ");
		int deadline = Integer.parseInt(br.readLine());
		Date endDate = new Date(startDate.getTime() + (deadline * 60000));

		// cria uma Auction
		Bid startBid = new Bid();
		startBid.setValue(startValue);

		Auction auction = new Auction();
		auction.setProduct(produto);
		auction.setStartBid(startBid);
		auction.setStartDate(startDate);
		auction.setEndDate(endDate);

		Broker broker = new Broker(HOST);
		broker.connect();

		long secondsRemaining = Math.abs(auction.getStartDate().getTime() - new Date().getTime()) / 1000;
		System.out.printf("Aguarde. O leilão irá começar em %s\n", Util.formatTime(secondsRemaining));
		// cria o znode da auction
		broker.createAuction(auction);
		System.out.printf("Leilão iniciado\n");
		
		while (auction.getEndDate().after(new Date())) {
			Bid bid = broker.pollBid();
			Bid bestBid = broker.getBestBid();
			
			if (bid.getValue() > bestBid.getValue()) {
				broker.updateBestBid(bid);
				System.out.printf("Melhor lance: %s - R$ %.2f\n", bid.getAuctionator().getName(), bid.getValue());
			}
		}
	}
	
	public Auction getAuction() {
		return auction;
	}
}
