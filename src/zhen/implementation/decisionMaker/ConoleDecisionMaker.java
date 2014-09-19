package zhen.implementation.decisionMaker;

import java.util.Map;
import java.util.Scanner;

import zhen.framework.AbstractDecisionMaker;
import zhen.framework.Framework;
import zhen.implementation.graph.Event;

public class ConoleDecisionMaker extends AbstractDecisionMaker{

	private  Scanner sc;
	public ConoleDecisionMaker(Framework frame) {
		super(frame);
	}

	@Override
	public Event[] nextEvent() {
		System.out.println("Command:");
		String reading = sc.nextLine().trim();
		if(reading.equals("help")){
			System.out.println("");
		}
		return null;
	}

	@Override
	public boolean init(Map<String,Object> attribute) {
		sc = new Scanner(System.in);
		return true;
	}

	@Override
	public void terminate() {
		sc.close();
	}
}
