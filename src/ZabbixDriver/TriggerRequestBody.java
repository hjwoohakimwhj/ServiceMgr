package ZabbixDriver;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.sf.json.JSONObject;
import net.sf.json.JSONArray;

public class TriggerRequestBody extends RequestBody{
	private static List<String> methodList = Collections.synchronizedList(
			new ArrayList<String>(){{add("trigger.create"); add("trigger.delete"); add("trigger.update");}});
	private static List<String> comparision =  Collections.synchronizedList(new ArrayList<String>() {
		{
			add(">");
			add("<");
			add("=");
			add("!=");
		}
	});
	
	private static List<String> function =  Collections.synchronizedList(new ArrayList<String>() {
		{
			add("avg(%)");
			add("sum(%)");
			add("min(%)");
			add("max(%)");
			add("last(%)");
			add("diff(%)");
		}
	});
	
	public synchronized void addComparision(String compare) {
		if (comparision.contains(compare)) {
			return;
		}
		comparision.add(compare);
	}
	
	public synchronized void addFunction(String func) {
		if(function.contains(func)) {
			return ;
		}
		function.add(func);
	}
	
	public TriggerRequestBody(String method, JSONObject params) {
		super(method,params);
	}
	
	public TriggerRequestBody(String method, JSONObject params, String auth) {
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
			case "trigger.create" :
				setTriggerCreate(params);
				break;
			case "trigger.delete" :
				setTriggerDelete(params);
				break;
			case "trigger.update" :
				setTriggerUpdate(params);
				break;
			default :
				break;
		}
	}
	
	public void setTriggerCreate(JSONObject params) {
		JSONObject parameter = new JSONObject();
		//parameters.put("priority",(int)params.get("priority"));
		parameter.put("description",String.valueOf(params.get("triggerName")));
		String expression = createExpression(params);
		parameter.put("expression", expression);
		JSONArray parameters = new JSONArray();
		parameters.add(parameter);
		body.put("params", parameters);
	}
	
	/*
	 * params: hostName itemName function unit duration timeUnit comparison valueCompare compareUnit
	 */
	public String createExpression(JSONObject params) {
		String expression = "{" + String.valueOf(params.get("hostName")) + ':' + 
				String.valueOf(params.get("itemName")) + ".";
		String functionExpr = function.get((int)(params.get("function")));
		if(String.valueOf(params.get("unit")).equals("time")) {
			//timeUnit must be s m h d w
			expression  = expression + functionExpr.replaceFirst(
					"%",String.valueOf(params.get("duration"))
					+ String.valueOf(params.get("timeUnit"))) + "}";			
		}else if(String.valueOf(params.get("unit")).equals("count")) {
			expression  = expression + functionExpr.replaceFirst("%","#" +String.valueOf(params.get("count"))) + "}";
		}else if(String.valueOf(params.get("unit")).equals("now")) {
			expression  = expression + functionExpr.replaceFirst("%","") + "}";
		}
		expression = expression + comparision.get((int)params.get("comparison")) + 
				String.valueOf(params.get("valueCompare")) + 
				String.valueOf(params.get("compareUnit"));
		return expression;
	}
	
	
	public void setTriggerDelete(JSONObject params) {
		List<String> triggerIds = new ArrayList<String>();
		triggerIds.add(String.valueOf(params.get("triggerId")));
		body.put("params", triggerIds);
	}
	
	//status=0 enable
	public void setTriggerUpdate(JSONObject params) {
		JSONObject obj = new JSONObject();
		obj.put("triggerid", String.valueOf(params.get("triggerId")));
		obj.put("status", (int)params.get("status"));
		body.put("params", obj);
	}
	
	public String getTriggerId(String response) {
		return JSONHandler.getId(response, "triggerids");
	}
}
