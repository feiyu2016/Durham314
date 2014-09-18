package zhen.previous.framework;

import java.util.HashMap;
import java.util.Map;

public class Event {
	public Event(int type){
		this.type = type;
		attribute = new HashMap<String,String>();
	}
	
	public int type;	
	public Map<String, String> attribute;
	public static final int SYSTEM = 1;
	public static final int MOTION = 2;
	public static final int KEY = 3;
	public static final int LAUNCH = 4;
	
	public static Event getLaunchEvent(String actName){
		Event e = new Event(LAUNCH);
		e.attribute.put("act", actName);
		return e;
	}
	
	/**
	 * Attribute list
	 * act
	 * 
	 */
}
