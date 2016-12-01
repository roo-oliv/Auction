package br.edu.ufabc.auction;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.zookeeper.KeeperException;

public class AuctionBarrier extends TimerTask {
    private Auction auction;
    private Integer mutex;

    public AuctionBarrier(Auction auction) throws KeeperException, InterruptedException {
        this.auction = auction;
        mutex = new Integer(-1);
        Timer timer = new Timer();

        timer.schedule(this, auction.getStartDate());
    }

    @Override
    synchronized public void run() {
        synchronized (mutex) {
            mutex.notify();
        }
    }

    public void enter() throws InterruptedException {
        while (true) {
            synchronized (mutex) {
                if (auction.getStartDate().after(new Date())) {
                    mutex.wait();
                } else {
                    return;
                }
            }
        }
    }
}
