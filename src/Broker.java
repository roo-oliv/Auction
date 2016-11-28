import java.io.IOException;

import javax.swing.plaf.SliderUI;

import org.apache.log4j.BasicConfigurator;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.ZooKeeper;

// zookeeper connector
public class Broker implements Runnable  {
	private String connectionString;
	private String path;
	private ZooKeeper zk;
	public boolean connected = true;
	private float lastBid = 0;
	private float currentBid = 0;
	
	public Broker(String connectionString, String path) {
//		BasicConfigurator.configure();
		this.connectionString = connectionString;
		this.path = path;
	}
	
	// conecta a cria um znode para o cliente
	public ZooKeeper init() throws Exception {
		zk = new ZooKeeper(connectionString, 60000, new Watcher() {
			@Override
			public void process(WatchedEvent e) {
			}
		});
	
		zk.create(path, null, Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);
		
		return zk;
	}
	
	public boolean bid(float value) {
		if (value > currentBid) {
			lastBid = currentBid;
			currentBid = value;
			System.out.println("bid: " + currentBid);
			return true;
		} else {
			return false;
		}
	}

	@Override
	public void run() {
	}
}
