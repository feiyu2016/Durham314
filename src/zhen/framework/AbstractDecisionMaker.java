package zhen.framework;

import zhen.implementation.graph.Event;

public abstract class AbstractDecisionMaker extends BaseComponent{

	public AbstractDecisionMaker(Framework frame) {
		super(frame);
	}

	public abstract Event[] nextEvent();
}
