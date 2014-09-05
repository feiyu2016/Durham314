package zhen.framework;

import com.android.hierarchyviewerlib.models.ViewNode;

public abstract class AbstractRunTimeInformation extends AbstractBaseFrameworkComponent {

	public AbstractRunTimeInformation(Framework frame) {
		super(frame);
	}
	
	public abstract ViewNode captureRunTimeLayout();
	public abstract String getCurrentWindowName();
	public abstract double processCPUUsage();
	public abstract double memoryUsage();
	public abstract int getBreakPointCount();
	public abstract void observe();
}
