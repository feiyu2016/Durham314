package zhen.backup;

import zhen.implementation.graph.Event;

public abstract class AbstractDecisionMaker {
	protected Framework frame;
	public AbstractDecisionMaker(Framework frame) {
		this.frame = frame;
	}
	public abstract void init();
	public abstract Event generateEvent();
}
