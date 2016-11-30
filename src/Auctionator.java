import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Date;
import java.util.List;

import org.apache.zookeeper.KeeperException;

// cliente
public class Auctionator {
	static final String HOST = "localhost";
	
	public static void main(String[] args) throws Exception {
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

		System.out.printf("Insira seu nome: ");
		String userName = br.readLine();
		
		Broker broker = new Broker(HOST);
		broker.connect();
		
		List<Auction> auctions = broker.getAuctions();

		System.out.println("Escolha um produto para participar:");

		for (int i = 0; i < auctions.size(); i++) {
			System.out.printf("(%d) Produto: %s\n", i + 1, auctions.get(i).getProduct());
		}

		int i = Integer.parseInt(br.readLine()) - 1;

		Auction auction = auctions.get(i);

		Bid bid = new Bid();
		bid.setAuction(auction);
		bid.setOwner(userName);
		bid.setValue(0);

		broker.participate(bid);

		while (true) {
	        try {
		        System.out.print("Insira seu lance: ");
	            bid.setValue(Float.parseFloat(br.readLine()));
	            broker.bid(bid);
	        } catch (Exception e){
	            System.err.println("Formato inválido.");
	        }
		}
	}
}
