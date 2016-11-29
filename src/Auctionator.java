import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Date;
import java.util.List;

import org.apache.zookeeper.KeeperException;

// cliente
public class Auctionator {
	static final String CONNECTION_STRING = "localhost";
	
	public static void main(String[] args) throws NumberFormatException, IOException, KeeperException, InterruptedException, ClassNotFoundException {
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

		Broker broker = new Broker(CONNECTION_STRING);
		
		List<Auction> auctions = broker.getAuctions();

		System.out.println("Escolha um produto para participar:");

		for (int i = 0; i < auctions.size(); i++) {
			System.out.printf("(%d) Produto: %s\n", i + 1, auctions.get(i).getProduct());
		}

		int i = Integer.parseInt(br.readLine()) - 1;

		Auction auction = auctions.get(i);

		try {
			broker.connect();
			broker.participate(auction);
		} catch (Exception e) {
			System.out.println(e);
		}
		while (true) {
	        try {
		        System.out.print("Insira seu lance:");
	            float bid = Float.parseFloat(br.readLine());
	            if (broker.bid(bid)) {
	             
	            } else {
	            	System.out.println("Valor deve ser maior ou igual ao lance atual");
	            }
	        } catch (Exception e){
	            System.err.println("Formato inválido.");
	        }
		}
	}
}
