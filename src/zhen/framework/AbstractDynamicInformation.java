package zhen.framework;

import java.util.ArrayList;
import java.util.List;

import zhen.implementation.graph.Event;
import zhen.implementation.graph.RunTimeLayout;

public abstract class AbstractDynamicInformation extends BaseComponent{
	public AbstractDynamicInformation(Framework frame){
		super(frame);
	}
	
	public abstract void update(Event... event);
	public abstract RunTimeLayout getCurrentLayout();
	public abstract List<Event> getEventSequence(RunTimeLayout layout);
//	public abstract RunTimeLayout[] findPotentialLayout(String actName, String id);
}
