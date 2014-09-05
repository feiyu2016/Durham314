package zhen.framework;

public class Framework implements Runnable{
	

	private AbstractDecisionMaker decisionMaker;
	private AbstractExecuter executer;
	private AbstractTraversalTree traversalTree;
	private AbstractRunTimeInformation dynamicInfo;
	private AbstractBaseFrameworkComponent staticInfo;
	private boolean working = true;
	private boolean pause = false;
	public Framework(){}
	
	private int eventCount = 0;
	
	@Override
	public void run() {
		init();
		OuterLoop: while(working){
			while(pause){
				try { Thread.sleep(200);
				} catch (InterruptedException e) { break OuterLoop; }
			}
			
			dynamicInfo.observe();
			Event event = decisionMaker.nextEvent();
			eventCount+=1;
			executer.execute(event);
		}
		stop();
	}
	
	private void init(){
		decisionMaker.init();
		executer.init();
		traversalTree.init();
		dynamicInfo.init();
		staticInfo.init();
	}
	
	public void stop(){
		working = false;
	}
	
	public void setPause(boolean pause){
		this.pause = pause;
	}

	private void terminate(){
		decisionMaker.terminate();
		executer.terminate();
		traversalTree.terminate();
		dynamicInfo.terminate();
		staticInfo.terminate();
	}
	
	public AbstractDecisionMaker getDecisionMaker() {
		return decisionMaker;
	}

	public void setDecisionMaker(AbstractDecisionMaker decisionMaker) {
		this.decisionMaker = decisionMaker;
	}

	public AbstractExecuter getExecuter() {
		return executer;
	}

	public void setExecuter(AbstractExecuter executer) {
		this.executer = executer;
	}

	public AbstractTraversalTree getTraversalTree() {
		return traversalTree;
	}

	public void setTraversalTree(AbstractTraversalTree traversalTree) {
		this.traversalTree = traversalTree;
	}

	public AbstractRunTimeInformation getRunInfo() {
		return dynamicInfo;
	}

	public void setRunInfo(AbstractRunTimeInformation runInfo) {
		this.dynamicInfo = runInfo;
	}

	public AbstractBaseFrameworkComponent getStaticInfo() {
		return staticInfo;
	}

	public void setStaticInfo(AbstractBaseFrameworkComponent staticInfo) {
		this.staticInfo = staticInfo;
	}







}
