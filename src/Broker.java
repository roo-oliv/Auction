import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Date;
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
	static final String AUCTIONS_PATH = "/auctions";
	
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
		List<String> children = zk.getChildren(AUCTIONS_PATH, false);

		List<Auction> auctions = new ArrayList<Auction>();

		for (String child : children) {
			if (child.startsWith("auction")) {
				byte[] data = zk.getData(AUCTIONS_PATH + "/" + child, false, null);
				Auction auction = Converter.fromBytes(data);
				
				if (auction.getStartDate().after(new Date())) {
					auction.setId(child);
					auctions.add(auction);
				}
			}
		}

		return auctions;
	}
	
	// cria um znode sequencial no caminho do produto selecionado
	public void participate(Auctionator auctionator, Auction auction) throws KeeperException, InterruptedException, IOException {
		String auctionatorsPath = AUCTIONS_PATH + "/" + auction.getId() + "/auctionators";
		String auctionatorPath =  zk.create(auctionatorsPath + "/", Converter.toBytes(auctionator), Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
		auctionator.setId(auctionatorPath);
		
		AuctionBarrier barrier = new AuctionBarrier(auction);
		barrier.enter();
	}

	// cria um znode sequencial para a auction e dois filhos para os auctionators e os bids
	public void createAuction(Auction auction) throws KeeperException, InterruptedException, IOException {
		if (zk.exists(AUCTIONS_PATH, false) == null) 
			zk.create(AUCTIONS_PATH, null, Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);

		String auctionPath = zk.create(AUCTIONS_PATH + "/auction", Auction.toBytes(auction), Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT_SEQUENTIAL);

		zk.create(auctionPath + "/auctionators", null, Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
		zk.create(auctionPath + "/bids", null, Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
		auction.setId(auctionPath);
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
