package zhen.test.component;

import java.util.ArrayList;

public class generalTest {
	public static void main(String[] args){
//		ArrayList<String> ad = new ArrayList<String>();
//		ad.add("qwe");
//		
//		System.out.println("before : "+ad);
//		ArrayList<String> qe = new ArrayList<String>(ad);
//		qe.add("qw1e");
//		System.out.println("after : "+ad);
		
		
		String as = "I(10395:10395) METHOD_STARTING,<com.example.backupHelper.BackupActivity: boolean onMenuItemSelected(int android.view.MenuItem)>";
	
	
		System.out.println(as.split("METHOD_STARTING,<")[1].split(">")[0]);
	}
}
