package ZabbixDriver;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class HostGroupRequestBody extends RequestBody{
	private static List<String> methodList = Collections.synchronizedList(
			new ArrayList<String>(){{add("hostgroup.create"); add("hostgroup.delete");}});
	
	public HostGroupRequestBody(String method,JSONObject params,String auth) {
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
			case "hostgroup.create" :
				setHostGroupCreate(params);
				break;
			case "hostgroup.delete" :
				setHostGroupDelete(params);
				break;
			default :
				break;
		}
	}
	
	public void setHostGroupCreate(JSONObject params) {
		JSONObject parameters = new JSONObject();
		parameters.put("name",params.get("vnfTypeId"));
		body.put("params", parameters);	
	}
	
	//hostGroupId
	public void setHostGroupDelete(JSONObject params) {
		JSONArray hostGroupId = new JSONArray();
		hostGroupId.add(params.get("hostGroupId"));
		body.put("params",hostGroupId);
	}
	
	public String getHostGroupId(String response) {
		return JSONHandler.getId(response, "groupids");
	}
}
