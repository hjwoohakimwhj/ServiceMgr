package ZabbixDriver;
import net.sf.json.*;
public abstract class RequestBody {
	public JSONObject body;
	
	public RequestBody(String method,JSONObject params) {
		this(method,params,null);
	}	
	
	public RequestBody(String method,JSONObject params,String auth){
		body = new JSONObject();		
		if(setMethod(method)) {
			setJsonrpc();
			setId();
			if(!method.equals("user.login")) {
				setAuth(auth);
			}
			setParams(method,params);
		}
	}
	
	//return true means success
	public void setJsonrpc() {
		body.put("jsonrpc", "2.0");
	}
	
	public void setAuth(String auth) {
		if (auth != null) {
			body.put("auth", auth);
		}
	}
	
	public void setId() {
		body.put("id", 1);
	}
	
	public boolean setMethod(String method) {
		if(checkMethod(method)) {
			body.put("method",method);
			return true;
		}
		return false;	
	}
		
	public abstract boolean checkMethod(String method);
	
	public abstract void setParams(String method, JSONObject params);
	
	public String getBodyString() {
		return body.toString();
	}
}
