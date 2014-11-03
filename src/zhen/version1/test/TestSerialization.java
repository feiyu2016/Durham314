package zhen.version1.test;

import java.util.HashMap;
import java.util.Map;

import zhen.version1.Support.CommandLine;
import zhen.version1.component.Event;
import zhen.version1.component.UIState;
import zhen.version1.framework.Common;
import zhen.version1.framework.Framework;

public class TestSerialization {
	static String[] targetApp = { "CalcA.apk", "KitteyKittey.apk",
			"net.mandaria.tippytipper.apk", "backupHelper.apk", "TheApp.apk", };
	static int appSelect = 0;
	static String appPath = "APK/" + targetApp[appSelect];

	// setup input parameters
	static Map<String, Object> att = new HashMap<String, Object>();
	static {
		att.put(Common.apkPath, appPath);
	}

	public static void main(String[] args) {
		part1();

		part2();
	}

	static void part2() {
		Framework frame1 = new Framework(att);
		frame1.rInfo.restoreData("file1");
		System.out.println("restore successfully");
		for (UIState ui : frame1.rInfo.getUIModel().getKnownVertices()) {
			System.out.println(ui);
		}

		UIState ui = frame1.rInfo.getUIModel().getKnownVertices().get(1);
		for (Event e : frame1.rInfo.getEventDeposit()) {
			if (e.getSource() == ui) {
				System.out.println("found");
				break;
			}
		}
	}

	static void part1() {

		Framework frame = new Framework(att);

		// once the step control is added, the program will wait for human
		// instruction
		// before entering next operation loop
		// addExplorerStepControl(frame);
		// add a call back method which is called after the finish of traversal
		// addOnTraverseFinishCallBack(frame);
		CommandLine.executeCommand("adb install -r " + appPath);
		// frame.enableStdinMonitor(true);
		// NOTE: right now it does require apk installed on the device manually
		// and please close the app if previous opened
		frame.setup();// initialize
		frame.start();// start experiment

		frame.rInfo.dumpeData("file1", true);
		System.out.println("dump successfully");
	}

}
