package inputGeneration;


public class EventHandlers {

	public static final String[] eventHandlers = {
		// the name should be in the same format with xml layouts
		"android:onClick",
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
	
	public static int isStartActivity(String line) {
		int result = -1;
		for (int i = 0, len = intentSigs.length; i < len; i++)
			if (line.contains(intentSigs[i]))
				result = i;
		return result;
	}
	
	public static int isSetContentView(String line) {
		int result = -1;
		for (int i = 0, len = setContentViewSigs.length; i < len; i++)
			if (line.contains(setContentViewSigs[i]))
				result = i;
		return result;
	}
	
}
