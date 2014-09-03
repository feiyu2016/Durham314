package viewer;

import java.util.List;
import java.util.Scanner;

import android.view.KeyEvent;

public class MonkeyWrapperTest {
	static MonkeyWrapper m;
	static List<String> arr;
	static ViewPositionData pos;
	public static void main(String[] args){
		m = new MonkeyWrapper();
		pos = new ViewPositionData();
		arr = pos.retrieveViewInformation();		
		System.out.println("testing");
//		testInteractiveModel();
//		testScriptCase();
		testPress();
	}

	private static void testPress(){
		Scanner sc = new Scanner(System.in);
		m.startInteractiveModel();
		while(true){
			String reading = sc.nextLine().trim().split(" ")[0];
			if(reading.equals("0")){
				break;
			}
			if(reading.equals("b")){
				m.interactiveModelPress(KeyEvent.KEYCODE_BACK+"");
			}
		}
		sc.close();
		m.stopInteractiveModle();
	}
	
	private static void testInteractiveModel(){
		m.startInteractiveModel();
		
		for(String msg : arr){
			String[] parts = msg.split(";");
//			System.out.println(Arrays.toString(parts));
			if(parts[0].contains("Button") || parts[0].contains("View")){
				double x = Double.parseDouble(parts[1]) + 50;
				double y = Double.parseDouble(parts[2]) + 50;
				m.interactiveModelTouch(x+"", y+"", MonkeyWrapper.DOWN_AND_UP);
			}
		}
		
		
		m.stopInteractiveModle();
	}
	
	private static void testScript(){
		m.testWithScript("/home/zhenxu/Desktop/a2.py");
	}
	
	private static void testScriptCase(){
		m.createTestCase();
		m.appendInitString();
		for(String msg : arr){
			String[] parts = msg.split(";");
			if(parts[0].contains("Button")){
				double x = Double.parseDouble(parts[1]) + 50;
				double y = Double.parseDouble(parts[2]) + 50;
				m.appendSleepString(3);
				m.appendTouchString(x+"",y+"" , MonkeyWrapper.DOWN_AND_UP);
			}
		}
		m.commitTestCase();
	}
}
