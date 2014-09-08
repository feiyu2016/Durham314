package zhen.framework;

import com.android.hierarchyviewerlib.models.ViewNode;
import com.android.hierarchyviewerlib.models.Window;

public abstract class AbstractRunTimeInformation extends AbstractBaseFrameworkComponent {

	public AbstractRunTimeInformation(Framework frame) {
		super(frame);
	}
	
	public abstract ViewNode getFocusedLayout();
	public abstract ViewNode loadWindowData(Window win);
	public abstract Window getFocusedWindow();
	public abstract Window[] getWindowList();
	public abstract boolean isKeyBoardVisible();
	
	public abstract double processCPUUsage();
	public abstract double memoryUsage();
	public abstract int getBreakPointCount();
	public abstract void observe();
}
