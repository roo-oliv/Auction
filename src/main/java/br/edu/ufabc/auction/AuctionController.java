package br.edu.ufabc.auction;

import org.apache.zookeeper.*;

import java.io.IOException;
import java.io.Serializable;
import java.util.concurrent.CountDownLatch;

public class AuctionController implements Serializable {
    static final String AUCTIONS_PATH = "/auctions";

    private Long availableId;

    private AuctionController() {
        availableId = 0L;
    }

    public Auction createAuction(Auction.Builder auctionBuilder, ZooKeeper zk)
            throws IOException, KeeperException, InterruptedException
    {
        //auctionBuilder.id(availableId);
        availableId += 1;
        Auction auction = auctionBuilder.build();

        if (zk.exists(AUCTIONS_PATH, false) == null) {
            zk.create(AUCTIONS_PATH, null, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
        }

        String auctionPath = zk.create(AUCTIONS_PATH + "/", Converter.toBytes(auction), ZooDefs.Ids.OPEN_ACL_UNSAFE,
                CreateMode.PERSISTENT_SEQUENTIAL);

        zk.create(auctionPath + "/bestbid", Converter.toBytes(auction.getStartBid()), ZooDefs.Ids.OPEN_ACL_UNSAFE,
                CreateMode.PERSISTENT);
        zk.create(auctionPath + "/buyers", null, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);

        auction.setId(auctionPath);

        return auction;
    }

    public static void main(String[] args) throws Exception {
        final CountDownLatch connectedSignal = new CountDownLatch(1);

        ZooKeeper zk = new ZooKeeper(args[0], 60000, new Watcher() {
            @Override
            public void process(WatchedEvent e) {
                if (e.getState() == Event.KeeperState.SyncConnected) {
                    connectedSignal.countDown();
                }
            }
        });

        connectedSignal.await();

        zk.create("/controller", Converter.toBytes(new AuctionController()), ZooDefs.Ids.OPEN_ACL_UNSAFE,
                CreateMode.PERSISTENT);
    }
}
