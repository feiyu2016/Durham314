package viewer;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Stack;

import com.android.ddmlib.AndroidDebugBridge;
import com.android.ddmlib.Device;
import com.android.hierarchyviewer.device.DeviceBridge;
import com.android.hierarchyviewer.device.Window;
import com.android.hierarchyviewer.scene.ViewManager;
import com.android.hierarchyviewer.scene.ViewNode;
import com.android.hierarchyviewer.scene.WindowsLoader;

public class ViewPositionData {
	private ArrayList<Device> devices = new ArrayList<Device>();
	private SelectDevice deviceSelecter = null;
	private SelectWindow windowSelecter = null;
	private NodeDataFilter filter = null;
	private boolean terminating = false;
	public static boolean debug = false;
	public static boolean debug_1 = false;
	public static boolean debug_2 = false;
	
	public ViewPositionData(){
		deviceSelecter = new ViewPositionData.SelectFirstDevice();
		windowSelecter = new ViewPositionData.SelectActivityNamedMain();
		filter = new ViewPositionData.FilterDrawingData();
	}
	
	public ArrayList<String> retrieveViewInformation(){
		init();
		if(debug) System.out.println("Connecting Device");
		Device device = connectDevice();
		if(device == null){
			System.out.println("Cannot find device.");
			return null;
		}
		
		if(debug) System.out.println("Checking Device");
		if(checkViewServer(device)) {
			System.out.println("Checking window failure.");
			return null;
		}
		
		if(debug) System.out.println("Retrieving window info");
		Window win = retrieveWindow(device);
		if(win == null){
			System.out.println("Cannot find window.");
			return null;
		}
		
		if(debug)System.out.println("Request layout");
		invalidateLayout(device, win);
		
		if(debug) System.out.println("Retrieving view info");
		ArrayList<ViewNode> arrlist = retrieveViewData(device,win);
		
		if(debug) System.out.println("Filtering Data");
		ArrayList<String> result = processData(arrlist);
		return result;
	}
	
	public void setDataFilter(NodeDataFilter filter){
		this.filter = filter;
	}
	
	public static String selectRecordWithKey(ArrayList<String> info, int pos, String key ){
		for(String record: info){
			String[] parts = record.split(";");
			if(parts[pos].trim().equals(key)){
				return record;
			}
		}
		return null;
	}
	
	private void init(){
		DeviceBridge.initDebugBridge();
	}
	
	private Device connectDevice(){
		DeviceBridge.startListenForDevices(listener);
		Device selected = null;
		while(true){
			selected = deviceSelecter.select(devices);
			if(selected != null) break;
			if(terminating) break;
			try { Thread.sleep(2000);
			} catch (InterruptedException e) { }
		}
		DeviceBridge.stopListenForDevices(listener);
		return selected;
	}
	
	private boolean checkViewServer(Device device){
		DeviceBridge.startViewServer(device);
		
//		if(DeviceBridge.isViewServerRunning(device) == false){
//			DeviceBridge.startViewServer(device);
//		}
		return false;
	}
	
	private Window retrieveWindow(Device device){
		DeviceBridge.setupDeviceForward(device);
		if(debug_1) System.out.println("Loading window");
		Window[] wins = WindowsLoader.loadWindows(device);
		Window selected = windowSelecter.select(wins);
		return selected;
	}
	
	private void invalidateLayout(Device device, Window window){
		ViewManager.invalidate(device, window, "");
//		ViewManager.requestLayout(device, window, "");
	}
	
