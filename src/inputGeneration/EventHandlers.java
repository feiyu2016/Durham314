package inputGeneration;


public class EventHandlers {

	public static final String[] eventHandlers = {
		"android:OnClick"
	};
	
	public static boolean isEventHandler(String attribute) {
		boolean result = false;
		for (String eH: eventHandlers)
			if (eH.equals(attribute))
				result = true;
		return result;
	}
	
}
