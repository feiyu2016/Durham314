package zhen.test.component;

import java.util.List;

public class TestUtility {

	public static void printLineByLine(List list){
		if(list == null){
			System.out.println("null");
			return;
		}
		if(list.isEmpty()){
			System.out.println("empty");
			return;
		}
		for(Object o :list){
			System.out.println(o);
		}
	}
	
	public static void printLineByLine(String tag, List list){
		System.out.println(tag);
		if(list == null){
			System.out.println("null");
			return;
		}
		if(list.isEmpty()){
			System.out.println("empty");
			return;
		}
		for(Object o :list){
			System.out.println(o);
		}
	}
	
	public static void log(String msg){
		System.out.println(msg);
	}
}
