package zhen.test;

import inputGeneration.RunTimeInfo;

import java.io.File;
import java.util.Scanner;

public class runlogcat {

	public static void main(String[] args) throws Exception {
		String path = "APK/testApp1.apk";
		File f = new File(path);
		Scanner sc = new Scanner(System.in);
		while (true) {
			String reading = sc.nextLine();
			if (reading.equals("0"))
				break;

			if (reading.equals("1")) {
				RunTimeInfo.readLogcat(f);

			} else if (reading.equals("2")) {
				RunTimeInfo.clearLogcat();
			}

		}
		sc.close();
	}

}
