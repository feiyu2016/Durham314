package zhen.packet;

public class Utility {
	public static int findFirstFalse(boolean[] arr){
		for(int i=0;i<arr.length;i++){
			if(arr[i] == false) return i;
		}
		return -1;
	}
	
	public boolean toFile = false;
	
	public void Log(String msg){

	}
}
