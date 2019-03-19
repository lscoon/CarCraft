package com.huawei.service;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import com.huawei.entity.Car;
import com.huawei.entity.Cross;
import com.huawei.entity.Road;
import com.huawei.util.MapUtil;

public class MapSimulator {

	private static final Logger logger = Logger.getLogger(MapSimulator.class);

	public static int term = 0;
	public static List<Car> outRoadCars = new LinkedList<>();
	public static Set<Car> nowRunCars = new HashSet<>();
	public static Set<Car> finishCars = new HashSet<>();

	public static Set<Car> nowWaitedCars = new HashSet<>();

	public static void runMapWithOutView() {
		initMap();
		while (finishCars.size() != MapUtil.cars.size()) {
			updateMap();
			term++;
		}
	}

	public static void initMap() {
		logger.info("init map in " + term);
		for (Car i : MapUtil.cars.values())
			outRoadCars.add(i);
		Collections.sort(outRoadCars, new Comparator<Car>() {
			@Override
			public int compare(Car o1, Car o2) {
				return o1.getCarId() - o2.getCarId();
			}
		});
	}

	public static void updateMap() {
		logger.info("start updateMap in " + term);
		nowWaitedCars.addAll(nowRunCars);
		for (Car car : nowWaitedCars) {
			car.setWaited(true);
		}
		// step one
		// only consider and update cars that will not pass the cross
		Iterator<Map.Entry<Integer, Road>> iterator = MapUtil.roads.entrySet().iterator();
		while (iterator.hasNext()) {
			Map.Entry<Integer, Road> entry = iterator.next();
			entry.getValue().updateRunnableCars();
		}
		// step two
		while (nowWaitedCars.size() != 0) {
			for (int i = 0; i < MapUtil.crossSequence.size(); i++) {
				Cross cross = MapUtil.crosses.get(MapUtil.crossSequence.get(i));
				cross.updateCross();
			}
		}
		// step three
		for (int i = 0; i < outRoadCars.size(); i++) {
			Car car = outRoadCars.get(i);
			if (car.getStartTime() <= term)
				if (car.startOff())
					i--;
		}
	}

	public static boolean isDispatchFinished() {
		return finishCars.size() == MapUtil.cars.size();
	}

}
