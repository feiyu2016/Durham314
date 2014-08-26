package inputGeneration;

import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public class LayoutNode {

	private String ID;
	private String Type;
	private Node Node;
	
	public LayoutNode(String type, String id, Node node) {
		ID = id;
		Type = type;
		Node = node;
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