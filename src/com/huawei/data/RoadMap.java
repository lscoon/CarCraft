package com.huawei.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.log4j.Logger;

public class RoadMap {
	
	private static final Logger logger = Logger.getLogger(RoadMap.class);
	
	public static Map<Integer, Cross> crosses = new HashMap<>();
	public static Map<Integer, Road> roads = new HashMap<>();
	public static Map<Integer, Car> cars = new HashMap<>();
	
	// ordered sequence from min to max
	public static List<Integer> crossSequence = new ArrayList<>();
	
	public static int nowTime = 0;
	public static List<Integer> outRoadCars = new LinkedList<>();
	public static Set<Integer> nowRunCars = new HashSet<>();
	public static Set<Integer> nowWaitedCars = new HashSet<>();
	public static Set<Integer> finishCars = new HashSet<>();
	
	public static void runMap() {
		logger.info("init map in " + nowTime);
		initMap();
		while(finishCars.size()!=cars.size()) {
			updateMap();
			nowTime++;
		}
	}
	
	public static void initMap() {
		for(Integer i : cars.keySet())
			outRoadCars.add(i);
		Collections.sort(outRoadCars);
	}
	
	public static void updateMap() {
		logger.info("start updateMap in " + nowTime);
		nowWaitedCars.addAll(nowRunCars);
		// step one
		// only consider and update cars that will not pass the cross
		Iterator<Map.Entry<Integer, Road>> iterator = roads.entrySet().iterator();
		while (iterator.hasNext()) {
		    Map.Entry<Integer, Road> entry = iterator.next();
		    entry.getValue().updateRunnableCars();
		}
		// step two
		while(nowWaitedCars.size()!=0) {
			for(int i=0; i<crossSequence.size(); i++) {
				Cross cross = crosses.get(crossSequence.get(i));
				cross.updateCross();
			}
		}
		// step three
		for(int carId : outRoadCars) {
			Car car = cars.get((Integer)carId);
			if(car.getStartTime()<=nowTime)
				car.startOff();
		}
	}
	
	public static void printMapSize() {
		System.out.println("map size: ");
		System.out.println(roads.size() + " roads");
		System.out.println(crosses.size() + " crosses");
		System.out.println(cars.size() + " cars");
	}
}
