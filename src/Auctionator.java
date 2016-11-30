import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.apache.zookeeper.KeeperException;

// cliente
public class Auctionator implements Serializable {
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
				mutex.wait();
			}
		}

		long secondsRemaining = Math.abs(auction.getStartDate().getTime() - new Date().getTime()) / 1000;
		System.out.printf("Aguarde. O leilão irá começar em %s\n", formatTime(secondsRemaining));
		broker.participate(auctionator, auction);
		
		Bid bid = new Bid();
		bid.setAuction(auction);
		bid.setAuctionator(auctionator);
		
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

	private static String formatTime(long ts) {
		long[] tokens = new long[4];

		long s = ts % 60;
		long tm = ts / 60;
		long m = tm % 60;
		long th = m / 60;
		long h = th % 24;
		long d = th / 24;

		String days = (d == 0) ? "" : (d == 1) ? "1 dia, " : String.format("%d dias, ", d);
		String hours = (h == 0) ? "" : (h == 1) ? "1 hora, " : String.format("%d horas, ", h);
		String minutes = (m == 0) ? "" : (m == 1) ? "1 minuto, " : String.format("%d minutos, ", m);
		String seconds = (s == 0) ? "" : (s == 1) ? "1 segundo, " : String.format("%d segundos, ", s);

		String time = days + hours + minutes + seconds;

		// remove a útlima vírgula
		return replaceLast(replaceLast(time, ",", ""), ",", " e");
	}
	
	private static String replaceLast(String text, String substring, String replacement)
	{
	  int i = text.lastIndexOf(substring);
	  if (i == -1)
	    return text;
	  else
		  return text.substring(0, i) + replacement + text.substring(i + substring.length());
	}
}
