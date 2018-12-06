package ServiceApi;
import net.sf.json.JSONArray;
import java.util.HashMap;
import java.util.HashSet;

import net.sf.json.JSONObject;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;

import org.apache.commons.lang.StringUtils;

public class AlarmFormat {
	public static final ScriptEngine jse = new ScriptEngineManager().getEngineByName("JavaScript");
	public static final List<String> compressType = new ArrayList<String>() {
		{
			add(".tar.gz");
			add(".tar");
			add(".gz");
			add(".tgz");
		}
	};
	
	public static final Map<String, String> compareType = new HashMap<String,String>(){
		{
			put("lt","<");
			put("gt",">");
			put("eq","==");
		}
	};

	private String alarmId = null;
	private String csarFilePath = null;
	private String packType = null;
	private String statFormat = null;
	private String relPath = null;
	private String outputEnv = null;
	private String comparison = null;
	private String threshold = null;
	//private String description = null;
	private String alarmStatus = "false"; //false or true
	private String tmpStatFormat;
	private String newestValue = null;
	//monitorTargetName , <name: , vnf: >
	private Map<String, Map<String,String>> involveMonitorTargets = new HashMap<String, Map<String,String>>();
	private Set<String> vnfSet = new HashSet<String>();
	
	public AlarmFormat(String alramId, JSONObject alarmInfo,Map<String, List<String>> targetToVnf) {
		this.alarmId = alarmId;
		filePathCopy(alarmInfo.getString("csarFilePath"));
		this.comparison = alarmInfo.getString("comparison");
		this.outputEnv = alarmInfo.getString("outputEnv");
		this.packType  = alarmInfo.getString("");
		this.statFormat = alarmInfo.getString("statFormat");
		this.relPath = alarmInfo.getString("relPath");
		this.threshold = alarmInfo.getString("threshold");
		//this.description = alarmInfo.getString("description");
		JSONArray monitorTargets = JSONArray.fromObject(alarmInfo.get("involveMetrics"));
		for(Object obj : monitorTargets) {
			String targetName = String.valueOf(obj);
			String vnf = this.searchVnf(targetToVnf,targetName);
			Map<String,String> targetMap = new HashMap<String,String>();
			targetMap.put("value", "");
			targetMap.put("vnf", vnf);
			this.vnfSet.add(vnf);
			involveMonitorTargets.put(targetName, targetMap);
		}
	}
	
	public Set<String> getVnfSet(){
		return this.vnfSet;
	}
	
	private String searchVnf(Map<String, List<String>> targetToVnf,String target) {
		for(String key : targetToVnf.keySet()) {
			if(targetToVnf.get(key).contains(target)) {
				return key;
			}
		}
		return "";
	}

	private void filePathCopy(String filePath) {
		String dir = "";
		for(String type : AlarmFormat.compressType) {
			if(filePath.endsWith(type)) {
				dir = StringUtils.substringBefore(filePath, type);
				break;
			}
		}
		if(!dir.equals("")) {
			this.csarFilePath = dir;
		}
	}
	
	private void setAlarmStatus(String status) {
		this.alarmStatus = status;
	}

	public  synchronized String getAlarmStatus() {
		return this.alarmStatus;
	}
	
	public String getAlarmId() {
		return this.alarmId;
	}
	
	public synchronized void copy(JSONObject monitorTargets) {
		this.refreshMonitorTargets(monitorTargets);
		String tmpStatFormat = new String(this.statFormat);
		String alarmExpression = this.refreshTempStat(tmpStatFormat);
		this.refreshNewestValue(alarmExpression);
		this.refreshStatus();
	}
	
	private void refreshMonitorTargets(JSONObject monitorTargets) {
		for(String monitorTarget : this.involveMonitorTargets.keySet()) {
			this.involveMonitorTargets.get(monitorTarget).put("value", monitorTargets.getString(monitorTarget));
		}
	}
	/*
	 * before running this function , we must run the refresh monitorTarget first
	 * result is the expression which remove all the "{{" and "}}"
	 */
	private String refreshTempStat(String tmpStatFormat) {
		for(String key : this.involveMonitorTargets.keySet()) {
			String newKey = "{{" + key + "}}";
			tmpStatFormat = tmpStatFormat.replace(newKey, this.involveMonitorTargets.get(key).get("value"));
		}
		return tmpStatFormat;
	}
	
	private void refreshNewestValue(String expression) {
		try {
			File dir = new File(this.csarFilePath);
			Process ps = Runtime.getRuntime().exec(expression, null, dir);
			ps.waitFor();
			BufferedReader br = new BufferedReader(new InputStreamReader(ps.getInputStream()));
			StringBuffer stringBr = new StringBuffer();
			String line;
			while((line = br.readLine()) != null) {
				stringBr.append(line);
			}
			this.newestValue = stringBr.toString();
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	private void refreshStatus() {
		String compare = this.compareType.get(this.comparison);
		String expression = this.newestValue + compare + this.threshold;
		try {
			this.setAlarmStatus(String.valueOf(jse.eval(expression)));
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
}
