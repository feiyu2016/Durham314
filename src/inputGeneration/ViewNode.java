package inputGeneration;

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
	
	public ViewNode(String type, String id, Node node, boolean isCustom) {
		ID = id;
		if (ID.contains("/"))
			ID = ID.split("/")[1];
		Type = type;
		Node = node;
		isCustomView = isCustom;
		eventHandlers = new HashMap<String, String>();
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
}