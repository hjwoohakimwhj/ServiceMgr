package Core;
import java.util.concurrent.ExecutorService;
import java.util.Collections;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.HashSet;
import java.util.Set;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.ArrayList;

public class TrackingExecutorService extends AbstractExecutorService{
	private final ExecutorService exec;
	
	//use the synchronizedSet to guarantee the thread safe
	private final Set<Runnable> taskCancelAtShutdown = Collections.synchronizedSet(
			new HashSet<Runnable>());
	
	public TrackingExecutorService(ThreadPoolExecutor threadPoolExecutor) {
		exec = threadPoolExecutor;
	}
	
	public boolean isShutdown() {
		return exec.isShutdown();
	}
	
	public void shutdown() {
		exec.shutdown();
	}
	
	public boolean isTerminated() {
		return exec.isTerminated();
	}
	
	public List<Runnable> shutdownNow() {
		return exec.shutdownNow();
	}
	
	public boolean awaitTermination(long timeout, TimeUnit unit) {
		try {
			return exec.awaitTermination(timeout, unit);
		}catch(InterruptedException e) {
			return false;
		}
	}
	
	public void execute(final Runnable runnable) {
		exec.execute(new Runnable() {
			public void run() {
				try {
					runnable.run();
				}finally {
					if(isShutdown()&& Thread.currentThread().isInterrupted()) {
						taskCancelAtShutdown.add(runnable);
					}
				}
			}
		});
	}
	
	public List<Runnable> getTaskCancel(){
		boolean flagTerminate = false;
		try {
			flagTerminate = exec.awaitTermination(Long.MAX_VALUE,TimeUnit.SECONDS);
		}catch(InterruptedException e) {
		}
		if (flagTerminate&&exec.isTerminated()){
			return new ArrayList<Runnable>(taskCancelAtShutdown);
		}
		throw new IllegalStateException();
	}
}