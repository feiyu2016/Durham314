package zhen.framework;

public abstract class AbstractExecuter extends AbstractBaseFrameworkComponent {

	public AbstractExecuter(Framework frame) {
		super(frame); 
	}

	
	public abstract boolean execute(Event... input);
}
