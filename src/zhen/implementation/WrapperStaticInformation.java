package zhen.implementation;

import inputGeneration.StaticInfo;

import java.io.File;
import java.util.Map;

import zhen.framework.AbstractStaticInformation;
import zhen.framework.Framework;

public class WrapperStaticInformation extends AbstractStaticInformation {

	public WrapperStaticInformation(Framework frame) {
		super(frame); 
	}

	@Override
	public boolean init(Map<String,Object> attributes) {
		File inputFile = (File) attributes.get("apkfile");
		StaticInfo.initAnalysis(inputFile, false);
		
		
		
		
		return true;
	}

	@Override
	public void terminate() {
		// TODO Auto-generated method stub
	}

}
