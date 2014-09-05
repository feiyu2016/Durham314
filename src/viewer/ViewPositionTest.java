package viewer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class ViewPositionTest {
	public static void main(String[] args){

		ViewPositionData.debug = true;
		ViewPositionData.debug_1 = true;
		ViewPositionData.debug_2 = true;
		
		ViewPositionData v = new ViewPositionData();
//		v.setDataFilter(new ViewPositionData.UnfilteredData());
		//mID  layout:getWidth()   layout:getHeight()   layout_x  layout_y
		//layout:layout_height layout:layout_width
		//layout:mLeft layout:mTop layout:mRight layout:mBottom
		
//		//index 0 is for name
//		v.setDataFilter(new ViewPositionData.StringValueRetriever(
//				"mID", " layout:getWidth()",  "layout:getHeight()",
//				"layout:mLeft" ,"layout:mTop" ,"layout:mRight", "layout:mBottom"
//				));
//		
//		
//		
//		List<String> info = v.retrieveViewInformation();
////		String result = ViewPositionData.selectRecordWithKey(info,1,"mID=id/button1");
////		System.out.println(result);
//		
////		System.out.println(info);
//		for(String msg : info){
//			String[] parts = msg.split(";");
//			System.out.println(Arrays.toString(parts));
//		}
		v.init();
		Scanner sc = new Scanner(System.in);
		String reading = null;
		while(sc.hasNext()){
			reading = sc.nextLine();
			if(reading.equals("0")) break;
			else{
				long time1 = System.currentTimeMillis();
				boolean b = v.isInputMethodVisibleWithoutInvalid();
				long time2 = System.currentTimeMillis();
				
				System.out.println((time2-time1)+" "+b);
			}
		}
		sc.close();
	}
}
