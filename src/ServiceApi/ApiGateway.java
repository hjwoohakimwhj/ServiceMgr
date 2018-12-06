package ServiceApi;

import ServiceApi.MonitorFormat;
import ServiceApi.Alarm;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.List;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import Core.ServiceMgr;
import ZabbixDriver.ZabbixDriver;
import java.util.Map;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import Mongo.MongoApi;
/**
 * Servlet implementation class ApiGateway
 */
@WebServlet("/ApiGateway")
public class ApiGateway extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private ServiceMgr serviceMgr ;
	private ZabbixDriver zabbixDriver;
	private Alarm alarm;
	private MongoApi mongo ;
	
	//monitorTarget,
	private Map<String,MonitorFormat> requestMonitorInfo = Collections.synchronizedMap(
			new HashMap<String,MonitorFormat>());
	
	//<monitorTarget,monitorTargetValue>
	private Map<String,String> quickCache = Collections.synchronizedMap(new HashMap<String,String>());
	
	private Map<String, List<String>> vnfContains = new HashMap<String,List<String>>();

	public static JSONObject map2JSON(Map<String,String> map) {
		JSONObject obj = new JSONObject();
		for(String key : map.keySet()) {
			obj.put(key, map.get(key));
		}
		return obj;
	}
    /**
     * @see HttpServlet#HttpServlet()
     */
    public ApiGateway() {
        super();
		int threadNumber = 5;
		int queueSize = 5;
		serviceMgr = new ServiceMgr(threadNumber, queueSize);
		
		String userName = "Admin";
		String password = "zabbix";
		zabbixDriver = new ZabbixDriver(userName,password);
		this.mongo = new MongoApi();
		
		JSONObject params = new JSONObject();
		params.put("interval", 10000);
		serviceMgr.handler(params, this.zabbixDriver, this.requestMonitorInfo,this.serviceMgr,
				this.quickCache,this.mongo);//it will run all the time

		//initiate the alarm
		this.alarm = new Alarm(this.mongo);
		this.alarm.start();
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		request.setCharacterEncoding("UTF-8");
		String monitorTarget = request.getParameter("monitorTarget");
		MonitorFormat monitorFormat =  requestMonitorInfo.get(monitorTarget);
		String result = "null";
		if(request.getParameter("monConfigId") == null ) {
			result = this.quickCache.get(monitorTarget);
		}else {
			String monConfigId = request.getParameter("monConfigId");
			result = monitorFormat.request(zabbixDriver,serviceMgr,monConfigId);
		}
		response.getWriter().write(result);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("text/html;charset=UTF-8");
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader((ServletInputStream)request.getInputStream(),"utf-8"));
			StringBuffer stringBuffer = new StringBuffer();
			String tmp;
			while((tmp = br.readLine()) != null) {
				stringBuffer.append(tmp);
			}
			br.close();
			String acceptJSON = stringBuffer.toString();
			if(acceptJSON != null) {
				JSONObject monitorObject  = JSONObject.fromObject(acceptJSON);
				System.out.println(monitorObject);

				if(String.valueOf(monitorObject.get("flag")).equals("alarm")) {
					alarmHandler(monitorObject);
					response.getWriter().write("happy alert");
					return;
				}

				if(!String.valueOf(monitorObject.get("flag")).equals("monitor")) {
					response.getWriter().write("lacking the  monitor flag");
					return;
				}

				Iterator<Object> iterator = monitorObject.keys();
				while(iterator.hasNext()) {
					String key = String.valueOf(iterator.next());
					if(key.equals("flag")){
						continue;
					}
					JSONObject vnfMonitorBody = JSONObject.fromObject(monitorObject.get(key));
					this.copy(vnfMonitorBody);
				}
/*				Iterator<Object> iterator = monitorObject.keys();
				while(iterator.hasNext()) {
					String key = (String) iterator.next();
					switch(key) {
						case "Info" :
							infoHandler(monitorObject.get(key));
							break;
						case "VnfcNodes" :
							vnfcNodesHandler(monitorObject.get(key));
							break;
						case "MgmtNode" :
							mgmtNodeHandler(monitorObject.get(key));
							break;
						case "MonitorOptions" :
							monitorOptionsHandler(monitorObject.get(key));
							break;
					}
				}*/
			}
			response.getWriter().write("happy");
		}catch(Exception e){
			e.printStackTrace();
			response.getWriter().write("sad");
		}
	}
	
	private void copy(JSONObject monitorObject) {
		try {
			if(monitorObject.get("Info") == null) {
				throw new Exception("Info is missing");
			}
			String[] hostGroup = infoHandler(monitorObject.get("Info"));
			String hostGroupId = hostGroup[0];
			String vnfNodeId = hostGroup[1];

			if(monitorObject.get("MgmtNode") == null || hostGroupId == "null") {
				throw new Exception("MgmtNode is missing");
			}
			String proxyId = mgmtNodeHandler(monitorObject.get("MgmtNode"));

			if(monitorObject.get("VnfcNodes") == null) {
				throw new Exception("VnfcNodesId is missing");
			}
			HashMap<String,String>  hostsInfo  = new HashMap<String,String>();
			vnfcNodesHandler(monitorObject.get("VnfcNodes"),
					hostGroupId,proxyId,hostsInfo);

			if(monitorObject.get("MonitorOptions") == null) {
				throw new Exception("MonitorOptions is missing");
			}
			HashMap<String,String>  monitorToItem  = new HashMap<String,String>();
			monitorOptionsHandler(monitorObject.get("MonitorOptions")
					, hostsInfo,monitorToItem,vnfNodeId);
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	private void alarmHandler(JSONObject params) {
		this.alarm.addAlarmInfo(params,this.vnfContains);
	}

	/*
	 * zabbix hostgroup register
	 */
	private String[] infoHandler(Object vnfNodeId) throws Exception {
		JSONObject vnfId = JSONObject.fromObject(vnfNodeId);
		String vnfGroupId = "null";
		try {
     		if(vnfId.get("vnfNodeId") == null) {
     			throw new Exception("vnfNodeId is missing");
     		}
     		vnfGroupId = zabbixDriver.registerHostGroup(vnfId);//use vnfGroupId to create a host group for this vnf
		}catch(Exception e) {
			e.printStackTrace();
		}
		String[] vnfGroup = {vnfGroupId, String.valueOf(vnfId.get("vnfNodeId"))};
		return vnfGroup;
	}

	/*
	 * zabbix hosts register
	 */
	private void vnfcNodesHandler(Object vnfcNodes,String hostGroupId
			, String proxyId,HashMap<String,String> vnfcToHostId) throws Exception{
		JSONArray vnfcNodesArray = JSONArray.fromObject(vnfcNodes);
		Iterator<Object> iterator = vnfcNodesArray.iterator();
		while(iterator.hasNext()) {
			JSONObject hostInfo = JSONObject.fromObject(iterator.next());
			try {
     			if(hostInfo.get("vnfcNodeId") == null || hostInfo.get("ip") == null) {
     				throw new Exception("vnfcNodeId or ip is missing");
     			}
     			hostInfo.put("hostGroupId",hostGroupId);
     			hostInfo.put("proxyId", proxyId);
     			vnfcToHostId.put(String.valueOf(hostInfo.get("vnfcNodeId")), "null");
     			serviceMgr.handler(hostInfo,this.zabbixDriver,vnfcToHostId,0);
			}catch(Exception e) {
				e.printStackTrace();
			}
		}
		while(true) {
			int count = 0;
			for(String key :  vnfcToHostId.keySet()) {
				if(vnfcToHostId.get(key) == "null") {
					break;
				}
				count++;
			}
			if(count == vnfcToHostId.size()) {
				break;
			}
		}
	}
	
	/*
	 * zabbix proxy register
	 */
	private String mgmtNodeHandler(Object mgmtNode) throws Exception{
		JSONObject mgmtNodeInfo = JSONObject.fromObject(mgmtNode);
		String returnValue = "null";
		try {
   			if(mgmtNodeInfo.get("vnfcNodeId") == null || mgmtNodeInfo.get("ip") == null) {
   				throw new Exception("vnfcNodeId or ip is missing");
   			}
   			returnValue = zabbixDriver.registerProxy(mgmtNodeInfo);
		}catch(Exception e) {
			e.printStackTrace();
		}
		return returnValue;
	}
	
	private void monitorOptionsHandler(Object monitorOptions,HashMap<String,String> vnfcToHostId,
			HashMap<String,String> monitorToItem,String vnfNodeId ){
		JSONArray monitorOptionsArray = JSONArray.fromObject(monitorOptions);
		Iterator<Object> iterator = monitorOptionsArray.iterator();
		List<String> vnfMonitorTargetsChain = new ArrayList<String>();
		while(iterator.hasNext()) {
			JSONObject monitorTarget = JSONObject.fromObject(iterator.next());
			String monitorTargetString = "null";
			for(Object monitorOne :  monitorTarget.keySet()) {
				monitorTargetString = String.valueOf(monitorOne);
				break;
			}
			if(monitorTargetString == "null") {
				break;
			}
			vnfMonitorTargetsChain.add(monitorTargetString);
			JSONObject monitorTargetBody = JSONObject.fromObject(monitorTarget.get(monitorTargetString));
			JSONArray parameters = JSONArray.fromObject(monitorTargetBody.get("parameters"));
			Object updateTime = monitorTargetBody.get("updateTime");
			MonitorFormat format = new MonitorFormat(monitorTargetBody.get("format"),
					 monitorTargetString,vnfNodeId);
			Iterator<Object> iteratorParam = parameters.iterator();
			while(iteratorParam.hasNext()) {
				JSONObject monConfigId = JSONObject.fromObject(iteratorParam.next());
				String monitorConfigIdKey = "null";
				for(Object monitorOne :  monConfigId.keySet()) {
					monitorConfigIdKey = String.valueOf(monitorOne);
					break;
				}
				System.out.println(monitorConfigIdKey);
				JSONObject monitorConfigBody = JSONObject.fromObject(monConfigId.get(monitorConfigIdKey));
				String hostId = vnfcToHostId.get(String.valueOf(monitorConfigBody.get("target")));
				JSONObject params = new JSONObject();
				params.put("hostId", hostId);
				params.put("updateTime", updateTime);
				params.put("itemName", monitorConfigIdKey);
				//lack interface id
				String url = String.valueOf(JSONObject.fromObject(monitorConfigBody.get("script")).get("url"));
				System.out.println("==" + url);
				JSONObject monitorInfo = parseUrl(url);
				params.put("monitorInfo", monitorInfo);
				monitorToItem.put(monitorConfigIdKey, "null");
				serviceMgr.handler(params, zabbixDriver, monitorToItem,1);
			}
			while(true) {
				int count = 0;
				for(String key :  monitorToItem.keySet()) {
					if(monitorToItem.get(key) == "null") {
						break;
					}
					count++;
				}
				if(count == monitorToItem.size()) {
					break;
				}
			}
			format.mapItemId(monitorToItem);
			this.requestMonitorInfo.put(format.getMonitorTarget(), format);
		}
		this.vnfContains.put(vnfNodeId, vnfMonitorTargetsChain);
	}
	
	private JSONObject parseUrl(String url) {
		JSONObject returnUrl = new JSONObject();
		String newString = url.substring(21);
		String[] strs = newString.split("/");
		returnUrl.put("type", strs[0]);
		if(strs[1].contains("%")) {
			String[] strList = strs[1].split("%");
			String param = strList[1];
			String item = "null";
			if(strs[0] == "cpu") {
				item = strList[0] + "%Minites%";
			}
			if(strs[0] == "network") {
				item = strList[0] + "%Ethx%";
			}
			returnUrl.put("item", item);
			returnUrl.put("param",param);
		}else {
			returnUrl.put("item", strs[1]);
		}
		return returnUrl;
	}
}
