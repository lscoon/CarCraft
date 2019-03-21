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
import com.huawei.entity.CarFlow;
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
	
	public static List<CarFlow> nowRunCarFlows = new LinkedList<>();
	
	private static int stepOneCount = 0;
	private static int stepTwoCount = 0;
	private static int stepTwoTimes = 0;
	private static int stepThreeCount = 0;
	public static int stepFinishCount = 0;
	
	public static void runMapWithView() {
		logger.info("start run map in " + term + "\n");
		initMap();
		MapUtil.mapView = new MapFrame();
	}
	
	public static void runMapWithOutView() {
		logger.info("start run map in " + term + "\n");
		initMap();
		while (finishCars.size() != MapUtil.cars.size()) {
			updateMap();
			term++;
		}
		logger.info("end run map in " + term + "\n");
	}

	public static void runMapWithCarList(ArrayList<Car> carList) {
		logger.info("start run map with car list in " + term + "\n");
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
		logger.info("end run map with car list in " + term + "\n");
	}
	
	public static void runMapWithCarFlow() {
		logger.info("start run map with car flow in " + term + "\n");
		
		addRunCarFlows();
		while (finishCars.size() != MapUtil.cars.size()) {
			updateMap();
			
			for(Car car : outRoadCars)
				car.carFlow.putback(car);
			outRoadCars.clear();
			
			int count=0;
			for(int i=0; i<nowRunCarFlows.size(); i++) {
				CarFlow carflow = nowRunCarFlows.get(i);
				if(carflow.checkIfFinished()) {
					nowRunCarFlows.remove(i);
					i--;
					count++;
				}
			}
			if(count > 0)
				addRunCarFlows();
			term++;
		}
		logger.info("end run map with car set in " + term + "\n");
	}
	
	private static void addRunCarFlows() {
		for(CarFlow carflow : GlobalSolver.carFlows)
			if(!carflow.isFinished() && !carflow.isRunning())
				if(carflow.startoff())
					nowRunCarFlows.add(carflow);
	}
	
	public static void initMap() {
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
		
		for (CarFlow carFlow : nowRunCarFlows)
			outRoadCars.addAll(carFlow.getNowStartOffCars());
		
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
