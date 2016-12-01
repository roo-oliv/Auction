package br.edu.ufabc.auction;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;

import java.io.IOException;
import java.util.List;

public class BidQueue extends SyncPrimitive {
    static final String AUCTIONS_PATH = "/auctions";

    private ZooKeeper zk;
    private Auction auction;
    private String bidsPath;

    public BidQueue(ZooKeeper zk, Auction auction) throws KeeperException, InterruptedException {
        this.zk = zk;
        this.auction = auction;

        // cria o nó bids dentro da auction
        bidsPath = auction.getId() + "/bids";
        if (zk.exists(bidsPath, false) == null)
            zk.create(bidsPath, null, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);

        // adiciona o watcher inicial
        zk.getChildren(bidsPath, this, null);
    }

    public void add(Bid bid) throws KeeperException, InterruptedException, IOException {
        zk.create(bidsPath + "/", Converter.toBytes(bid), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
    }

    public Bid poll() throws KeeperException, InterruptedException, ClassNotFoundException, IOException {
        while (true) {
            synchronized (mutex) {
                List<String> children = zk.getChildren(bidsPath, false);

                if (children.size() == 0) {
                    // adiciona o watcher
                    zk.getChildren(bidsPath, this, null);
                    mutex.wait();
                } else {
                    String bidPath = bidsPath + "/" + children.get(0);
                    Bid bid = (Bid)Converter.fromBytes(zk.getData(bidPath, false, null));
                    zk.delete(bidPath, zk.exists(bidPath, true).getVersion());

                    return bid;
                }
            }
        }
    }
}
