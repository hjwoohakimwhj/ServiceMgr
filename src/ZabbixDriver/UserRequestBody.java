package ZabbixDriver;
import net.sf.json.*;
import net.sf.json.JSONArray;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
public class UserRequestBody extends RequestBody{
	private static List<String> methodList = Collections.synchronizedList(
			new ArrayList<String>(){{add("user.login"); add("user.logout");add("user.addmedia");}});
	
	public UserRequestBody(String method, JSONObject params) {
		super(method,params);
	}
	
	public UserRequestBody(String method, JSONObject params, String auth) {
		super(method,params,auth);
	}
	
	public static synchronized void addUserMethod(String method) {
		methodList.add(method);
	}
	
	public boolean checkMethod(String method) {
		if(methodList.contains(method)) {
			return true;
		}
		return false;
	}
	
	public void setParams(String method, JSONObject params) {
		switch(method) {
			case "user.login" :
				setUserLogin(params);
				break;
			case "user.logout" :
				setUserLogout(params);
				break;
			case "user.addmedia" :
				setMediaAdd(params);
				break;
			default :
				break;
		}
	}
	
	/*
	 * parameters: user password
	 */
	public void setUserLogin(JSONObject params) {
		JSONObject parameters = new JSONObject();
		if(params.containsKey("user")&&params.containsKey("password")) {
			parameters.put("user", String.valueOf(params.get("user")));
			parameters.put("password", String.valueOf(params.get("password")));	
			body.put("params", parameters);
		}
	}
	
	//userId mediaTypeId period sendTo
	public void setMediaAdd(JSONObject params) {
		JSONObject parameters = new JSONObject();
		MediaUser mediaUser = new MediaUser(String.valueOf(params.get("mediaTypeId"))
				,String.valueOf(params.get("period")),String.valueOf(params.get("sendTo")));
		parameters.put("medias", mediaUser.getMediaUser());

		
		JSONObject user = new JSONObject();
		user.put("userid", params.get("userId"));
		JSONArray userIds = new JSONArray();
		userIds.add(user);
		parameters.put("users", userIds);

		body.put("params", parameters);

	}
	
	public void setUserLogout(JSONObject params) {		
		JSONArray parameters = new JSONArray();
		body.element("params", parameters);
	}
	
	public String getAuth(String response) {
		JSONObject responseJSON = JSONObject.fromObject(response);
		String auth = String.valueOf(responseJSON.get("result"));
		if(auth != null) {
			return auth;
		}
		return "error";
	}
	
	public String getMediaIds(String response) {
		return JSONHandler.getId(response,"mediaids");
	}
}
