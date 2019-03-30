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
import com.huawei.util.MapUtil;

public class MapUpdate {
	
	private static final Logger logger = Logger.getLogger(MapUpdate.class);
	
	public static Set<Car> nowWaitedCars = new HashSet<>();
	
	private static int stepOneCount = 0;
	private static int stepTwoCount = 0;
	private static int stepTwoTimes = 0;
	private static int stepThreeCount = 0;
	public static int stepFinishCount = 0;
	
	public static void updateMap() {
//		logger.info("start updateMap in " + MapSimulator.term);
		if(MapSimulator.term==1000)
			logger.error("reach " + MapSimulator.term);
		nowWaitedCars.addAll(MapSimulator.nowRunCars);
		for (Car car : nowWaitedCars) {
			car.setWaited(true);
		}
		stepFinishCount = 0;

//		logger.info("start step one");
		stepOneCount = 0;
		Iterator<Map.Entry<Integer, Road>> iterator = MapUtil.roads.entrySet().iterator();
		while (iterator.hasNext()) {
			Map.Entry<Integer, Road> entry = iterator.next();
			stepOneCount += driveCarJustOnRoadToEndState(entry.getValue());
		}
//		logger.info("end step one: update " + stepOneCount + " cars");
		//logger.info("leave " + nowWaitedCars.size() + " cars waited");
		
//		logger.info("start step two");
		stepTwoCount = 0;
		stepTwoTimes = 0;
		while (nowWaitedCars.size() != 0) {
			
			int count = 0;
			for (int i = 0; i < MapUtil.crossSequence.size(); i++) {
				Cross cross = MapUtil.crosses.get(MapUtil.crossSequence.get(i));
				count += updateCross(cross);
			}
			if(count==0) {
				findDeadLock();
				System.exit(MapSimulator.term);
			}
			stepTwoCount += count;
			stepTwoTimes++;
		}
//		logger.info("end step two: update " + stepTwoCount + " cars in "
//				+ stepTwoTimes + " iterators");
		
//		int count = 0;
		for (CarFlow carFlow : MapSimulator.nowRunCarFlows) {
			ArrayList<Car> list = carFlow.getNowStartOffCars();
			MapSimulator.outRoadCars.addAll(list);
//			count += list.size();
		}
//		logger.info("add " + count + " cars to outRoadCars");
		
//		logger.info("start step three");
		stepThreeCount = 0;
		
		Collections.sort(MapSimulator.outRoadCars, new Comparator<Car>() {
			@Override
			public int compare(Car o1, Car o2) {
				// TODO Auto-generated method stub
				if(o2.getRealStartTime() == o1.getRealStartTime())
					return o1.getCarId() - o2.getCarId();
				return o1.getRealStartTime() - o2.getRealStartTime();
				
			}
		});
		for (int i = 0; i < MapSimulator.outRoadCars.size(); i++) {
			Car car = MapSimulator.outRoadCars.get(i);
			if (car.getRealStartTime() <= MapSimulator.term)
				if (car.startOff()) {
					stepThreeCount++;
					i--;
				}
		}
		if(MapSimulator.nowRunCars.size()!=0)
			if(MapSimulator.realStartTerm == -1)
				MapSimulator.realStartTerm = MapSimulator.term;
//		logger.info("end step three: start off " + stepThreeCount + " cars");
//		logger.info("finish " + stepFinishCount + " cars");
//		logger.info("end updateMap");
	}
	
	private static int driveCarJustOnRoadToEndState(Road road) {
		return road.updateRunnableCars();
	}
	
	private static int updateCross(Cross cross) {
		return cross.updateCross();
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
		logger.info("dead lock happen in " + MapSimulator.term);
	}
}
