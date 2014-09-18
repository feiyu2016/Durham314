package zhen.previous.framework;

public abstract class AbstractDecisionMaker extends AbstractBaseFrameworkComponent{
	
	public AbstractDecisionMaker(Framework frame) {
		super(frame);
	}

	public abstract Event[] nextEventSet();
}
