package com.huawei.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class RoadMap {
	
	public static Map<Integer, Cross> crosses = new HashMap<>();
	public static Map<Integer, Road> roads = new HashMap<>();
	public static Map<Integer, Car> cars = new HashMap<>();
	public static List<Integer> crossSequence = new ArrayList<>();
	
	private static int nowTime = 0;
	public static Set<Integer> nowRunCars = new LinkedHashSet<>();
	
	public static void updateMap() {
		
		//stepOne
	}
	
	public static void printMapSize() {
		System.out.println("map size: ");
		System.out.println(roads.size() + " roads");
		System.out.println(crosses.size() + " crosses");
		System.out.println(cars.size() + " cars");
	}
}
