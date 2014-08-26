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
	
	public ViewNode(String type, String id, Node node) {
		ID = id;
		Type = type;
		Node = node;
		eventHandlers = new HashMap<String, String>();
		parseEventHandlers();
	}
	
	private void parseEventHandlers() {
		NamedNodeMap attrs = Node.getAttributes();
		for (int i = 0, len = attrs.getLength(); i < len; i++) {
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
		Node node = Node.getAttributes().getNamedItem(EventType);
		if (node!=null)
			result = true;
		return result;
	}
	
	public String getEventHandler(String EventType) {
		String result = "";
		Node node = Node.getAttributes().getNamedItem(EventType);
		if (node!=null)
			result = node.getNodeValue();
		return result;
	}
	
	public void setEventHandler(String EventType, String methodName) {
		Element e = (Element) Node;
		e.setAttribute(EventType, methodName);
	}
}