package zhen.underConsturction;

import zhen.graph.Event;

public abstract class AbstractDecisionMaker {
	protected Framework frame;
	public AbstractDecisionMaker(Framework frame) {
		this.frame = frame;
	}
	public abstract void init();
	public abstract Event generateEvent();
}
