package zhen.implementation;

import zhen.framework.AbstractRunTimeInformation;
import zhen.framework.Configuration;
import zhen.framework.Framework;
import zhen.packet.Utility;

import com.android.ddmlib.AndroidDebugBridge;
import com.android.ddmlib.Device;
import com.android.ddmlib.IDevice;
import com.android.hierarchyviewerlib.device.DeviceBridge;
import com.android.hierarchyviewerlib.device.HvDeviceFactory;
import com.android.hierarchyviewerlib.device.IHvDevice;
import com.android.hierarchyviewerlib.device.WindowUpdater;
import com.android.hierarchyviewerlib.device.DeviceBridge.ViewServerInfo;
import com.android.hierarchyviewerlib.models.ViewNode;
import com.android.hierarchyviewerlib.models.ViewNode.Property;
import com.android.hierarchyviewerlib.models.Window;

public class HierarchyLayoutInformation extends AbstractRunTimeInformation{
	private static String TAG = "HierarchyLayoutInformation";
	private IDevice mDevice;
	private IHvDevice device;
	public static boolean DEBUG = true;
	private Window[] windowList;
	private int focusedWindowHash;
	private ViewServerInfo mViewServerInfo;
	
	private ViewNode currentActLayout;
	private boolean inputMethodVisbility = false;
	
	public HierarchyLayoutInformation(Framework frame) {
		super(frame);
	}

	@Override
	public boolean init(){
		DeviceBridge.initDebugBridge(Configuration.adbPath);
		DeviceBridge.startListenForDevices(listener1);
		DeviceBridge.startListenForDevices(listener2);
		while(mDevice == null){
			try { Thread.sleep(200); } catch (InterruptedException e) { }
		}//wait until a device is found
		DeviceBridge.stopListenForDevices(listener1);
		try { Thread.sleep(50); } catch (InterruptedException e) { }
		
        if (!mDevice.isOnline()) {return false; }
        DeviceBridge.setupDeviceForward(mDevice);
        if (!DeviceBridge.isViewServerRunning(mDevice)) {
            if (!DeviceBridge.startViewServer(mDevice)) {
            	if(DEBUG) Utility.log(TAG,"cannot start viewserver");
                DeviceBridge.removeDeviceForward(mDevice);
                return false;
            }
        }
        mViewServerInfo = DeviceBridge.loadViewServerInfo(mDevice);
        if (mViewServerInfo == null) { return false; }
        WindowUpdater.startListenForWindowChanges(windowListener, mDevice);
        Utility.sleep(100);
        windowList = DeviceBridge.loadWindows(null, mDevice);
		return true;
	}
	
	@Override
	public void observe() {
		if(windowList == null) return;
		Window focused = getFocusedWindow();
		currentActLayout = DeviceBridge.loadWindowData(focused);
		//find input method
		Window inputmethod = null;
		for(Window win: windowList){
			if(win.getTitle().toLowerCase().contains("inputmethod")){
				inputmethod = win; 
				ViewNode node = DeviceBridge.loadWindowData(inputmethod);
				String result = node.namedProperties.get("getVisibility()").value;
				String[] parts = result.split("=");
				if(parts.length >1 ){ inputMethodVisbility = parts[1].startsWith("VISIBLE");
				}else{ inputMethodVisbility = false; }
				break;
			}
		}
	}
	
	@Override
	public ViewNode getFocusedLayout() { 
		return currentActLayout;
	}
	@Override
	public boolean isKeyBoardVisible(){
		return inputMethodVisbility;
	}
	
	@Override
	public ViewNode loadWindowData(Window window) {
		return window==null?null:DeviceBridge.loadWindowData(window);
	}

	@Override
	public Window getFocusedWindow() {
    	if(windowList == null) return null;
    	for(Window win: windowList){
    		if(win.getHashCode() == focusedWindowHash){
    			return win;
    		}
    	}
        return null;
    }
	
	@Override
	public Window[] getWindowList(){
		return windowList==null?new Window[0]:windowList;
	}
	
	@Override
	public double processCPUUsage() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double memoryUsage() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getBreakPointCount() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void onLaunchApplication() {
		// TODO
	}

	@Override
	public void onLaunchApplicationFinish() {
		// TODO
	}

	@Override
	public void onQuitApplication() {
		// TODO
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
    	WindowUpdater.stopListenForWindowChanges(windowListener, mDevice);
    	WindowUpdater.terminate();
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
		}
		@Override
		public void windowsChanged(IDevice arg0) {
			windowList = DeviceBridge.loadWindows(device, arg0);
		}
	};


}
