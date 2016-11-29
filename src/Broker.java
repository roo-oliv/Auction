import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import javax.swing.plaf.SliderUI;

import org.apache.log4j.BasicConfigurator;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.Watcher.Event.KeeperState;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.ZooKeeper;

// zookeeper connector
public class Broker {
	private String host;
	private String znode;

	private ZooKeeper zk;
   	final CountDownLatch connectedSignal = new CountDownLatch(1);

	public boolean connected = true;
	private float lastBid = 0;
	private float currentBid = 0;
	
	public Broker(String host) {
//		BasicConfigurator.configure();
		this.host = host;
	}
	
	// conecta a cria um znode para o cliente
	public ZooKeeper connect() throws Exception {
		zk = new ZooKeeper(host, 60000, new Watcher() {
			@Override
			public void process(WatchedEvent e) {
				if (e.getState() == KeeperState.SyncConnected) {
	               connectedSignal.countDown();
	            }
			}
		});
      	connectedSignal.await();
		
		return zk;
	}

	public List<Auction> getAuctions() throws KeeperException, InterruptedException, ClassNotFoundException, IOException {
		List<String> children = zk.getChildren("/", false);

		List<Auction> auctions = new ArrayList<Auction>();

		for (String child : children) {
			byte[] data = zk.getData(child, false, null);
			auctions.add(Auction.fromByte(data));
		}

		return auctions;
	}
	
	// cria um znode sequencial no caminho do produto selecionado e retorna o caminho gerado
	public String participate(Auction auction) throws KeeperException, InterruptedException {
		String path = "/" + auction.getName() + "/auctionator";
		znode = zk.create(path, null, Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
		return znode;
	}

	public String auction(Auction auction) throws KeeperException, InterruptedException, IOException {
		String path = "/auction";
		znode = zk.create(path, Auction.toByte(auction), Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
		return znode;
	}

	public boolean bid(float value) throws KeeperException, InterruptedException {
		if (value > currentBid) {
			zk.setData(znode, floatToBytes(value), zk.exists(znode, true).getVersion());
			lastBid = currentBid;
			currentBid = value;
			System.out.println("bid: " + currentBid);
			return true;
		} else {
			return false;
		}
	}

	public static byte [] floatToBytes(float value)
	{  
	     return ByteBuffer.allocate(4).putFloat(value).array();
	}
}
