package Core;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;

/**
 * Used to create new thread which contains more operations
 * @author Administrator
 *
 */
public class NotifyThreadFactory implements ThreadFactory {
	private final String poolName;
	
	//number of the thread created by the factory(instance not the class)
	private final AtomicInteger createdNumber = new AtomicInteger();
	
	public NotifyThreadFactory(String poolName) {
		this.poolName = poolName;
	}
	
	public NotifyThreadFactory() {
		this.poolName = "IAA";
	}
	public Thread newThread(Runnable runnable) {
		createdNumber.incrementAndGet();
		return new NotifyThread(runnable, poolName);
	}
	
	/**get the number of the thread created by the factory instance,
	 * if the thread is dead,the number will not decrease
	 * @return
	 */
	public int getNumber() {
		return createdNumber.get();
	}
}
