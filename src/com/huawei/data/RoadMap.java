package com.huawei.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class RoadMap {
	
	public static Map<Integer, Road> roads = new LinkedHashMap<Integer, Road>();
	public static Map<Integer, Cross> crosses = new LinkedHashMap<Integer, Cross>();
	public static Map<Integer, Car> cars = new LinkedHashMap<Integer, Car>();
	public static List<Integer> crossSequence = new ArrayList<Integer>();
	
	public static void generateCrossSequence() {
		for(int crossId : crosses.keySet()) {
			crossSequence.add(crossId);
		}
		Collections.sort(crossSequence);
	}
	
	public static void printMapSize() {
		System.out.println("map size: ");
		System.out.println(roads.size() + " roads");
		System.out.println(crosses.size() + " crosses");
		System.out.println(cars.size() + " cars");
	}
}
