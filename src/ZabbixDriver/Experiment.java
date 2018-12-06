package ZabbixDriver;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class Experiment {
	private static final String interfaceHost = "[{'type':1,'main':1,'useip':1,'dns':'','port':'10050'}]";
	
	public static void main(String[] args) {
		JSONArray interfaces = JSONArray.fromObject(interfaceHost);
		JSONObject item = JSONObject.fromObject(interfaces.get(0));
		item.put("ip", "sdom");
		JSONArray body = new JSONArray();
		body.add(item);
		System.out.print("body" + body);		
	}
}
