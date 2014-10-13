package zhen.version1.component;

import java.util.ArrayList;

import zhen.version1.Support.Utility;
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
	public static boolean DEBUG = true;
	public static String TAG = "RunTimeLayoutInformation";
	
	private IDevice mDevice;
	private ArrayList<IDevice> deviceList = new ArrayList<IDevice>();
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
    
    public void invalidView(ViewNode node){
    	DeviceBridge.invalidateView(node);
    }
    
    public void requestLayout(ViewNode node){
    	DeviceBridge.requestLayout(node);
    }
    
    public IHvDevice getIHvDevice(){
    	return this.device;
    }

    public IDevice getPrimaryDevice(){
    	return this.mDevice;
    }
    
    public void terminate(){
    	device.removeWindowChangeListener(windowListener);
    	device.terminateViewDebug();
    	DeviceBridge.stopListenForDevices(listener1);
    	DeviceBridge.removeDeviceForward(mDevice);
    	DeviceBridge.removeViewServerInfo(mDevice);
    	DeviceBridge.terminate();
    }
    
	public ArrayList<IDevice> getDeviceList() {
		return deviceList;
	}

	private AndroidDebugBridge.IDeviceChangeListener listener1 = new AndroidDebugBridge.IDeviceChangeListener(){
		@Override
		public void deviceChanged(IDevice arg0, int arg1) { }
		@Override
		public void deviceConnected(IDevice arg0) { 
			if(mDevice == null){
				if(DEBUG) Utility.log(TAG, arg0.getSerialNumber());
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
				deviceList.add(0,mDevice);
			}else{
				deviceList.add(arg0);
			}
		}
		@Override
		public void deviceDisconnected(IDevice arg0) { 
			if(mDevice == arg0 || mDevice.isOffline()){
				mDevice = null; 
				deviceList.remove(0);
			}else{
				deviceList.remove(arg0);//not sure if this is right
			}
		}
	};
	private WindowUpdater.IWindowChangeListener windowListener = new WindowUpdater.IWindowChangeListener(){
		@Override
		public void focusChanged(IDevice arg0) {
			if(mDevice == arg0 || mDevice.equals(arg0))
			focusedWindowHash = DeviceBridge.getFocusedWindow(arg0);
//			System.out.println("focusChanged:"+focusedWindowHash);
		}
		@Override
		public void windowsChanged(IDevice arg0) {
			if(mDevice == arg0 || mDevice.equals(arg0)){
				windowList = DeviceBridge.loadWindows(device, arg0);
			}
		}
	};
}
