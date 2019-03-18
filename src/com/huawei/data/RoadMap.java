package com.huawei.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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
	public static List<Car> outRoadCars = new LinkedList<>();
	public static Set<Car> nowRunCars = new HashSet<>();
	public static Set<Car> finishCars = new HashSet<>();

	public static Set<Car> nowWaitedCars = new HashSet<>();

	public static void runMapWithOutView() {
		initMap();
		while (finishCars.size() != cars.size()) {
			updateMap();
			nowTime++;
		}
	}

	public static void initMap() {
		logger.info("init map in " + nowTime);
		for (Car i : cars.values())
			outRoadCars.add(i);
		Collections.sort(outRoadCars, new Comparator<Car>() {
			@Override
			public int compare(Car o1, Car o2) {
				return o1.getCarId() - o2.getCarId();
			}
		});
	}

	public static void updateMap() {
		logger.info("start updateMap in " + nowTime);
		nowWaitedCars.addAll(nowRunCars);
		for (Car car : nowWaitedCars) {
			car.setUpdated(false);
		}
		// step one
		// only consider and update cars that will not pass the cross
		Iterator<Map.Entry<Integer, Road>> iterator = roads.entrySet().iterator();
		while (iterator.hasNext()) {
			Map.Entry<Integer, Road> entry = iterator.next();
			entry.getValue().updateRunnableCars();
		}
		// step two
		while (nowWaitedCars.size() != 0) {
			for (int i = 0; i < crossSequence.size(); i++) {
				Cross cross = crosses.get(crossSequence.get(i));
				cross.updateCross();
			}
		}
		// step three
		for (int i = 0; i < outRoadCars.size(); i++) {
			Car car = outRoadCars.get(i);
			if (car.getStartTime() <= nowTime)
				if (car.startOff())
					i--;
		}
	}

	public static void printMapSize() {
		System.out.println("map size: ");
		System.out.println(roads.size() + " roads");
		System.out.println(crosses.size() + " crosses");
		System.out.println(cars.size() + " cars");
	}

	public static boolean isDispatchFinished() {
		return finishCars.size() == cars.size();
	}

}
