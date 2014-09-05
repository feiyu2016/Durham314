package zhen.framework;

public abstract class AbstractRunTimeInformation extends AbstractBaseFrameworkComponent {

	public AbstractRunTimeInformation(Framework frame) {
		super(frame);
	}
	
	public abstract RunTimeLayout captureRunTimeLayout();
	public abstract double processCPUUsage();
	public abstract double memoryUsage();
	public abstract int getBreakPointCount();
}
