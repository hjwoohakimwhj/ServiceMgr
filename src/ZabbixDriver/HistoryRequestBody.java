package ZabbixDriver;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class HistoryRequestBody extends RequestBody{
	private static List<String> methodList = Collections.synchronizedList(
			new ArrayList<String>(){{add("history.get");}});
	
	public HistoryRequestBody(String method, JSONObject params,String auth) {
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
			case "history.get" :
				setHistoryGet(params);
				break;
			default :
				break;
		}
	}
	
	public void setHistoryGet(JSONObject params) {
		body.put("params", params);
		System.out.print("in the setHistoryGet");
		System.out.print(body);
	}
	
	public String getValue(String response) {
		return JSONHandler.getItemValue(response, "value");
	}
	
	public boolean getResult(String response) {
		JSONObject responseJSON = JSONObject.fromObject(response);
		JSONArray result = JSONArray.fromObject(responseJSON.get("result"));
		if(result.size() == 0) {
			return true;
		}
		return false;
	}
}
