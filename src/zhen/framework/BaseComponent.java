package zhen.framework;

import java.util.Map;

public abstract class BaseComponent {
	protected Framework frame;
	public BaseComponent(Framework frame){
		this.frame = frame;
	}
	
	public abstract boolean init(Map<String,Object> attribute);
	public abstract void terminate();
}
