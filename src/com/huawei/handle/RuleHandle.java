package com.huawei.handle;

import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;

import com.huawei.data.Car;
import com.huawei.data.Road;
import com.huawei.data.RoadMap;

public class RuleHandle {
	
	private static final Logger logger = Logger.getLogger(RuleHandle.class);
	
	// first step: update all cars
	// 	 1.could run without crosses
	// 	 2.won't fall into jammed status
	public boolean carRunOnRoad(int carId, Road road) {
		Car car = RoadMap.cars.get(carId);
		if(car.isUpdated()) {
			logger.error("Step1: car " + carId + " has been updated before");
			return true;
		}
		if(car.getNowSpeed()==0) {
			logger.info("Step1: car " + carId + " speed = 0");
			return false;
		}
		List<Integer> lane=road.getCarLane(carId);
		if(lane==null) {
			logger.error("Step1: car " + carId + " is not in road " + road.getRoadId());
			return false;
		} else {
			if(lane.size()<=1) {
				logger.error("Step1: car " + carId + " has been in cross, should be checked in nowSpeed");
				return false;
			}
			int distance = car.getNowSpeed()-1;
			for(int i=1; i<lane.size(); i++,distance--) {
				int carI = lane.get(i);
				if(carI!=0) {
					if(RoadMap.cars.get(carI).isUpdated()) {
						logger.info("Step1: car " + carId + " success: distance " + (i-1));
						// move forward and change speed
						return true;
					} else {
						logger.info("Step1: car " + carId + " will wait car ahead, pass to Step2");
						//move forward and change speed
						return false;
					}
				}
				if(distance==0) {
					logger.info("Step1: car " + carId + " success: max distance " + (i-1));
					//move foward
					return true;
				}
			}
			logger.info("Step1: car " + carId + " will cross crosses, pass to Step2");
			//move forward and change speed
			return false;
		}
	}
}
