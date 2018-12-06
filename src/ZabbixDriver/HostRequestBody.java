package ZabbixDriver;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.sf.json.JSONObject;
import net.sf.json.JSONArray;

public class HostRequestBody extends RequestBody{
	
	//we use the same template for all hosts ,and disable all items.When we need to monitor some items ,we just
	//enable them.In addition ,for the items which the user custom , we'll use the ItemRequestBody to add it .
	//Linux OS server templateId = 10001
	private static final String interfaceHost = "[{'type':1,'main':1,'useip':1,'dns':'','port':'10050'}]";
	
	private static List<String> methodList = Collections.synchronizedList(
			new ArrayList<String>(){{add("host.create"); add("host.update"); add("host.delete");}});
	
	public HostRequestBody(String method,JSONObject params,String auth) {
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
			case "host.create" :
				setHostCreate(params);
				break;
			case "host.delete" :
				setHostDelete(params);
				break;
			case "host.update" :
				setHostUpdate(params);
				break;
			default :
				break;
		}
	}
	
	//we only need three params : hostName ip macAddress
	public void setHostCreate(JSONObject params) {
			
/*		JSONArray templates = JSONArray.fromObject(templateId);
		parameters.put("templates", templates);*/
			
		JSONArray interfaceList = JSONHandler.modifiedJSONList(interfaceHost, "ip", String.valueOf(params.get("ip")));
		params.put("interfaces", interfaceList);
		body.put("params", params);	
	}
	
	//hostId
	public void setHostDelete(JSONObject params) {
		JSONArray hostID = new JSONArray();
		hostID.add(params.get("hostId"));
		body.put("params",hostID);
	}
	
	//hostId status
	public void setHostUpdate(JSONObject params) {
		JSONObject parameters = new JSONObject();
		parameters.put("hostid", params.get("hostId"));
		parameters.put("status", params.get("status"));
		body.put("params", parameters);
	}
	
	public String getHostId(String response) {
		return JSONHandler.getId(response, "hostids");
	}
	

/*	public static void main(String[] args) {
		Map<String,Object> obj = new HashMap<String,Object>();
		obj.put("ip", "182.12.5.3");
		obj.put("host", "zabbix server");
		obj.put("MacAddress", "null");
		HostRequestBody body = new HostRequestBody("host.create",obj,"ss");
		System.out.println(body.getBodyString());
		733ae8bdde1196910a6218f8c2f7958e
	}*/
}
