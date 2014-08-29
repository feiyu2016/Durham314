package ZhensPackaget;

import java.util.List;

import inputGeneration.Layout;
import inputGeneration.StaticInfo;

/**
 * Use the info from static analysis to guide the search algorithm
 * Implementation of the workflow 
 * 
 * @author zhenxu
 *
 */

public class StaticGuidedAlgoirthm extends TraverseAlgorithm{	
	public StaticGuidedAlgoirthm(String apkPath) {
		super(apkPath);
	}

	@Override
	public void execute() {
		//pick an activity that has not been tested
		while(true){
			int actIndex = Utility.findFirstFalse(actMark);
			if(actIndex<0) break; // all activities are label as tested -- finished.
			
			//launch the act --> am start -n yourpackagename/.activityname
			String startingActName = this.activityNames.get(actIndex);
			ADBControl.sendSellCommand("am start -n "+this.packageName+"/."+startingActName);
			
			String currentActName = startingActName;
			while(true){
				//retrieve current layout information
				List<String> currentLayoutInfo = this.viewData.retrieveFocusedActivityInformation();
				
				//match with existing static information
				Layout matchedLayoutInfo = matchWithLayout(currentLayoutInfo);
				if(matchedLayoutInfo != null){ // there is a match!
					// where associated view ids are stored
					List<String> stayingWidgtsInfo = StaticInfo.getStayingWidgets(apkFile, currentActName, matchedLayoutInfo.getName());
					
					
				}else{
					
				}
				
			}
			
		}
	}

	//
	private Layout matchWithLayout(List<String> currentLayoutInfo){
		
		
		return null;
	}
}
