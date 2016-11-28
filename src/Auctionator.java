import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Date;

// cliente
public class Auctionator {
	static final String CONNECTION_STRING = "localhost";
	// nome do nó, deve possuir uma chave única (timestamp, por exemplo)
	static final String PATH = "/caminho" + new Date();
	
	public static void main(String[] args) {
		Broker broker = new Broker(CONNECTION_STRING, PATH);
		
		try {
			broker.init();
		} catch (Exception e) {
			System.out.println(e);
		}

		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
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
