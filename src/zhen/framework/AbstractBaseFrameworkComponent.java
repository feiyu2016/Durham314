package zhen.framework;

public abstract class AbstractBaseFrameworkComponent {
	private Framework frame;
	public AbstractBaseFrameworkComponent(Framework frame){
		this.frame = frame;
	}
	
	public Framework getFramework(){
		return this.frame;
	}
	
	public abstract void onLaunchApplication();
	public abstract void onQuitApplication();
	public abstract void init();
	public abstract void terminate();
}
