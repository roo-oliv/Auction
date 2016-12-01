import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;

// cliente
public class Auctionator implements Serializable, Watcher {
	static final String HOST = "localhost";
	static final String DATE_PATTERN = "dd/MM/yyyy hh:mm:ss";
	static final Integer mutex = new Integer(-1);
	
	// atributos do participante (somente o nome para facilitar os testes)
	private String id;
	private String name;
	
	public String getId() { return id; }
	public void setId(String id) { this.id = id; }

	public String getName() { return name; }
	public void setName(String name) { this.name = name; }

	synchronized public void process(WatchedEvent e) {
        synchronized (mutex) {
            mutex.notify();
        }
    }
	public static void main(String[] args) throws Exception {
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		// TODO: validar nome
		Auctionator auctionator = new Auctionator();
		System.out.printf("Insira seu nome de usuário: ");
		auctionator.setName(br.readLine());
		
		Broker broker = new Broker(HOST);
		broker.connect();
		Auction auction = null;

		System.out.println("Escolha um produto para participar:");
		while (auction == null) {
			synchronized (mutex) {
				List<Auction> auctions = broker.getAuctions();
	
				if (auctions.size() > 0) {
					System.out.println("=========================");
					for (int i = 0; i < auctions.size(); i++) {
						if (i > 0)
							System.out.println("-------------------------");
						System.out.printf("%-4s Produto: %s\n", "", auctions.get(i).getProduct());
						System.out.printf("%-4s Lance inicial: R$%.2f\n", "(" + (i + 1) + ")", auctions.get(i).getStartBid().getValue());
						System.out.printf("%-4s Início: %s - Fim: %s\n", "",
							new SimpleDateFormat(DATE_PATTERN).format(auctions.get(i).getStartDate()), 
							new SimpleDateFormat(DATE_PATTERN).format(auctions.get(i).getEndDate()));
					}
					System.out.println("=========================");
			
					int i = Integer.parseInt(br.readLine()) - 1;
			
					Auction selectedAuction = auctions.get(i);
					
					if (selectedAuction.getStartDate().after(new Date()))
						auction = selectedAuction;
					else 
						System.out.println("Leilão já iniciou, escolha outro:");
				} else {
					System.out.println("Nenhum leilão disponível no momento. Aguarde um instante.");
					broker.watchAuctions(auctionator);
					mutex.wait();
				}
			}
		}

		long secondsRemaining = Math.abs(auction.getStartDate().getTime() - new Date().getTime()) / 1000;
		System.out.printf("Aguarde. O leilão irá começar em %s\n", Util.formatTime(secondsRemaining));
		broker.participate(auctionator, auction);
		System.out.printf("Leilão iniciado\n");
		
		Bid bid = new Bid();
		bid.setAuction(auction);
		bid.setAuctionator(auctionator);

        System.out.print("Insira seus lances:\n");
		while (true) {
            bid.setValue(Float.parseFloat(br.readLine()));
            broker.bid(bid);
		}
	}
}
