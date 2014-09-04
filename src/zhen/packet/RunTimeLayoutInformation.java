package zhen.packet;

import java.util.Arrays;

import com.android.ddmlib.AndroidDebugBridge;
import com.android.ddmlib.Device;
import com.android.ddmlib.IDevice;
import com.android.hierarchyviewerlib.device.DeviceBridge;
import com.android.hierarchyviewerlib.device.HvDeviceFactory;
import com.android.hierarchyviewerlib.device.IHvDevice;
import com.android.hierarchyviewerlib.device.WindowUpdater;
import com.android.hierarchyviewerlib.models.ViewNode;
import com.android.hierarchyviewerlib.models.Window;

public class RunTimeLayoutInformation {
	private IDevice mDevice;
	private IHvDevice device;
	private String path;
	
	private Window[] windowList;
	private int focusedWindowHash;
	
	public RunTimeLayoutInformation(String adbPosition){
		path = adbPosition;
	}
	
	public void init(){
		DeviceBridge.initDebugBridge(path);
		DeviceBridge.startListenForDevices(listener1);
		DeviceBridge.startListenForDevices(listener2);
		while(mDevice == null){
			try { Thread.sleep(200); } catch (InterruptedException e) { }
		}//wait until a device is found
		DeviceBridge.stopListenForDevices(listener1);
		try { Thread.sleep(50); } catch (InterruptedException e) { }
		device = HvDeviceFactory.create(mDevice);
		device.initializeViewDebug();
		try { Thread.sleep(50); } catch (InterruptedException e) { }
		device.addWindowChangeListener(windowListener);
	}
	
	public Window[] getWindowList(){
		return windowList==null?new Window[0]:windowList;
	}
	
    public Window getFocusedWindow() {
    	if(windowList == null) return null;
    	for(Window win: windowList){
    		if(win.getHashCode() == focusedWindowHash){
    			return win;
    		}
    	}
        return null;
    }
    
    public ViewNode loadWindowData(Window window) {
        return window==null?null:DeviceBridge.loadWindowData(window);
    }
    
    public ViewNode loadFocusedWindowData(){
    	return loadWindowData(Window.getFocusedWindow(device));
    }
    
    public void invalidView(ViewNode node){
    	DeviceBridge.invalidateView(node);
    }
    
    public void requestLayout(ViewNode node){
    	DeviceBridge.requestLayout(node);
    }
    
    public IHvDevice getIHvDevice(){
    	return this.device;
    }

    
    public void terminate(){
    	device.removeWindowChangeListener(windowListener);
    	device.terminateViewDebug();
    	DeviceBridge.stopListenForDevices(listener2);
    	DeviceBridge.removeDeviceForward(mDevice);
    	DeviceBridge.removeViewServerInfo(mDevice);
    	DeviceBridge.terminate();
    }
	
	private AndroidDebugBridge.IDeviceChangeListener listener1 = new AndroidDebugBridge.IDeviceChangeListener(){
		@Override public void deviceChanged(Device arg0, int arg1) { }
		@Override public void deviceConnected(Device arg0) { mDevice = arg0; }
		@Override public void deviceDisconnected(Device arg0) { mDevice = null; }
	};
	private AndroidDebugBridge.IDeviceChangeListener listener2 = new AndroidDebugBridge.IDeviceChangeListener(){
		@Override
		public void deviceChanged(Device arg0, int arg1) {
			System.out.println("deviceChanged:"+arg0+";"+arg1);
		}
		@Override
		public void deviceConnected(Device arg0) {
			System.out.println("deviceConnected:"+arg0+";");
		}
		@Override
		public void deviceDisconnected(Device arg0) {
			System.out.println("deviceDisconnected:"+arg0+";");
		}
	};
	private WindowUpdater.IWindowChangeListener windowListener = new WindowUpdater.IWindowChangeListener(){
		@Override
		public void focusChanged(IDevice arg0) {
			focusedWindowHash = DeviceBridge.getFocusedWindow(arg0);
//			System.out.println("focusChanged:"+focusedWindowHash);
		}
		@Override
		public void windowsChanged(IDevice arg0) {
			windowList = DeviceBridge.loadWindows(device, arg0);
//			System.out.println("windowsChanged:"+Arrays.toString(windowList));
		}
	};
}
