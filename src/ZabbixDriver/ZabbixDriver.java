package ZabbixDriver;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
/*
 * need the deployFile(JSONObject)
 */
public class ZabbixDriver {
	public String auth;
	public String hostId;
	public String interfaceId;
	public String itemId;
	public String hostName;
	public String triggerId;
	public String mediaTypeId;
	public String mediaId;
	public String actionId;
	public String itemKey;
	public final static String userId = "1";
	TestHttp httpTest = new TestHttp();
	
	private String userName;
	private String password;
	
	public ZabbixDriver(String userName,String password) {
		this.userName = userName;
		this.password = password;
	}
	
	public String getAuth() {
		JSONObject params = new JSONObject();
		params.put("user", this.userName);
		params.put("password", this.password);
		String method = "user.login";
		UserRequestBody body = new UserRequestBody(method, params);
		String response = httpTest.doPost(body.getBodyString());	
		return body.getAuth(response);
	}
	
	public String registerHostGroup(JSONObject vnfJSON) {
		System.out.println("============");
		System.out.println(vnfJSON);
		String auth = getAuth();
		String method = "hostgroup.create";
		HostGroupRequestBody body = new HostGroupRequestBody(method, vnfJSON,auth);
		String response = httpTest.doPost(body.getBodyString());	
		return body.getHostGroupId(response);
	}
	
	public String registerProxy(JSONObject mgmtNode) {
		String auth = getAuth();
		String method = "proxy.create";
		ProxyRequestBody body = new ProxyRequestBody(method, mgmtNode,auth);
		String response = httpTest.doPost(body.getBodyString());	
		return body.getProxyId(response);
	}

	public String registerHost(JSONObject hostInfo) {
		String auth = getAuth();

		JSONObject params = new JSONObject();
		params.put("ip", hostInfo.get("ip"));
		params.put("host",hostInfo.get("vnfcNodeId") );
		params.put("proxy_hostid", hostInfo.get("proxyId"));
		

		JSONArray groupIds = new JSONArray();
		JSONObject groupId = new JSONObject();
		groupId.put("groupid", hostInfo.get("hostGroupId"));
		groupIds.add(groupId);
		params.put("groups", groupIds);

		String method = "host.create";
		HostRequestBody body = new HostRequestBody(method, params,auth);
		String response = httpTest.doPost(body.getBodyString());	
		System.out.println(response);
		return body.getHostId(response);
	}
	
	public String getInterfaceId(String hostId,String auth) {
		String method = "hostinterface.get";
		JSONObject params = new JSONObject();
		params.put("hostId", hostId);
		InterfaceBody body = new InterfaceBody(method,params,auth);
		String response = httpTest.doPost(body.getBodyString());	
		return body.getInterfaceInfo(response);
	}
	
	public String createItem(JSONObject monitorConfig) {
		String auth = getAuth();
		String method = "item.create";
		String hostId = String.valueOf(monitorConfig.get("hostId"));
		String interfaceId = getInterfaceId(hostId,auth);
		monitorConfig.put("interfaceId",interfaceId);
		ItemRequestBody body = new ItemRequestBody(method,monitorConfig,auth);
		String response = httpTest.doPost(body.getBodyString());	
		return body.getItemId(response);
	}
	
	public String getHistory(JSONObject params) {
		String auth = getAuth();
		String method = "history.get";
		HistoryRequestBody body = new HistoryRequestBody(method,params,auth);
		String response = "";
		boolean blank = true;
		do {
			response = httpTest.doPost(body.getBodyString());
			blank = body.getResult(response);
			try {
				Thread.sleep(500);
			}catch(Exception e) {
				e.printStackTrace();
			}
		}while(blank);
		return body.getValue(response);
	}
	
	private void createTrigger(JSONObject deployFile) {
		String method = "trigger.create";
		JSONObject params = new JSONObject();
		params.put("comparison",deployFile.get("comparison"));
		params.put("valueCompare",deployFile.get("valueCompare"));
		params.put("compareUnit",deployFile.get("compareUnit"));
		params.put("function",deployFile.get("function"));
		params.put("hostName",this.hostName);
		params.put("itemName",this.itemKey);
		params.put("triggerName",this.hostName + "Trigger");
		params.put("unit",deployFile.get("unit"));
		if(String.valueOf(deployFile.get("unit")) == "time") {
			params.put("duration",deployFile.get("duration"));
			params.put("timeUnit",deployFile.get("timeUnit"));
		}else if (String.valueOf(deployFile.get("unit")) == "count") {
			params.put("count",deployFile.get("count"));
		}
		
		TriggerRequestBody body = new TriggerRequestBody(method,params,this.auth);
		String response = httpTest.doPost(body.getBodyString());
		System.out.println(response);
		this.triggerId = body.getTriggerId(response);
		System.out.println("Step 5 : triggerId is " + this.triggerId);
	}
	
	private void createMedia(Object execPath) {
		String mediaMethod = "mediatype.create";
		JSONObject paramsItem = new JSONObject();
		paramsItem.put("mediaName", this.hostName + "Media");
		paramsItem.put("execPath", execPath);	
		MediaType mediaType = new MediaType(mediaMethod,paramsItem,this.auth);
		String responseItem = httpTest.doPost(mediaType.getBodyString());
		this.mediaTypeId = mediaType.getMediaId(responseItem);
		System.out.println("Step 6 : mediaTypeId is " + this.mediaTypeId);
		
	}
	
	private void registerMedia(Object period,Object sendTo) {
		String mediaAdd = "user.addmedia";
		JSONObject paramsItem = new JSONObject();
		paramsItem.put("userId", this.userId);
		paramsItem.put("mediaTypeId", this.mediaTypeId);
		paramsItem.put("period", period);//周一到周日全天
		paramsItem.put("sendTo", sendTo);	
		UserRequestBody media = new UserRequestBody(mediaAdd,paramsItem,this.auth);
		String responseItem = httpTest.doPost(media.getBodyString());
		this.mediaId = media.getMediaIds(responseItem);
		System.out.println("Step 7 : mediad is " + this.mediaId);
	}
	
	private void createAction(Object array) {
		String method = "action.create";
		JSONObject paramsItem = new JSONObject();
		paramsItem.put("actionName",this.hostName + "Action");
		paramsItem.put("hostId",this.hostId);
		paramsItem.put("triggerId",this.triggerId);	
		JSONArray operations = JSONArray.fromObject(array);
		JSONArray operationsNew = new JSONArray();
		
		for(Object entry : operations) {
			JSONObject operation = JSONObject.fromObject(entry);
			JSONObject operationNew = new JSONObject();	
			
			String type = String.valueOf(operation.get("type"));
			operationNew.put("type", type);
			switch(type) {
				case "mail":
					operationNew.put("userId",this.userId);
					operationNew.put("mediaTypeId",this.mediaTypeId);
					break;
				case "command":
					operationNew.put("command",operation.get("command"));
					break;
				case "script":					
					operationNew.put("scriptid",operation.get("scriptId"));
					break;				
			}
			operationsNew.add(operationNew);
		}
		paramsItem.put("operations", operationsNew);
		ActionType actionType = new ActionType(method,paramsItem,auth);
		String responseItem = httpTest.doPost(actionType.getBodyString());
		System.out.println(responseItem);
		this.actionId = actionType.getActionId(responseItem);
		System.out.println("Step 8 : actionId is " + this.actionId);
	}

}
