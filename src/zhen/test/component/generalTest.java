package zhen.test.component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import zhen.version1.component.Event;
import zhen.version1.framework.Common;

public class generalTest {
	public static void main(String[] args){

		Map<String, Object> extraInformation = new HashMap<String,Object>();
		
		extraInformation.put(Common.event_att_packname, "asd");
		List<Event> list = new ArrayList<Event>();
		extraInformation.put(Common.ui_extra_event, list);
		
		System.out.println(extraInformation.get(Common.ui_extra_event));
	}
}
