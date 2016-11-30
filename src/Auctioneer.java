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
public class Auctioneer implements Watcher {
	static final String HOST = "localhost";
	static final String DATE_PATTERN = "dd/MM/yyyy hh:mm:ss";
	static final CountDownLatch connectedSignal = new CountDownLatch(1);

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
		auction.setCurrentBid(startBid);
		auction.setStartDate(startDate);
		auction.setEndDate(endDate);

		Broker broker = new Broker(HOST);
		broker.connect();
		// cria o nó da auction
		broker.createAuction(auction);
		//broker.watchBids(new Auctioneer(broker, auction));
		connectedSignal.await();
	}

	@Override
	public void process(WatchedEvent e) {
		if (e.getType() == Event.EventType.None) {
            switch(e.getState()) {
               case Expired:
               connectedSignal.countDown();
               break;
            }
		} else {
         	try {
         		// para continuar verificando alterações no znode
     			List<Bid> bids = broker.watchBids(this);
     			
	 			Bid highestBid = null;
	 			for (Bid bid : bids) {
	 				// verifica se é maior que o lance atual do leilão
	 				if (bid.getValue() > auction.getCurrentBid().getValue()) {
	 					// verifica se é maior que o lance entre todos os bids da rodada
	 					if (highestBid == null || bid.getValue() > highestBid.getValue()) 
	 						highestBid = bid;
	 				}
	 			}
	 			
	 			// substitui o lance atual do leilão e atualiza o nó
	 			if (highestBid != null) {
	 				auction.setCurrentBid(highestBid);
	 				broker.updateAuction(auction);
	 				System.out.printf("Maior lance: %s - R$ %.2f\n", "TESTE", auction.getCurrentBid().getValue());
	 			}
            } catch(Exception ex) {
            	System.out.println(ex.getMessage());
            }
		}
	}
	
	public Auction getAuction() {
		return auction;
	}
}
