package com.huawei.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.transform.Templates;

import com.huawei.entity.Car;
import com.huawei.entity.Cross;
import com.huawei.entity.Road;
import com.huawei.ui.MapFrame;

public class MapUtil {
	// id max = 312 -> CarIdMaxLength = 1000
	// will init while handle input data
	public static int CarIdMaxLength = 10;
	
	public static int CarMinSpeed = 10;
	public static int CarMaxSpeed = 0;
	
	public static int CarFlowNumTag = 1000;
	public static int DelayTerm = 0;
	public static int MaxFailCount = 20;
	public static int MaxCarFlowFinishCount = 10;
	
	public static final int IntMax = Integer.MAX_VALUE;
	public static final float FloatMax = Float.MAX_VALUE;
	
	public static MapFrame mapView = null;
	
	public static Map<Integer, Cross> crosses = new HashMap<>();
	public static Map<Integer, Road> roads = new HashMap<>();
	public static Map<Integer, Car> cars = new HashMap<>();

	// ordered sequence from min to max
	public static List<Integer> crossSequence = new ArrayList<>();
	
	public static void printMapSize() {
		System.out.println("map size: ");
		System.out.println(roads.size() + " roads");
		System.out.println(crosses.size() + " crosses");
		System.out.println(cars.size() + " cars");
	}
	
}
