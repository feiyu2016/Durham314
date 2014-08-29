package viewer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Scanner;

import com.android.hierarchyviewer.device.Window;
import com.android.hierarchyviewer.scene.ViewNode;

public class CommandlineViewPositionTest {

	public static void main(String[] args){
		new CommandlineViewPositionTest().execute();
	}
	
	public CommandlineViewPositionTest(){ }
	
	public void execute(){
		Scanner in = new Scanner(System.in);
		ViewPositionData viewData = new ViewPositionData();
		

		viewData.setDataFilter(new ViewPositionData.UnfilteredData());
		
		ViewPositionData.SelectWindow inputMethodWindow = new ViewPositionData.SelectWindow() {
			@Override
			public Window select(Window[] wins) {
				for(Window win : wins){
					if(win.getTitle().toLowerCase().equals("inputmethod")){
						return win;
					}
				}
				return null;
			}
		};
		
//		viewData.setWindowSelecter(inputMethodWindow);
		
		
		viewData.debug = true;
		viewData.init();
		
		MonkeyWrapper monkey = new MonkeyWrapper();
		monkey.startInteractiveModel();
		ArrayList<String> infoBundle =  null;
		while(true){
			System.out.print("Command:");
			String command = in.nextLine().split(" ")[0];	
			if(command.equals("1")){
				System.out.println("Refresh list");
				infoBundle = showInformation(viewData);
				
			}else if(command.equals("2")){
				System.out.print("Choose index to click:");
				int arg_num = -1; 
				try{ arg_num = Integer.parseInt( in.nextLine().trim().split(" ")[0]);
				}catch(Exception e){ break; }
				if(arg_num < 0){ break; }
				String viewInfo = infoBundle.get(arg_num);
				String[] parts = viewInfo.split(";");
				double x = Double.parseDouble(parts[1]) + 50;
				double y = Double.parseDouble(parts[2]) + 50;
				monkey.interactiveModelTouch(x+"", y+"", MonkeyWrapper.DOWN_AND_UP);
			}else{
				break;
			}
		}
		monkey.stopInteractiveModle();
		in.close();
		System.exit(0);
	}
	static int count = 0;
	private ArrayList<String> showInformation(ViewPositionData viewData){
		ArrayList<String> infoBundle = viewData.retrieveViewInformation();
//		for(int i=0;i<infoBundle.size();i++){
//			System.out.println(i+":\t"+infoBundle.get(i));
//		}
		File towrite = new File("result/"+count+".txt");
		count += 1;
		try {
			PrintWriter writer = new PrintWriter(towrite);
			for(String info : infoBundle){
				info.replace("{", "");
				info.replace("}", "");
				String parts[] = info.split(",");
				for(String part : parts){
					writer.println(part);
				}
				writer.println("---------------------------");
			}
			writer.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return infoBundle;
	}
}
