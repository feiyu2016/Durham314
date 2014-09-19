package zhen.framework;

import zhen.implementation.graph.Event;

public abstract class AbstractExecuter extends BaseComponent{
	public AbstractExecuter(Framework frame){
		super(frame);
	}
	public abstract boolean carryOutEvent(Event... event);
}
