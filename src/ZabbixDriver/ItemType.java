package ZabbixDriver;

import java.util.HashMap;
import java.util.Map;
import java.util.Iterator;

import net.sf.json.JSONObject;

public class ItemType {
	private final Map<String,HashMap<String,Object>> memoryKey = new HashMap<String,HashMap<String,Object>>();
	private final Map<String,HashMap<String,Object>> osKey = new HashMap<String,HashMap<String,Object>>();
	private final Map<String,HashMap<String,Object>> cpuKey = new HashMap<String,HashMap<String,Object>>();
	private final Map<String,HashMap<String,Object>> processKey = new HashMap<String,HashMap<String,Object>>();
	private final Map<String,HashMap<String,Object>> serviceKey = new HashMap<String,HashMap<String,Object>>();
	private final Map<String,HashMap<String,Object>> networkKey = new HashMap<String,HashMap<String,Object>>();
	private final Map<String,HashMap<String,Object>> securityKey = new HashMap<String,HashMap<String,Object>>();
	

	public ItemType() {
		initiateMemoryItem();
		initiateOsItem();
		initiateCpuItem();
		initiateProcessItem();
		initiateSecurityItem();
		initiateServiceItem();
		initiateNetworkItem();
	}
	
	public void initiateMemoryItem() {
		
		memoryKey.put("availableMemory", new HashMap<String,Object>(
				json2HashMap("{'key_':'vm.memory.size[available]','type':0,'value_type':3}")));
		memoryKey.put("freeSwapSpace", new HashMap<String,Object>(
				json2HashMap("{'key_':'system.swap.size[,free]','type':0,'value_type':3}")));
		memoryKey.put("totalMemory", new HashMap<String,Object>(
				json2HashMap("{'key_':'vm.memory.size[total]','type':0,'value_type':3}")));
		memoryKey.put("totalSwapSpace", new HashMap<String,Object>(
				json2HashMap("{'key_':'system.swap.size[,total]','type':0,'value_type':3}")));		
		
	}
	
	public void initiateOsItem() {
		
		osKey.put("hostName", new HashMap<String,Object>(
				json2HashMap("{'key_':'system.hostname','type':0,'value_type':1}")));
		osKey.put("maxOpenFiles", new HashMap<String,Object>(
				json2HashMap("{'key_':'kernel.maxfiles','type':0,'value_type':3}")));
		osKey.put("numberLogin", new HashMap<String,Object>(
				json2HashMap("{'key_':'system.users.num','type':0,'value_type':3}")));
		osKey.put("systemUptime", new HashMap<String,Object>(
				json2HashMap("{'key_':'system.uptime','type':0,'value_type':3}")));				
	}
	
	public void initiateCpuItem() {
		
		cpuKey.put("cpuIdleTime", new HashMap<String,Object>(
				json2HashMap("{'key_':'system.cpu.util[,idle]','type':0,'value_type':0}")));
		cpuKey.put("cpuStealTime", new HashMap<String,Object>(
				json2HashMap("{'key_':'system.cpu.util[,steal]','type':0,'value_type':0}")));
		cpuKey.put("processLoad%Minites%", new HashMap<String,Object>(
				json2HashMap("{'key_':'system.cpu.load[percpu,avg%Minites%]','type':0,'value_type':0}")));	
	}
	
	public void initiateProcessItem() {
		
		processKey.put("processNum", new HashMap<String,Object>(
				json2HashMap("{'key_':'proc.num[]','type':0,'value_type':3}")));
		processKey.put("processRunNum", new HashMap<String,Object>(
				json2HashMap("{'key_':'proc.num[,,run]','type':0,'value_type':3}")));
	}
	
	public void initiateSecurityItem() {		
		securityKey.put("checksum%File%", new HashMap<String,Object>(
				json2HashMap("{'key_':'vfs.file.cksum[%File%]','type':0,'value_type':3}")));
	}
	
	public void initiateServiceItem() {		
		serviceKey.put("ssh", new HashMap<String,Object>(
				json2HashMap("{'key_':'net.tcp.service[ssh]','type':3,'value_type':3}")));
		serviceKey.put("ftp", new HashMap<String,Object>(
				json2HashMap("{'key_':'net.tcp.service[ftp]','type':3,'value_type':3}")));
		serviceKey.put("http", new HashMap<String,Object>(
				json2HashMap("{'key_':'net.tcp.service[http]','type':3,'value_type':3}")));
		serviceKey.put("ntp", new HashMap<String,Object>(
				json2HashMap("{'key_':'net.udp.service[ntp]','type':3,'value_type':3}")));
	}
	
	public void initiateNetworkItem() {		
		networkKey.put("outTraffic%Ethx%", new HashMap<String,Object>(
				json2HashMap("{'key_':'net.if.out[%Ethx%]','type':0,'value_type':3}")));
		networkKey.put("inTraffic%Ethx%", new HashMap<String,Object>(
				json2HashMap("{'key_':'net.if.in[%Ethx%]','type':0,'value_type':3}")));
		networkKey.put("totalTraffic%Ethx%", new HashMap<String,Object>(
				json2HashMap("{'key_':'net.if.total[%Ethx%]','type':0,'value_type':3}")));
		networkKey.put("netInterfaceList", new HashMap<String,Object>(
				json2HashMap("{'key_':'net.if.list','type':0,'value_type':4}")));
	}
	
	public HashMap<String,Object> json2HashMap(String json){
		HashMap<String,Object> map = new HashMap<String,Object>();
		JSONObject obj = JSONObject.fromObject(json);
		Iterator iterator = obj.keys();
		while(iterator.hasNext()) {
			String key = (String) iterator.next();
	        map.put(key, obj.get(key));
		}
		return map;
	}
	
	public Map<String,HashMap<String,Object>> getCpu(){
		return cpuKey;
	}
	
	public Map<String,HashMap<String,Object>> getMemory(){
		return memoryKey;
	}
	
	public Map<String,HashMap<String,Object>> getOs(){
		return osKey;
	}
	
	public Map<String,HashMap<String,Object>> getService(){
		return serviceKey;
	}
	
	public Map<String,HashMap<String,Object>> getProcess(){
		return processKey;
	}
	
	public Map<String,HashMap<String,Object>> getSecurity(){
		return securityKey;
	}
	
	public Map<String,HashMap<String,Object>> getNetwork(){
		return networkKey;
	}
}
