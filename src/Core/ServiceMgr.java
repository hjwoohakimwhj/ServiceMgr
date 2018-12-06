package Core;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import ZabbixDriver.ZabbixDriver;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import Mongo.MongoApi;
import ServiceApi.MonitorFormat;
import net.sf.json.JSONObject;

public class ServiceMgr {
	private final NotifyThreadFactory notifyThreadFactory;
	private final TrackingExecutorService serviceMgr;
	private final LinkedBlockingQueue<Runnable> workQueue;
	private static final AtomicInteger taskNumber = new AtomicInteger();
	
	/**
	 * the number of thread is limited ,and the size of the queue is also fixed
	 * when the queue is fulled , we'll drop the task
	 */
	public ServiceMgr(int threadNum, int capacity) {
		System.out.println("ServiceManager initiate begin!");
		notifyThreadFactory =  new NotifyThreadFactory();
	    workQueue = new LinkedBlockingQueue<Runnable>(capacity);
		serviceMgr = new TrackingExecutorService(new ThreadPoolExecutor(threadNum, threadNum, 0L, 
				TimeUnit.SECONDS, workQueue, notifyThreadFactory, new ThreadPoolExecutor.AbortPolicy()));
		System.out.println("ServiceManager initiate finish!");
	}
	public void handler(JSONObject params,ZabbixDriver zabbixDriver
			,Map<String,MonitorFormat> requestMonitorInfo,ServiceMgr serviceMgr,
			Map<String,String> quickCache,MongoApi mongo) {
		this.serviceMgr.execute(new Runnable() {
			public void run() {
				while(true) {
					//System.out.println("running =================");
					for(String monitorTarget : requestMonitorInfo.keySet()) {
						MonitorFormat monitorFormat =  requestMonitorInfo.get(monitorTarget);
						String result = monitorFormat.request(zabbixDriver,serviceMgr);
						String vnfNodeId = monitorFormat.vnfNodeId;
						JSONObject uploadJSON = new JSONObject();
						uploadJSON.put(monitorTarget, result);
						quickCache.put(monitorTarget,result); 
						mongo.putVnfTarget(vnfNodeId,uploadJSON);
					}
					try {
						Thread.sleep((int)params.get("interval"));
					}catch(Exception e) {
						e.printStackTrace();
					}
				}
			}
		});
	}
	
	/*
	 * flag = 0 -> hostRegister
	 * flag = 1 -> itemCreate
	 */
	public void handler(JSONObject params,ZabbixDriver zabbixDriver,
			HashMap<String,String> map, int flag) {
		try {
			switch(flag) {
				case  0 :
					serviceMgr.execute(new Runnable() {
						public void run() {
							String vnfcNodeId = String.valueOf(params.get("vnfcNodeId"));
							String hostId = zabbixDriver.registerHost(params);
							map.put(vnfcNodeId,hostId);
						}
					});
					break;
				case 1 :
					serviceMgr.execute(new Runnable() {
						public void run() {
							String monitorConfigId = String.valueOf(params.get("itemName"));
							String itemId = zabbixDriver.createItem(params);
							System.out.println("itemId is " + itemId);
							map.put(monitorConfigId,itemId);
						}
					});
					break;
				case 2 :
					serviceMgr.execute(new Runnable() {
						public void run() {
							System.out.println("in the execute\n");
							System.out.println(params);
							String itemId = String.valueOf(params.get("itemids"));
							String itemValue = zabbixDriver.getHistory(params);
							System.out.println("itemValue is " + itemValue);
							map.put(itemId,itemValue);
						}
					});			
					break;
			}

		}catch(RejectedExecutionException e) {
			int taskNumLocal = taskNumber.incrementAndGet();
			//the work queue is full , and the task will be drop
			System.out.println("the queue is blocked, task " + taskNumLocal + " has been dropped");
		}
	}
	/**
	 * waiting for the task which is in queue or running to finish 
	 */
	public void slowStop() {
		try {
			serviceMgr.shutdown();
			serviceMgr.awaitTermination(Long.MAX_VALUE,TimeUnit.SECONDS);
		}
		finally {
			System.out.println("Service Finish");
		}
	}
	
	
    /**
     * cancel all running tasks and clear the queue	
     */
	public void quickStop() {
		try {
			System.out.println("quickStop begin");
			int tasksInQueue = serviceMgr.shutdownNow().size();
			System.out.println("the number of the tasks in queue has been cancelled is " + tasksInQueue);
			int tasksAtRun = serviceMgr.getTaskCancel().size();
			System.out.println("the number of the tasks at run has been cancelled is " + tasksAtRun);
		}catch(IllegalStateException e) {
			System.out.println("quickStop error");
		}
		finally {
			System.out.println("quickStop finish");
		}
	}
	
/*	public static void main(String[] args) {
		int threadNumber = 5;
		int queueSize = 5;
		ServiceMgr serviceManager = new ServiceMgr(threadNumber, queueSize);
		for(int i = 1 ; i < 7 ; i++) {
			serviceManager.handler();
		}
		try {
			Thread.sleep(2000);
		}catch(InterruptedException e) {
		}finally {
			serviceManager.quickStop();
		}
	}*/
}
