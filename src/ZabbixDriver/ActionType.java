package ZabbixDriver;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class ActionType extends RequestBody {
	private static List<String> methodList = Collections.synchronizedList(
			new ArrayList<String>(){{add("action.create");  add("action.delete"); add("action.update");}});
	
	public ActionType(String method, JSONObject params,String auth) {
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
			case "action.create" :
				setActionCreate(params);
				break;
			case "action.delete" :
				setActionDelete(params);
				break;
			case "action.update" :
				setActionUpdate(params);
				break;
			default :
				break;
		}
	}
	/*
	 * 我们需要先知道跟这个action绑定的trigger
	 * we need the params including:
	 * 哪个主机hostId和哪个triggerName 以及operations这是arry结构
	 */
	public void setActionCreate(JSONObject params) {
		JSONObject filter = setActionFilter(params.get("hostId"),params.get("triggerId"));
		JSONArray operations = setActionOperations(params.get("operations"));
		JSONObject parameters = new JSONObject();
		parameters.put("name", params.get("actionName"));//action名字
		parameters.put("eventsource", 0);//0 表示与trigger相关
		parameters.put("status", 0);
		parameters.put("def_shortdata","{TRIGGER.NAME}: {TRIGGER.STATUS}");
		parameters.put("def_longdata","{TRIGGER.NAME}: {TRIGGER.STATUS}\r\nLast value: {ITEM.LASTVALUE}\r\n\r\n{TRIGGER.URL}");
		parameters.put("esc_period", 120);
		parameters.put("filter", filter);
		parameters.put("operations",operations);
		body.put("params", parameters);		
	}
	
	/*
	 * params must have the actionId
	 */
	public void setActionDelete(JSONObject params) {
		List<String> actionIds = new ArrayList<String>();
		actionIds.add(String.valueOf(params.get("actionId")));
		body.put("params", actionIds);
	}
	
	/*
	 * params must have the actionId and the status we want 
	 */
	public void setActionUpdate(JSONObject params) {
		JSONObject parameter = new JSONObject();
		parameter.put("actionid", params.get("actionId"));
		parameter.put("status",params.get("status"));		
	}

	public JSONObject setActionFilter(Object hostIdPara, Object triggerIdPara) {
		String hostId = String.valueOf(hostIdPara);
		String triggerId = String.valueOf(triggerIdPara);
		Map<String,Integer> properties = new HashMap<String,Integer>();
		properties.put("evaltype",Integer.valueOf(0));
		properties.put("operatorEqual",Integer.valueOf(0));
		properties.put("operatorLike",Integer.valueOf(2));
		properties.put("conditiontypeHost",Integer.valueOf(1));
		properties.put("conditiontypeTrigger",Integer.valueOf(2));
		
		JSONArray conditions = new JSONArray();
		
		JSONObject conditionHost = new JSONObject();
		conditionHost.put("conditiontype", properties.get("conditiontypeHost"));
		conditionHost.put("operator", properties.get("operatorEqual"));
		conditionHost.put("value", hostId);
		
		JSONObject conditionTrigger = new JSONObject();
		conditionTrigger.put("conditiontype", properties.get("conditiontypeTrigger"));
		conditionTrigger.put("operator", properties.get("operatorEqual"));
		conditionTrigger.put("value", triggerId);	
		
		conditions.add(conditionHost);
		conditions.add(conditionTrigger);
		
		JSONObject filter = new JSONObject();
		filter.put("evaltype", properties.get("evaltype"));
		filter.put("conditions",conditions);
		
		return filter;
		
	}
	
	/*
	 * 默认一定会产生一种media通知行为，然后如果用户有定义执行命令的话，再添加
	 * params: type
	 * 
	 */
	public JSONArray setActionOperations(Object params) {
		JSONArray operations = JSONArray.fromObject(params);
		JSONArray operationArray = new JSONArray();
		for(Object param : operations) {
			JSONObject operation = JSONObject.fromObject(param);
			String type = String.valueOf(operation.get("type"));
			switch(type) {
				case "mail" :
					/*
					 * userId mediaId 目前会出错
					 */
					operationArray.add(setMailOper(operation.get("userId"),operation.get("mediaTypeId")));
					break;
				case "command" :
					/*
					 * hostId command
					 */
					operationArray.add(setCommandOper(operation.get("hostId"),operation.get("command"),0));
					break;
				case "script" :
					/*
					 * hostId scriptId
					 */
					operationArray.add(setCommandOper(operation.get("hostId"),operation.get("scriptId"),4));
					break;					
			}
		}
		
	
		
		return operationArray;
	}
	
	public JSONObject setMailOper(Object userIdPara , Object mediaTypeIdPara){
		String userId = String.valueOf(userIdPara);
		String mediaTypeId = String.valueOf(mediaTypeIdPara);
		JSONObject operationMedia = new JSONObject();
		operationMedia.put("operationtype", 0);
		operationMedia.put("evaltype", 0);
		
		JSONObject opmessage = new JSONObject();
		opmessage.put("default_msg",1);
		opmessage.put("mediatypeid",mediaTypeId);
		operationMedia.put("opmessage",opmessage);
		
		JSONArray opmessageUser = new JSONArray();
		JSONObject user = new JSONObject();
		user.put("userid",userId);//多了一个空格
		opmessageUser.add(user);
		operationMedia.put("opmessage_usr", opmessageUser);
		return operationMedia;
	}
	
	public JSONObject setCommandOper(Object hostIdPara , Object commandPara, int type){
		String hostId = String.valueOf(hostIdPara);
		String command = String.valueOf(commandPara);

		JSONObject operationCommand = new JSONObject();
		operationCommand.put("operationtype", 1);
		operationCommand.put("evaltype", 0);
		
		JSONObject opCondition = new JSONObject();
		opCondition.put("conditiontype",14);
		opCondition.put("operator",0);
		opCondition.put("value","0");
		JSONArray opConditionArray = new JSONArray();
		opConditionArray.add(opCondition);
		operationCommand.put("opconditions",opConditionArray);
		
		JSONArray opcommandUsr = new JSONArray();
		JSONObject host = new JSONObject();
		host.put("hostid",hostId);
		opcommandUsr.add(host);
		operationCommand.put("opcommand_hst",opcommandUsr);
		JSONObject opCommand = new JSONObject();
		if(type==0) {
			opCommand.put("type",0);//0 - custom script; 4 - global script.
			opCommand.put("execute_on",0);//0 - Zabbix agent; 1 - Zabbix server. 
			opCommand.put("command",command);

		}else {
			opCommand.put("type",0);//0 - custom script; 4 - global script.
			opCommand.put("scriptid",command);
		}
		operationCommand.put("opcommand",opCommand);
		return operationCommand;
	}
	
	public String getActionId(String response) {
		return JSONHandler.getIntId(response, "actionids");
	}
}
