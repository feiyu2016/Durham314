package zhen.test;

import java.util.ArrayList;

public class generalTest {
	public static void main(String[] args){
		ArrayList<String> ad = new ArrayList<String>();
		ad.add("qwe");
		
		System.out.println("before : "+ad);
		ArrayList<String> qe = new ArrayList<String>(ad);
		qe.add("qw1e");
		System.out.println("after : "+ad);
	}
}
