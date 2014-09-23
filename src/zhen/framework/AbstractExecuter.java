package zhen.framework;

import java.util.List;

import zhen.implementation.graph.Event;

public abstract class AbstractExecuter extends BaseComponent{
	public AbstractExecuter(Framework frame){
		super(frame);
	}
	public abstract boolean carryOutEvent(Event... event);
	
	/**
	 * The ith single list of LogCatFeedBack represents the response from the ith event 
	 * @return
	 */
	public abstract List<List<String>> getInstrumentationFeedBack();
	
	public static class LogCatFeedBack{
		////I( 4771: 4771) clickme3
		String msg;
		String level; 
		public LogCatFeedBack(String msg){
			this.msg = msg;
		}
		
		public String toString(){
			return this.msg;
		}
	}
}
