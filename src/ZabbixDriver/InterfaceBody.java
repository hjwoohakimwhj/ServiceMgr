package ZabbixDriver;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class InterfaceBody extends RequestBody{
	private static List<String> methodList = Collections.synchronizedList(
			new ArrayList<String>(){{add("hostinterface.create"); add("hostinterface.update");
			add("hostinterface.delete"); add("hostinterface.get");}});
	
	public InterfaceBody(String method, JSONObject params,String auth) {
		super(method,params,auth);
	}
	
	public boolean checkMethod(String method) {
		if(methodList.contains(method)) {
			return true;
		}
		return false;
	}
	
	public static synchronized void addUserMethod(String method) {
		methodList.add(method);
	}
	
	public void setParams(String method, JSONObject params) {
		switch(method) {
			case "hostinterface.create" :
				setInterfaceCreate(params);
				break;
			case "hostinterface.delete" :
				setInterfaceDelete(params);
				break;
			case "hostinterface.update" :
				setInterfaceUpdate(params);
				break;
			case "hostinterface.get" :
				setInterfaceGet(params);
			default :
				break;
		}
	}
	
	/*if the interface is created by this method ,it can not be the default interface
	 *so main = 0 
	 *params:hostId ip 
	 */
	public void setInterfaceCreate(JSONObject params) {
		JSONObject parameter = new JSONObject();
		parameter.put("hostid", params.get("hostId"));
		parameter.put("dns", "");
		parameter.put("ip", params.get("ip"));
		parameter.put("main", 0);
		parameter.put("port", "10050");
		parameter.put("type", 1);
		parameter.put("useip", 1);
		body.put("params", parameter);		
	}
	
	//interfaceId
	public void setInterfaceDelete(JSONObject params) {
		JSONArray interfaceId = new JSONArray();
		interfaceId.add(params.get("interfaceId"));
		body.put("params",interfaceId);		
	}
	
	public void setInterfaceUpdate(JSONObject params) {

	}
	
	//hostId
	public void setInterfaceGet(JSONObject params) {
		JSONObject parameter = new JSONObject();
		parameter.put("output", "extend");
		parameter.put("hostids",params.get("hostId"));
		body.put("params", parameter);		
	}
	
	/*
	 * setInterfaceCreate
	 */
	public String getInterfaceId(String response) {
		JSONObject responseJSON = JSONObject.fromObject(response);
		JSONObject inteterfaceIds =  (JSONObject)responseJSON.get("result");
		JSONArray interfaceId = JSONArray.fromObject(inteterfaceIds.get("interfaceids"));
		List<String> interfaceList = JSONArray.toList(interfaceId,String.class);
		return interfaceList.get(0);
	}
	
	/*
	 * setInterfaceGet
	 */
	public String getInterfaceInfo(String response) {
		JSONObject responseJSON = JSONObject.fromObject(response);
		String inteterfaces =  String.valueOf(responseJSON.get("result"));
		JSONArray interfaceArray = JSONArray.fromObject(inteterfaces);
		
		/*JSONArray returnArray = new JSONArray();*/
/*		for(Object entry : interfaceArray) {
			JSONObject interfaceOne = JSONObject.fromObject(entry);
			JSONObject interfaceOneJSON = new JSONObject();
			interfaceOneJSON.put("interfaceId",interfaceOne.get("interfaceid"));
			interfaceOneJSON.put("ip",interfaceOne.get("ip"));
			interfaceOneJSON.put("port",interfaceOne.get("port"));
			returnArray.add(interfaceOneJSON);
		}*/
		return String.valueOf(((JSONObject)(interfaceArray.get(0))).get("interfaceid"));
	}
}
