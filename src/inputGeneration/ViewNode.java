package inputGeneration;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public class ViewNode {

	private String ID;
	private String Type;
	private Node Node;
	private Map<String, String> eventHandlers;
	private boolean isCustomView;
	private ArrayList<String> leavingInfo;
	
	public ViewNode(String type, String id, Node node, boolean isCustom) {
		ID = id;
		if (ID.contains("/"))
			ID = ID.split("/")[1];
		Type = type;
		Node = node;
		isCustomView = isCustom;
		eventHandlers = new HashMap<String, String>();
		leavingInfo = new ArrayList<String>();
		parseEventHandlers();
	}
	
	private void parseEventHandlers() {
		NamedNodeMap attrs = Node.getAttributes();
		for (int i = 0, len = attrs.getLength(); i < len; i++) {
			// attrName is EventHandler Type, attrValue is EventHandler Method
			String attrName = attrs.item(i).getNodeName();
			String attrValue = attrs.item(i).getNodeValue();
			if (EventHandlers.isEventHandler(attrName))
				eventHandlers.put(attrName, attrValue);
		}
	}
	
	public String getID() {	
		return ID;
	}
	
	public String getType() {
		return Type;
	}
 
	public NamedNodeMap getAttributes() {
		return Node.getAttributes();
	}
	
	public boolean hasEventHandler(String EventType) {
		boolean result = false;
		for (Map.Entry<String, String> entry: eventHandlers.entrySet()) {
			String key = entry.getKey();
			if (key.equals(EventType))
				result = true;
		}
		return result;
	}
	
	public String getEventHandler(String EventType) {
		String result = "";
		for (Map.Entry<String, String> entry: eventHandlers.entrySet()) {
			String key = entry.getKey();
			String value = entry.getValue();
			if (key.equals(EventType))
				result = value;
		}
		return result;
	}
	
	public void setEventHandler(String EventType, String methodName) {
		Element e = (Element) Node;
		e.setAttribute(EventType, methodName);
	}
	
	public Map<String, String> getAllEventHandlers() {
		return eventHandlers;
	}

	public boolean isCustomView() {
		return isCustomView;
	}
	
	public void addLeavingEventHandler(String info) {
		if (!leavingInfo.contains(info))
			leavingInfo.add(info);
	}
	
	public ArrayList<String> getLeavingInfo() {
		return leavingInfo;
	}
	
	public boolean hasLeavingEventHandlers() {
		if (leavingInfo.size()>0)
			return true;
		return false;
	}
	
	public Map<String, ArrayList<String>> getLeavingEvents(String activityName) {
		// format: (Event Handler1, target1, target2),...
		Map<String, ArrayList<String>> ehMap = new HashMap<String, ArrayList<String>>();
		// "StartActivity",foundTargetActvt?,inOnCreate?,inEventHandler?,classname,methodsig,linenumber,targetActvt,NumberOfOnCreate,NumberOfViews,actvt1,...(layout&&widgetID),...
		// "setContentView",foundTargetLayout?,inOnCreate?,inEventHandler?,classname,methodsig,linenumber,targetLayout,NumberOfOnCreate,NumberOfViews,actvt1,...(layout&&widgetID),...
			for (String lI : leavingInfo) {
				if (!activityName.equals(lI.split(",")[0]))	continue;
				String theEH = lI.split(",")[1];
				String target = lI.split(",")[2] + "," + lI.split(",")[3];
				if (!ehMap.containsKey(theEH)) {
					ArrayList<String> ts = new ArrayList<String>();
					ts.add(target);
					ehMap.put(theEH, ts);
				}
				else {
					ArrayList<String> ts = ehMap.get(theEH);
					if (!ts.contains(target))
						ts.add(target);
					ehMap.put(theEH, ts);
				}
			}
		return ehMap;
	}
	
	public ArrayList<String> getStayingEvents(String activityName) {
		ArrayList<String> results = new ArrayList<String>();
		for (Map.Entry<String, String> entry: eventHandlers.entrySet())
			results.add(entry.getKey());
		for (Map.Entry<String, ArrayList<String>> entry: getLeavingEvents(activityName).entrySet())
			if (results.contains(entry.getKey()))
				results.remove(entry.getKey());
		return results;
	}
	
	public ArrayList<String> getLeavingTargets(String actvtName, String eventHandler) {
		ArrayList<String> result = new ArrayList<String>();
		if (getLeavingEvents(actvtName).containsKey(eventHandler))
			result = getLeavingEvents(actvtName).get(eventHandler);
		return result;
	}
	
}