	private ArrayList<ViewNode> retrieveViewData(Device device, Window window){
		Socket socket = null;
        BufferedReader in = null;
        BufferedWriter out = null;
        
        String line;
        ArrayList<ViewNode> arrlist = new ArrayList<ViewNode>();
        try {
            socket = new Socket();
            socket.connect(new InetSocketAddress("127.0.0.1",
                    DeviceBridge.getDeviceLocalPort(device)));
            out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out.write("DUMP " + window.encode());
            out.newLine();
            out.flush();
            
            Stack<ViewNode> stack = new Stack<ViewNode>();
            boolean setRoot = true;
            ViewNode lastNode = null;
            int lastWhitespaceCount = Integer.MAX_VALUE;

            while ((line = in.readLine()) != null) {
                if ("DONE.".equalsIgnoreCase(line)) {
                    break;
                }
                
                int whitespaceCount = Utility.countFrontWhitespace(line);
                if (lastWhitespaceCount < whitespaceCount) {
                    stack.push(lastNode);
                } else if (!stack.isEmpty()) {
                    final int count = lastWhitespaceCount - whitespaceCount;
                    for (int i = 0; i < count; i++) {
                        stack.pop();
                    }
                }

                lastWhitespaceCount = whitespaceCount;
                line = line.trim();
                int index = line.indexOf(' ');
                
                lastNode = new ViewNode();
                lastNode.name = line.substring(0, index);

                line = line.substring(index + 1);
                Utility.loadProperties(lastNode, line);
                arrlist.add(lastNode);
                if (setRoot) {
                    setRoot = false;
                }
                
                if (!stack.isEmpty()) {
                    final ViewNode parent = stack.peek();
                    lastNode.parent = parent;
                    parent.children.add(lastNode);
                }
            }
        } catch (IOException ex) {
        	ex.printStackTrace();
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
                if (in != null) {
                    in.close();
                }
                socket.close();
            } catch (IOException ex) {
            	ex.printStackTrace();
            }
        }
        return arrlist;
	}
	
	private ArrayList<String> processData(ArrayList<ViewNode> list){
		return filter.process(list);
	}
	
	private AndroidDebugBridge.IDeviceChangeListener listener = 
			new AndroidDebugBridge.IDeviceChangeListener(){ 
				@Override
				public void deviceConnected(Device device) { 
					ViewPositionData.this.devices.add(device);
				}
				@Override
				public void deviceDisconnected(Device device) { 
					ViewPositionData.this.devices.remove(device);
				}
				@Override
				public void deviceChanged(Device device, int changeMask) {  } 
	};
	
	
	public static class UnfilteredData implements NodeDataFilter{

		@Override
		public ArrayList<String> process(ArrayList<ViewNode> list) {
			ArrayList<String> result = new ArrayList<String>();
			for(ViewNode node: list){
				result.add(node.namedProperties.toString());
			}
			
			return result;
		}
		
	}
	
	public static class FilterDrawingData implements NodeDataFilter{
		@Override
		public ArrayList<String> process(ArrayList<ViewNode> list) {
			ArrayList<String> result = new ArrayList<String>();
			for(ViewNode node: list){
				
				double x = Double.parseDouble(node.namedProperties.get("drawing:getX()").value) ,
						y = Double.parseDouble(node.namedProperties.get("drawing:getY()").value);
				
				ViewNode current = node.parent;
				while(current != null){
					x+=Double.parseDouble(current.namedProperties.get("drawing:getX()").value);
					y+=Double.parseDouble(current.namedProperties.get("drawing:getY()").value);
					current = current.parent;
				}
				
				result.add(node.name+";"+x +";"+y);
			}
			return result;
		}
	}
	
	public static class SelectFirstDevice implements SelectDevice{
		@Override
		public Device select(ArrayList<Device> devices) { 
			if(devices == null || devices.isEmpty()) return null;
			return devices.get(0);
		}
	}
	
	public static class SelectActivityNamedMain implements SelectWindow{
		@Override
		public Window select(Window[] wins) {
			if(wins == null || wins.length <=0){
				System.out.println("No input window");
				return null;
			}
			for(Window win : wins){
				if(win.toString().toLowerCase().contains("main")){
					return win;
				}
			}
			return null;
		}
		
	}
	
	public static class StringValueRetriever implements NodeDataFilter{
		private String[] keys;
		public StringValueRetriever(String... keys){
			this.keys = keys;
			for(int i=0;i<keys.length;i++){
				this.keys[i] = this.keys[i].trim();
			}
		}
		@Override
		public ArrayList<String> process(ArrayList<ViewNode> list) {
			ArrayList<String> arr = new ArrayList<String>();
			for(ViewNode node : list){
				StringBuilder sb = new StringBuilder();
				sb.append(node.name).append(";");
				for(String key:keys){
					sb.append(node.namedProperties.get(key)).append(";");
				}
				arr.add(sb.toString());
			}
			return arr;
		}
		
	}
	
	public static interface NodeDataFilter{
		public ArrayList<String> process(ArrayList<ViewNode> list);
	}
	
	public static interface SelectDevice{
		public Device select(ArrayList<Device> devices);
	}
	
	public static interface SelectWindow{
		public Window select(Window[] wins);
	}
}
