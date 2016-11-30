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
			if (child.startsWith("auction")) {
				byte[] data = zk.getData("/" + child, false, null);
				Auction auction = Auction.fromBytes(data);
				auction.setId(child);
				auctions.add(auction);
				}
		}

		return auctions;
	}
	
	// cria um znode sequencial no caminho do produto selecionado e retorna o caminho gerado
	public void participate(Bid bid) throws KeeperException, InterruptedException, IOException {
		String path = "/" + bid.getAuction().getId() + "/bid";
		znode = zk.create(path, Bid.toBytes(bid), Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
		bid.setId(znode);
	}

	// cria um znode sequencial na raiz, representa um produto a ser leiloado
	public void auction(Auction auction) throws KeeperException, InterruptedException, IOException {
		String path = "/auction";
		znode = zk.create(path, Auction.toBytes(auction), Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT_SEQUENTIAL);
		auction.setId(znode);
	}
	
	public void updateAuction(Auction auction) throws KeeperException, InterruptedException, IOException {
		zk.setData(auction.getId(), Auction.toBytes(auction), zk.exists(auction.getId(), true).getVersion());
	}
	
	public Auction watchAuction(String path, Watcher watcher) throws KeeperException, InterruptedException, ClassNotFoundException, IOException {
		byte[] data = zk.getData(path, watcher, null);
		return Auction.fromBytes(data);
	}
	
	public List<Bid> watchBids(Auctioneer auctioneer) throws KeeperException, InterruptedException, ClassNotFoundException, IOException {
		List<Bid> bids = new ArrayList<Bid>(); 
		
		// watcher quando for adicionado um novo filho
		List<String> children = zk.getChildren(auctioneer.getAuction().getId(), auctioneer);
		for (String child : children) {
			// watcher quando for alterado
			byte[] data = zk.getData(auctioneer.getAuction().getId() + "/" + child, auctioneer, null);
			bids.add(Bid.fromBytes(data));
		}
		
		return bids;
	}

	public void bid(Bid bid) throws KeeperException, InterruptedException, ClassNotFoundException, IOException {
		zk.setData(bid.getId(), Bid.toBytes(bid), zk.exists(bid.getId(), true).getVersion());
//		Auction auction = Auction.fromBytes(zk.getData(bid.getAuction().getId(), false, null));
//
//		if (bid.getValue() > auction.getCurrentBid().getValue()) {
//			zk.setData(bid.getId(), Bid.toBytes(bid), zk.exists(bid.getId(), true).getVersion());
//			auction.setCurrentBid(bid);
//			zk.setData(auction.getId(), Auction.toBytes(auction), zk.exists(auction.getId(), true).getVersion());
//			System.out.println("bid: " + auction.getCurrentBid().getValue());
//			return true;
//		} else {
//			return false;
//		}
	}
}
