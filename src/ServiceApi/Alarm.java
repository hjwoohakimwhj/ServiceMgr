package ServiceApi;

import net.sf.json.JSONObject;
import java.util.Iterator;
import java.util.List;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;
import java.util.HashMap;
import Mongo.MongoApi;

/**
 *Sends request to the mongoDB to acquire the current value of each  monitorTarget of all 
 *VNFs in all NSs.
 *Sends the monitorTargets' value to the AlarmFormat to calculate the newest alarm value
 * 
 */
public class Alarm implements Runnable{
	//alarmInfoMap: <nsTypeId, alarmInfoList> . Each  list contains all the alarmId of this vnf
	private Map<String,List<AlarmFormat>> alarmInfoMap = Collections.synchronizedMap(new HashMap<String, 
			List<AlarmFormat>>());
	private Map<String, Set<String>> nsToVnf = new HashMap<String,Set<String>>();
	private Map<String,JSONObject> targetMaterials = new HashMap<String,JSONObject>();

	private MongoApi mongoApi;

	public Alarm(MongoApi mongo) {
		this.mongoApi = mongo;
	}

	public void start() {
		System.out.println("alamr starts");
	}

	public void run() {
		System.out.println("alarm begins running");
		try {
			while(true) {
				// this request must contains all the monitor target
				this.sendToMongDB();
				this.copy();
				Thread.sleep(20000);
			}
		}catch(Exception e) {
			e.printStackTrace();
		}
	}

	
	private void sendToMongDB() {
		for(String nsTypeId : this.nsToVnf.keySet()) {
			for(String vnf_nid : this.nsToVnf.get(nsTypeId)) {
				JSONObject vnfMonitorTarget = this.mongoApi.getVnfMonitorTarget(vnf_nid);
				this.targetMaterials.put(vnf_nid, vnfMonitorTarget);
			}
		}
	}
	
	/*
	 * refresh all monitortargets 
	 */
	private void copy() {
		for(String nsTypeId : this.nsToVnf.keySet()) {
			for(String vnf : this.nsToVnf.get(nsTypeId)) {
				//string is the alarm id
				Map<String,FutureTask<Integer>> futureMap = new HashMap<String, FutureTask<Integer>>();
				JSONObject monitorTargets = this.targetMaterials.get(vnf);
				for(AlarmFormat alarm : this.alarmInfoMap.get(nsTypeId)) {
					Callable<Integer> callable = new Callable<Integer>() {
						public Integer call() throws Exception{
							try {
								alarm.copy(monitorTargets);
							}catch(Exception e) {
								return -1;
							}
							return 0;
						}
					};
					FutureTask<Integer> future = new FutureTask<Integer>(callable);
					String alarmId = alarm.getAlarmId();
					futureMap.put(alarmId, future);
					new Thread(future).start();
				}

				while(true) {
					boolean allDone = true;
					for(String alarmId : futureMap.keySet()) {
						if(!futureMap.get(alarmId).isDone()) {
							allDone = false;
							break;
						}
					}
					if(allDone) {
						break;
					}
					try {
						Thread.sleep(1000);
					}catch(Exception e) {
						e.printStackTrace();
					}
				}
			}	
		}
	}
	
	/*
	 *an interface to get the NS's alarm values 
	 *
	 *@param nsTypeId
	 * 
	 */
	public JSONObject getAlarmsStatus(String nsTypeId) {
		List<AlarmFormat> alarmList = this.alarmInfoMap.get(nsTypeId);
		JSONObject returnObj = new JSONObject();
		for(AlarmFormat format : alarmList) {
			String alarmId = format.getAlarmId();
			String status = format.getAlarmStatus();
			returnObj.put(alarmId, status);
		}
		return  returnObj;
	}
	
	/*
	 * an interface to add new NS's alarm
	 * 
	 * @param alarm the raw alarmInfo from the Slice O
	 * @param targetToVnf : maps each VNF to related monitorTargets
	 * 
	 */
	public void addAlarmInfo(JSONObject alarm,Map<String, List<String>> targetToVnf) {
		String nsTypeId = alarm.getString("nsTypeId");
		JSONObject alarmInfo = JSONObject.fromObject(alarm.get("alarmInfo"));
		Iterator<Object> alarmKey = alarmInfo.keys();
		while(alarmKey.hasNext()) {
			String alarmId = String.valueOf(alarmKey.next());
			JSONObject alarmEntry = alarmInfo.getJSONObject(alarmId);
			AlarmFormat alarmFormat = new AlarmFormat(alarmId,alarmEntry,targetToVnf);
			this.alarmInfoMap.get(nsTypeId).add(alarmFormat);
			Set<String> vnfSet = alarmFormat.getVnfSet();
			this.nsToVnf.get(nsTypeId).addAll(vnfSet);
		}
	}
}
