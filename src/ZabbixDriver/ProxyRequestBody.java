package ZabbixDriver;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class ProxyRequestBody extends RequestBody{
	
	private static List<String> methodList = Collections.synchronizedList(
			new ArrayList<String>(){{add("proxy.create"); add("proxy.delete");}});
	
	public ProxyRequestBody(String method,JSONObject params,String auth) {
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
			case "proxy.create" :
				setProxyCreate(params);
				break;
			case "proxy.delete" :
				setProxyDelete(params);
				break;
			default :
				break;
		}
	}
	
	public void setProxyCreate(JSONObject params) {
		JSONObject parameters = new JSONObject();
		parameters.put("host",params.get("vnfcNodeId"));
		parameters.put("status","5");//    5 -> active mode
		body.put("params", parameters);	
	}
	
	//proxyId
	public void setProxyDelete(JSONObject params) {
		JSONArray proxyId = new JSONArray();
		proxyId.add(params.get("proxyId"));
		body.put("params",proxyId);
	}
	
	public String getProxyId(String response) {
		return JSONHandler.getId(response, "proxyids");
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
