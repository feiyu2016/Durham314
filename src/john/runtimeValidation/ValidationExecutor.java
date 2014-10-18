package john.runtimeValidation;

import main.Paths;
import zhen.version1.component.WindowInformation;

public class ValidationExecutor {
	public static final String DOWN_AND_UP = "MonkeyDevice.DOWN_AND_UP";
	
	private String serial = null;
	
	public ValidationExecutor(String serial) {
		this.serial = serial;
	}
	
	public void touch(String x, String y) {
		try {
			Process pc = Runtime.getRuntime().exec(Paths.adbPath + " -s " + serial + " shell input tap " + x + " " + y );
			pc.waitFor();
			pc.destroy();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void closeKeyboard() {
		WindowInformation[]  visibleWindows = WindowInformation.getVisibleWindowInformation(serial);
		for(WindowInformation vwin : visibleWindows){
			//TODO to improve
			if(vwin.name.toLowerCase().contains("inputmethod")){
				try {
					Process pc = Runtime.getRuntime().exec(Paths.adbPath + " -s " + serial + " shell input keyevent 4");
					pc.waitFor();
					pc.destroy();
				} catch(Exception e) {}
				break;
			}
		}
	}
}
