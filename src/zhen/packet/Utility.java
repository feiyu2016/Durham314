package zhen.packet;

import java.io.File;
import java.io.IOException;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Utility {
	public static int findFirstFalse(boolean[] arr){
		for(int i=0;i<arr.length;i++){
			if(arr[i] == false) return i;
		}
		return -1;
	}
	
	public static Logger setupLogger(Class clazz){
		Logger logger = Logger.getLogger(clazz.getName());
		FileHandler fhandler;
		ConsoleHandler chandler;
		try {
			for(Handler handle : logger.getHandlers()){
				handle.setLevel(Level.FINER);
	        }
			
//			logger.removeHandler(handle);
//			Handler[] hlist = ;
//	        for(Handler handle : hlist){
//	        	logger.removeHandler(handle);
//	        }
//	        logger.setUseParentHandlers(false);
////			fhandler = new FileHandler(Common.LogFolder+clazz.getName()+".html",1024*1024,10,true);
//			chandler = new ConsoleHandler();
////			fhandler.setLevel(Level.ALL);
//			chandler.setLevel(Level.ALL);
//			
////	        logger.addHandler(fhandler);
//	        logger.addHandler(chandler);

		} catch (Exception e) {
			e.printStackTrace();
		}
		return logger;
	}
	
	public static void removeFileUnderFolder(String folderName){
		File folder = new File(folderName);
		String[] names = folder.list();
		for(String name:names){
			File afile = new File(folderName+"/"+name);
			afile.delete();
		}
	}
	
	public static void log(String msg){
		System.out.println(msg);
	}
	
	public static void info(String msg){
		System.out.println(msg);
	}
	
	public static void log(String tag, String msg){
		System.out.println(tag+";"+msg);
	}
}
