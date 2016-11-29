import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

// servidor
// responsável por criar um leilão
public class Auctioneer {
	static final String HOST = "localhost";
	static final String DATE_PATTERN = "dd/mm/yyyy hh:MM:ss";

	public static void main(String[] args) throws Exception {
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		
		System.out.println("Leiloar produto:");
		System.out.printf("Nome do produto: ");
		String produto = br.readLine();
		System.out.printf("Lance inicial: ");
		float startBid = Float.parseFloat(br.readLine());
		float currentBid = startBid;
		System.out.printf("Data de início (%s): ", DATE_PATTERN);
		Date startDate = new SimpleDateFormat(DATE_PATTERN).parse(br.readLine());
		System.out.printf("Prazo (minutos): ");
		int deadline = Integer.parseInt(br.readLine());

		Broker broker = new Broker(HOST);
		broker.connect();
	}
}
