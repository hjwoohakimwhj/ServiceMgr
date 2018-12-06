package ZabbixDriver;
import java.util.List;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class JSONHandler {
	//like "[{'type':1,'main':1,'useip':1,'dns':'','port':'10050'}]"
	public static JSONArray modifiedJSONList(String listJSON, String key, String value) {
		JSONArray interfaces = JSONArray.fromObject(listJSON);
		JSONObject item = JSONObject.fromObject(interfaces.get(0));
		item.put(key, value);
		JSONArray body = new JSONArray();
		body.add(item);
		return body;
	}
	
	public static String getId(String response,String uniqueId) {
		JSONObject responseJSON = JSONObject.fromObject(response);
		JSONObject responseIds =  (JSONObject)responseJSON.get("result");
		JSONArray responseId = JSONArray.fromObject(responseIds.get(uniqueId));
		List<String> ids = JSONArray.toList(responseId,String.class);
		return ids.get(0);
	}
	
	public static String getIntId(String response,String uniqueId) {
		JSONObject responseJSON = JSONObject.fromObject(response);
		JSONObject responseIds =  (JSONObject)responseJSON.get("result");
		JSONArray responseId = JSONArray.fromObject(responseIds.get(uniqueId));
		List<Integer> ids = JSONArray.toList(responseId,Integer.class);
		return String.valueOf(ids.get(0));
	}
	
	public static String getItemValue(String response, String value) {
		JSONObject responseJSON = JSONObject.fromObject(response);
		JSONArray responseIds =  (JSONArray)responseJSON.get("result");
		String returnValue = "null";
		for(Object responseId : responseIds) {
			JSONObject responseIdJSON = JSONObject.fromObject(responseId);
			returnValue = String.valueOf(responseIdJSON.get("value"));
		}
		return returnValue;
	}
	
	public static String replaceItemChar(String string,String paramOne) {
		int start = string.indexOf("%");
		int end = string.lastIndexOf("%");
		String newString = string.substring(0,start) + paramOne + string.substring(end+1, string.length());
		return newString;
	}
}
