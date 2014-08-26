package inputGeneration;


public class EventHandlers {

	public static final String[] eventHandlers = {
		// the name should be in the same format with xml layouts
		"android:OnClick"
	};
	
	public static final String[] intentSigs = {
		// must be virtual invoke
		": void startActivity(android.content.Intent)>",
		": void startActivity(android.content.Intent,android.os.Bundle)>",
		": void startActivityForResult(android.content.Intent,int)>",
		": void startActivityForResult(android.content.Intent,int,android.os.Bundle)>",
		": void startActivityFromChild(android.app.Activity,android.content.Intent,int)>",
		": void startActivityFromChild(android.app.Activity,android.content.Intent,int,android.os.Bundle)>",
		": void startActivityFromFragment(android.app.Fragment,android.content.Intent,int)>",
		": void startActivityFromFragment(android.app.Fragment,android.content.Intent,int,android.os.Bundle)>",
		": boolean startActivityIfNeeded(android.content.Intent,int)>",
		": boolean startActivityIfNeeded(android.content.Intent,int,android.os.Bundle)>",
		": void startActivities(android.content.Intent[])>",
		": void startActivities(android.content.Intent[],android.os.Bundle)>"
	};
	
	public static final String[] setContentViewSigs = {
		// must be virtual invoke
		": void setContentView(int)>",
		": void setContentView(android.view.View)>",
		": void setContentView(android.view.View,android.view.ViewGroup$LayoutParams)>"
	};
	
	public static boolean isEventHandler(String attribute) {
		boolean result = false;
		for (String eH: eventHandlers)
			if (eH.equals(attribute))
				result = true;
		return result;
	}
	
}
