package br.edu.ufabc.auction;

import org.apache.zookeeper.*;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

public class DistributedLock {
    private final ZooKeeper zk;
    private final String lockBasePath;
    private final String lockName;
    private String lockPath;

    public DistributedLock(ZooKeeper zk, String lockBasePath, String lockName) {
        this.zk = zk;
        this.lockBasePath = lockBasePath;
        this.lockName = lockName;
    }

    public void lock() throws IOException {
        try {
            if (zk.exists(lockBasePath, false) == null) {
                zk.create(lockBasePath, null, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
            }

            lockPath = zk.create(lockBasePath + "/" + lockName, null, ZooDefs.Ids.OPEN_ACL_UNSAFE,
                    CreateMode.EPHEMERAL_SEQUENTIAL);
            final Object lock = new Object();
            synchronized(lock) {
                while(true) {
                    List<String> nodes = zk.getChildren(lockBasePath, new Watcher() {
                        @Override
                        public void process(WatchedEvent event) {
                            synchronized (lock) {
                                lock.notifyAll();
                            }
                        }
                    });
                    Collections.sort(nodes);
                    if (lockPath.endsWith(nodes.get(0))) {
                        return;
                    } else {
                        lock.wait();
                    }
                }
            }
        } catch (KeeperException e) {
            throw new IOException (e);
        } catch (InterruptedException e) {
            throw new IOException (e);
        }
    }

    public void unlock() throws IOException {
        try {
            zk.delete(lockPath, -1);
            lockPath = null;
        } catch (KeeperException e) {
            throw new IOException (e);
        } catch (InterruptedException e) {
            throw new IOException (e);
        }
    }
}
