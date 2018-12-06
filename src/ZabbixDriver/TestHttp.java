package ZabbixDriver;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

public class TestHttp {
	private URI uri;
	private CloseableHttpClient httpClient = HttpClients.custom().build();
	
	public TestHttp() {
		try {
			this.uri = new URI(new String("http://192.168.0.20/zabbix/api_jsonrpc.php").trim());
		}catch(URISyntaxException e) {			
		}
	}
	
	public String doPost(String body) {
		try {
			HttpUriRequest httpRequest = RequestBuilder.post().setUri(this.uri)
					.addHeader("Content-Type", "application/json")
					.setEntity(new StringEntity(body,ContentType.APPLICATION_JSON)).build();
			CloseableHttpResponse response = httpClient.execute(httpRequest);
			HttpEntity entity = response.getEntity();
			byte[] data = EntityUtils.toByteArray(entity);
			String dataResponse = new String(data);
			return dataResponse;
		}catch(IOException e) {
			return "error";
		}
	}
	
/*	public static void main(String[] args) {
		TestHttp httpTest = new TestHttp();
		
		String method = "user.login";	
		JSONObject params = new JSONObject();
		params.put("user", "Admin");
		params.put("password", "zabbix");
		
		UserRequestBody body = new UserRequestBody(method,params);
		String response = httpTest.doPost(body.getBodyString());	
		String auth = body.getAuth(response);
		System.out.println("Token is " + auth);*/
		
/*		String hostMethod = "host.create";
		Map<String,Object> paramsHost = new HashMap<String,Object>();
		paramsHost.put("ip", "192.168.0.58");
		paramsHost.put("macAddress", "00:50:56:20:ba:74");
		paramsHost.put("hostName", "monitorOne");
		HostRequestBody bodyHost = new HostRequestBody(hostMethod,paramsHost,auth);
		String responseHost = httpTest.doPost(bodyHost.getBodyString());
		System.out.println(responseHost);*/
		
/*		
 * 		String hostMethod = "host.delete";
		Map<String,Object> paramsHost = new HashMap<String,Object>();
		paramsHost.put("hostId", "10106");
		HostRequestBody bodyHost = new HostRequestBody(hostMethod,paramsHost,auth);
		String responseHost = httpTest.doPost(bodyHost.getBodyString());
		System.out.println(responseHost);	*/
		
/*		interfaceId =3
 * 		Map<String,Object> paramsInterface = new HashMap<String,Object>();
		paramsInterface.put("hostId", "10107");
		InterfaceBody interfaceBody = new InterfaceBody("hostinterface.get",paramsInterface,auth);
		String responseInterface = httpTest.doPost(interfaceBody.getBodyString());
		JSONArray interfaceInfo = interfaceBody.getInterfaceInfo(responseInterface);
		System.out.println(interfaceInfo);*/
		
/*		String itemMethod = "item.create";
		JSONObject paramsItem = new JSONObject();
		paramsItem.put("itemName", "memory free");
		paramsItem.put("hostId", "10107");
		paramsItem.put("interfaceId", "3");
		paramsItem.put("updateTime", 30);
		JSONObject monitorInfo = new JSONObject();
		monitorInfo.put("type", "memory");
		monitorInfo.put("item", "availableMemory");
		paramsItem.put("monitorInfo", monitorInfo);
		ItemRequestBody itemBody = new ItemRequestBody(itemMethod,paramsItem,auth);
		System.out.println(itemBody.getBodyString());
		String responseItem = httpTest.doPost(itemBody.getBodyString());
		System.out.println(responseItem);*/
		
/*		String triggerMethod = "trigger.create";
		JSONObject paramsItem = new JSONObject();
		paramsItem.put("hostName","monitorOne");
		paramsItem.put("itemName","vm.memory.size[available]");
		paramsItem.put("comparison",1);
		paramsItem.put("triggerName","memory trigger");
		paramsItem.put("valueCompare","500");
		paramsItem.put("compareUnit","M");
		paramsItem.put("function",4);
		paramsItem.put("unit","now");	
		TriggerRequestBody itemBody = new TriggerRequestBody(triggerMethod,paramsItem,auth);
		System.out.println(itemBody.getBodyString());
		String responseItem = httpTest.doPost(itemBody.getBodyString());
		System.out.println(responseItem);*/
		
/*		String mediaMethod = "mediatype.create";
		JSONObject paramsItem = new JSONObject();
		paramsItem.put("mediaName", "mediaOne");
		paramsItem.put("execPath", "mail.sh");	
		MediaType mediaType = new MediaType(mediaMethod,paramsItem,auth);
		System.out.println(mediaType.getBodyString());
		String responseItem = httpTest.doPost(mediaType.getBodyString());
		System.out.println(responseItem);*/
		
/*		String mediaAdd = "user.addmedia";
		JSONObject paramsItem = new JSONObject();
		paramsItem.put("userId", "1");
		paramsItem.put("mediaTypeId", "4");
		paramsItem.put("period", "1-7,00:00-24:00");//周一到周日全天
		paramsItem.put("sendTo", "1227116218@qq.com");	
		UserRequestBody mediaType = new UserRequestBody(mediaAdd,paramsItem,auth);
		System.out.println(mediaType.getBodyString());
		String responseItem = httpTest.doPost(mediaType.getBodyString());
		System.out.println(responseItem);*/
		
/*		String methodAction = "action.create";
		JSONObject paramsItem = new JSONObject();
		paramsItem.put("actionName","actionOne");
		paramsItem.put("hostId","10107");
		paramsItem.put("triggerName", "memory trigger");
		JSONArray operations = new JSONArray();
		JSONObject operation = new JSONObject();
		operation.put("type", "mail");
		operation.put("userId", "1");
		operation.put("mediaId","4");
		operations.add(operation);
		paramsItem.put("operations", operations);
		ActionType actionType = new ActionType(methodAction,paramsItem,auth);
		System.out.println(actionType.getBodyString());
		String responseItem = httpTest.doPost(actionType.getBodyString());
		System.out.println(responseItem);
	}*/
	
}
