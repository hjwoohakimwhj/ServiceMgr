package ZabbixDriver;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.sf.json.JSONObject;

public class MediaType extends RequestBody {
	private static List<String> methodList = Collections.synchronizedList(
			new ArrayList<String>(){{add("mediatype.create"); add("mediatype.update"); add("mediatype.delete");}});
	
	public MediaType(String method, JSONObject params,String auth) {
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
			case "mediatype.create" :
				setMediaTypeCreate(params);
				break;
			case "mediatype.delete" :
				setMediaTypeDelete(params);
				break;
			case "mediatype.update" :
				setMediaTypeDelete(params);
				break;
			default :
				break;
		}
	}
	
	/*
	 * params: mediaName execPath 
	 * the type must be 1 -> script
	 */
	public void setMediaTypeCreate(JSONObject params) {
		JSONObject obj = new JSONObject();
		obj.put("description", String.valueOf(params.get("mediaName")));
		obj.put("type",1);
		obj.put("exec_path",String.valueOf(params.get("execPath")));
		obj.put("exec_params","{ALERT.SENDTO}\r\n{ALERT.SUBJECT}\r\n{ALERT.MESSAGE}\r\n");
		body.put("params", obj);
		
	}
	
	/*
	 * mediatypeId
	 * 
	 */
	public void setMediaTypeDelete(JSONObject params) {
		List<String> mediaType = new ArrayList<String>();
		mediaType.add(String.valueOf(params.get("mediatypeId")));
		body.put("params", mediaType);
		
	}
	
	/*
	 * mediatypeId status
	 * status=0 -> enable
	 */
	public void setMediaTypeUpdate(JSONObject params) {
		JSONObject obj = new JSONObject();
		obj.put("mediatypeidn", String.valueOf(params.get("mediatypeId")));
		obj.put("status", (int)params.get("status"));
		body.put("params", obj);
	}
	
	public String getMediaId(String response) {
		return JSONHandler.getId(response,"mediatypeids");
	}
}
