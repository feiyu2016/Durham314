package zhen.version1.component;

import java.util.Arrays;
 


import zhen.version1.framework.Configuration;

import com.android.ddmlib.AndroidDebugBridge;
import com.android.ddmlib.IDevice;
import com.android.hierarchyviewerlib.device.DeviceBridge;
import com.android.hierarchyviewerlib.device.HvDeviceFactory;
import com.android.hierarchyviewerlib.device.IHvDevice;
import com.android.hierarchyviewerlib.device.ViewServerDevice;
import com.android.hierarchyviewerlib.device.WindowUpdater;
import com.android.hierarchyviewerlib.models.ViewNode;
import com.android.hierarchyviewerlib.models.Window;

//might want to extend AbstractHvDevice
public class RunTimeLayoutInformation {
	private IDevice mDevice;
	private IHvDevice device;
//	private String path;
	
	private Window[] windowList;
	private int focusedWindowHash;
//	private String systemAct = "SearchPanelNavigationBarStatusBarKeyguardKeyguardScrimInputMethodcom.android.systemui.ImageWallpaper";
	
	public void init(){
		DeviceBridge.initDebugBridge(Configuration.ADBPath);
		DeviceBridge.startListenForDevices(listener1);
//		DeviceBridge.startListenForDevices(listener2);
		while(mDevice == null){
			try { Thread.sleep(200); } catch (InterruptedException e) { }
		}//wait until a device is found
//		DeviceBridge.stopListenForDevices(listener1);
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
    
    
//    public Window getTopWindow(){
//    	if(windowList == null) return null;
//    	int index = 0;
//    	for(Window win: windowList){
//    		if(win.getHashCode() == focusedWindowHash){
//    			break;
//    		}
//    		index+=1;
//    	}
//    	
//    	//is it possible for a popup or dialog to have the same name as a system act ?
//    	//try to find the actual "top" activity
//    	//1) the window list is actually a drawing priority list, 
//    	//2) some basic system app like input method always on the top
//    	//and 3) the app launched recently will be on the side by side. 
//    	
//    	//some popup could be an act in the list but not focused!
//    	int pointerIndex = index;
//    	while(pointerIndex > 0){
//    		String previousActTitle = this.windowList[pointerIndex-1].getTitle();
//    		if(systemAct.contains(previousActTitle)){ //it is a system app. 
//    			break;
//    		}
//    		pointerIndex -= 1;
//    	}
//    	return windowList[pointerIndex];
//    }
    
//    public ViewNode loadTopWindowData(){
//    	return loadWindowData(getTopWindow());
//    }
    
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
    	DeviceBridge.stopListenForDevices(listener1);
    	DeviceBridge.removeDeviceForward(mDevice);
    	DeviceBridge.removeViewServerInfo(mDevice);
    	DeviceBridge.terminate();
    }
	
	private AndroidDebugBridge.IDeviceChangeListener listener1 = new AndroidDebugBridge.IDeviceChangeListener(){
		@Override
		public void deviceChanged(IDevice arg0, int arg1) { }
		@Override
		public void deviceConnected(IDevice arg0) { 
			if(mDevice == null){
				mDevice = arg0; 
				//as a compromise
				final ViewServerDevice vd = new ViewServerDevice(mDevice);
				new Thread(new Runnable(){
					@Override
					public void run() { 
						try { Thread.sleep(1000);
						} catch (InterruptedException e) { 
							e.printStackTrace();
						}
						windowList = DeviceBridge.loadWindows(vd, mDevice);
						focusedWindowHash = DeviceBridge.getFocusedWindow(mDevice);
					}
				}).start();
			}
		}
		@Override
		public void deviceDisconnected(IDevice arg0) { 
			if(mDevice == arg0 || mDevice.isOffline()){
				mDevice = null; 
			}
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
			for(Window win : windowList){
//				System.out.println(win.getHashCode()+"  :  "+win.encode());
			}
		}
	};
}
