import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;

public class SyncPrimitive implements Watcher {
	final Integer mutex;

	public SyncPrimitive()	{
		mutex = new Integer(-1);
	}

    synchronized public void process(WatchedEvent event) {
        synchronized (mutex) {
            mutex.notify();
        }
    }
}
