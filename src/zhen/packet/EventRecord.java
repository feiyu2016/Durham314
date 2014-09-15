package zhen.packet;

import java.util.HashMap;
import java.util.Map;
/**
 * Representation of an event e.g. touch, click
 * On a view which is in an activity, an event occurred, which leads to a destination UI.
 * @author zhenxu
 */
public class EventRecord {
	public EventRecord(String eventType, String sourceAct){
		this.eventType = eventType;
		this.sourceActName = sourceAct;
	}
	public String sourceActName, destActName;
	public Map<String,String> viewProfile;
	public String eventType;
	public boolean triggerLayoutChange;
	public String targetUIInformation;
	public Map<String,String> additionalInfo = new HashMap<String,String>();

	public void recordFromViewProfile(Map<String,String> profile){
		viewProfile = profile;
	}
	
	public String toString(){
		return eventType+" occurs on ("+viewProfile.get("x")+","+viewProfile.get("y")+") at " +sourceActName+" leads to "+destActName;
	}
}
