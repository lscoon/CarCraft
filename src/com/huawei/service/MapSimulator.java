package com.huawei.service;

import java.util.ArrayList;
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
import com.huawei.ui.MapFrame;
import com.huawei.util.MapUtil;

public class MapSimulator {

	private static final Logger logger = Logger.getLogger(MapSimulator.class);

	public static int term = 0;
	public static List<Car> outRoadCars = new LinkedList<>();
	public static Set<Car> nowRunCars = new HashSet<>();
	public static Set<Car> finishCars = new HashSet<>();

	public static Set<Car> nowWaitedCars = new HashSet<>();
	
	private static int stepOneCount = 0;
	private static int stepTwoCount = 0;
	private static int stepTwoTimes = 0;
	private static int stepThreeCount = 0;
	public static int stepFinishCount = 0;
	
	public static void runMapWithView() {
		initMap();
		MapUtil.mapView = new MapFrame();
	}
	
	public static void runMapWithOutView() {
		initMap();
		while (finishCars.size() != MapUtil.cars.size()) {
			updateMap();
			term++;
		}
	}

	public static void runMapWithCarList(ArrayList<Car> carList) {
		logger.info("init map in " + term);
		for (Car car : carList)
			if(car != null)
				outRoadCars.add(car);
		Collections.sort(outRoadCars, new Comparator<Car>() {
			@Override
			public int compare(Car o1, Car o2) {
				return o1.getCarId() - o2.getCarId();
			}
		});
		while (finishCars.size() != carList.size()) {
			updateMap();
			term++;
		}
		finishCars.clear();
		logger.info("end map in " + term);
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
		stepFinishCount = 0;

		logger.info("start step one");
		stepOneCount = 0;
		Iterator<Map.Entry<Integer, Road>> iterator = MapUtil.roads.entrySet().iterator();
		while (iterator.hasNext()) {
			Map.Entry<Integer, Road> entry = iterator.next();
			stepOneCount += entry.getValue().updateRunnableCars();
		}
		logger.info("end step one: update " + stepOneCount + " cars");
		//logger.info("leave " + nowWaitedCars.size() + " cars waited");
		
		logger.info("start step two");
		stepTwoCount = 0;
		stepTwoTimes = 0;
		while (nowWaitedCars.size() != 0) {
			int count = 0;
			for (int i = 0; i < MapUtil.crossSequence.size(); i++) {
				Cross cross = MapUtil.crosses.get(MapUtil.crossSequence.get(i));
				count += cross.updateCross();
			}
			if(count==0) {
				findDeadLock();
				System.exit(term);
			}
			stepTwoCount += count;
			stepTwoTimes++;
		}
		logger.info("end step two: update " + stepTwoCount + " cars in "
				+ stepTwoTimes + " iterators");
		
		logger.info("start step three");
		stepThreeCount = 0;
		for (int i = 0; i < outRoadCars.size(); i++) {
			Car car = outRoadCars.get(i);
			if (car.getRealStartTime() <= term)
				if (car.startOff()) {
					stepThreeCount++;
					i--;
				}
		}
		logger.info("end step three: startoff " + stepThreeCount + " cars");
		logger.info("finish " + stepFinishCount + " cars");
		logger.info("end updateMap\n");
	}
	
	private static void findDeadLock() {
		if(MapUtil.mapView!=null) {
			String temp = "dead lock " + nowWaitedCars.size() + " cars\n";
			List<Car> tempCarSet = new LinkedList<>();
			for(Car car : nowWaitedCars)
				tempCarSet.add(car);
			Collections.sort(tempCarSet, new Comparator<Car>() {
				@Override
				public int compare(Car o1, Car o2) {
					return o1.getCarId() - o2.getCarId();
				}
			});
			
			for(Car car : tempCarSet)
				temp = temp.concat(car.getCarId()+"\n");
			MapUtil.mapView.pControl.info.setText(temp);
		}
		logger.info("dead lock happen");
	}

	public static boolean isDispatchFinished() {
		return finishCars.size() == MapUtil.cars.size();
	}

}
