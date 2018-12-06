package ZabbixDriver;
import net.sf.json.*;

/**
 * this class 
 * 
 */
public class MediaUser {
	private final JSONObject mediaUser = new JSONObject();
	public MediaUser(String mediaTypeId,String period,String sendTo) {
		registerMedia(mediaTypeId,period,sendTo);
	}
	
	//0 - (default) not classified; 1 - information; 2 - warning; 3 - average; 4 - high; 5 - disaster.
	public void registerMedia(String mediaTypeId,String period,String sendTo) {
		mediaUser.put("active", 0);
		mediaUser.put("mediatypeid", mediaTypeId);
		mediaUser.put("period", period);
		mediaUser.put("sendto", sendTo);
		//mediaUser.put("userid", userId);
		mediaUser.put("severity", 0);
	}
	
	public JSONObject getMediaUser() {
		return mediaUser;
	}
}
