package br.edu.ufabc.auction;

import org.apache.zookeeper.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class Broker implements Watcher {
    static final String LOCK_PATH = "/lock";
    static final String AUCTIONS_PATH = "/auctions";

    private String host;

    private ZooKeeper zk;
    // a auction e a fila são criadas nos métodos participate e createAuction
    private Long buyerId;
    private BidQueue bidQueue;
    private Auction auction;
    final CountDownLatch connectedSignal = new CountDownLatch(1);

    public boolean connected = true;
    private float lastBid = 0;
    private float currentBid = 0;

    public Broker(String host) {
        this.host = host;
    }

    // conecta a cria um znode para o cliente
    public ZooKeeper connect() throws Exception {
        zk = new ZooKeeper(host, 60000, new Watcher() {
            @Override
            public void process(WatchedEvent e) {
                if (e.getState() == Event.KeeperState.SyncConnected) {
                    connectedSignal.countDown();
                }
            }
        });

        connectedSignal.await();

        return zk;
    }

    public List<Auction> getAuctions() throws KeeperException, InterruptedException, ClassNotFoundException,
            IOException
    {
        List<String> children = zk.getChildren(AUCTIONS_PATH, false);

        List<Auction> auctions = new ArrayList<Auction>();

        for (String child : children) {
            byte[] data = zk.getData(AUCTIONS_PATH + "/" + child, false, null);
            Auction auction = Converter.fromBytes(data);

            if (auction.getStartDate().after(new Date())) {
                // seta o valor pois o id foi gerado depois do nó ser criado
                auction.setId(AUCTIONS_PATH + "/" + child);
                auctions.add(auction);
            }
        }

        return auctions;
    }

    // cria um znode sequencial no caminho do produto selecionado
    public void participate(Buyer buyer, Auction auction) throws KeeperException, InterruptedException, IOException {
        this.auction = auction;
        bidQueue = new BidQueue(zk, auction);

        String buyersPath = auction.getId() + "/buyers";
        String buyerPath = zk.create(buyersPath + "/", Converter.toBytes(buyer), ZooDefs.Ids.OPEN_ACL_UNSAFE,
                CreateMode.PERSISTENT_SEQUENTIAL);
        buyer.setId(buyerPath);

        zk.getData(auction.getId() + "/bestbid", this, null);

        AuctionBarrier barrier = new AuctionBarrier(auction);
        barrier.enter();
    }

    // cria um znode sequencial para a auction e dois filhos para os buyers e os bids
    public void createAuction(Auction.Builder auctionBuilder) throws KeeperException, InterruptedException, IOException {
        DistributedLock distributedLock = new DistributedLock(zk, LOCK_PATH, "auction-creation-lock");
        try {
            distributedLock.lock();

            byte[] data = zk.getData("/controller", false, null);
            AuctionController controller = Converter.fromBytes(data);

            this.auction = controller.createAuction(auctionBuilder, zk);
            bidQueue = new BidQueue(zk, auction);

            AuctionBarrier barrier = new AuctionBarrier(auction);
            barrier.enter();
        } catch (IOException e) {
            // TODO: Handle appropriately
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            // TODO: Handle appropriately
            e.printStackTrace();
        } finally {
            distributedLock.unlock();
        }
    }

    // watchers
    public void watchAuctions(Buyer buyer) throws KeeperException, InterruptedException {
        zk.getChildren(AUCTIONS_PATH, buyer, null);
    }

    public void bid(float value) throws KeeperException, InterruptedException, ClassNotFoundException, IOException {
        Bid.Builder bidBuilder = new Bid.Builder();
        bidBuilder.buyerId(buyerId);
        bidBuilder.value(value);
        Bid bid = bidBuilder.build();

        Bid bestBid = Converter.fromBytes(zk.getData(this.auction.getId() + "/bestbid", false, null));

        if (bid.getValue() > bestBid.getValue()) {
            bidQueue.add(bid);
        }
    }

    public Bid pollBid() throws ClassNotFoundException, KeeperException, InterruptedException, IOException {
        return bidQueue.poll();
    }

    public Bid getBestBid() throws ClassNotFoundException, IOException, KeeperException, InterruptedException {
        return Converter.fromBytes(zk.getData(auction.getId() + "/bestbid", false, null));
    }

    public void updateBestBid(Bid bid) throws KeeperException, InterruptedException, IOException {
        String bestBidPath = auction.getId() + "/bestbid";
        zk.setData(bestBidPath, Converter.toBytes(bid), zk.exists(bestBidPath, true).getVersion());
    }

    @Override
    public void process(WatchedEvent e) {
        Bid bestBid;
        try {
            bestBid = Converter.fromBytes(zk.getData(auction.getId() + "/bestbid", this, null));
            System.out.printf("Melhor lance: R$ %.2f\n", bestBid.getValue());
        } catch (ClassNotFoundException | IOException | KeeperException | InterruptedException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
    }
}
