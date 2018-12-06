package Mongo;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoIterable;
import com.mongodb.client.model.Filters;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.bson.Document;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;

import net.sf.json.JSONObject;

public class MongoApi {
	private MongoClient mongoClient;
	private MongoDatabase mongoDatabase;
	private MongoClientOptions.Builder options = new MongoClientOptions.Builder();
	private MongoClientOptions mongoOptions;
	//private ServerAddress serverAddress;
	//private List<ServerAddress> addrs = new ArrayList<ServerAddress>();
	//private MongoCredential credential;
	//private List<MongoCredential> credentials = new ArrayList<MongoCredential>();
	public MongoApi(String ip) {
		//this.serverAddress = new ServerAddress(ip,27017);
		//this.addrs.add(this.serverAddress);

		//this.credential = MongoCredential.createScramSha1Credential("serviceMgr", "vnf", "123456".toCharArray());
		//this.credentials.add(credential);

		this.options.socketTimeout(0);
		this.options.connectTimeout(30000);
		this.options.maxWaitTime(5000);
		this.mongoOptions = options.build();
		this.mongoClient = new MongoClient(ip,this.mongoOptions);
		this.mongoDatabase = this.mongoClient.getDatabase("vnf");
	}

	public MongoApi() {
		this("192.168.0.20");
	}
	
	/*
	 * alarm module use this function to get the monitorTarget value
	 */
	public JSONObject getVnfMonitorTarget(String vnf) {
		JSONObject vnfMonitor = new JSONObject();
		MongoCollection<Document> collection = this.mongoDatabase.getCollection(vnf);
		FindIterable<Document> iterator = collection.find();
		MongoCursor<Document> mongoCursor = iterator.iterator();
		while(mongoCursor.hasNext()) {
			Document doc = mongoCursor.next();
			String monitorTarget = String.valueOf(doc.get("monitortarget"));
			String value = String.valueOf(doc.get("value"));
			vnfMonitor.put(monitorTarget, value);
		}
		return vnfMonitor;
	 }
	
	/*
	 * monitor moduler use this function to push the newest monitorTarget value
	 */
	public void putVnfTarget(String vnf,JSONObject monitorTargets) {
		MongoCollection<Document> collection;
		MongoIterable<String> collectionNames = this.mongoDatabase.listCollectionNames();
		boolean exist = false;
		for(String name : collectionNames) {
			if(name.equals(vnf)) {
				exist = true;
			}
		}
		if(exist) {
			System.out.println("exist");
			collection = this.mongoDatabase.getCollection(vnf);
			this.refreshMonitorTargets(collection, monitorTargets);
		}else {
			System.out.println("not exist");
			this.mongoDatabase.createCollection(vnf);
			collection = this.mongoDatabase.getCollection(vnf);
			this.createMonitorTargets(collection, monitorTargets);
		}
	}
	
	private void createMonitorTargets(MongoCollection<Document> collection, JSONObject monitorTargets) {
		List<Document> documents = new ArrayList<Document>();
		Iterator<Object> iterator = monitorTargets.keys();
		while(iterator.hasNext()) {
			String monitorTarget = String.valueOf(iterator.next());
			String value = String.valueOf(monitorTargets.get(monitorTarget));
			Document document = new Document("monitortarget",monitorTarget).append("value", value);
			documents.add(document);
		}
		collection.insertMany(documents);
	}

	private void refreshMonitorTargets(MongoCollection<Document> collection, JSONObject monitorTargets) {
		Iterator iterator = monitorTargets.keys();
		while(iterator.hasNext()) {
			String monitorTarget = String.valueOf(iterator.next());
			String value = String.valueOf(monitorTargets.get(monitorTarget));
			collection.updateOne(Filters.eq("monitortarget", monitorTarget), new Document("$set", new Document("value",value)));
		}
	}
	/*public static void main(String[] args) {
		MongoApi mongo = new MongoApi();
		JSONObject obj = new JSONObject();
		obj.put("target1","43");
		obj.put("target2","5");
		mongo.putVnfTarget("vnfc2", obj);
	}*/
}
