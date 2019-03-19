package com.huawei.service;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.huawei.entity.Car;
import com.huawei.entity.Road;
import com.huawei.util.FloydUtil;
import com.huawei.util.MapUtil;

public class GlobalSolver {
	private static final Logger logger = Logger.getLogger(GlobalSolver.class);

	public GlobalSolver() {
		// To Do
	}

	public static void initSolver() {
		MapSimulator.initMap();
		initCarRoadList();
	}

	public static void invokeSolver() {
		initSolver();

		while (!MapSimulator.isDispatchFinished()) {
			MapSimulator.updateMap();
			MapSimulator.term++;
			updateCarRoadList();
		}
	}

	
	
	
	
	
	
	
	// shortest path road sequence
	public static void initCarRoadList() {
		FloydUtil.initPathAndDistMatrixMap();
		
		for(Car car : MapUtil.cars.values()) {
			int originSeq = MapUtil.crossSequence.indexOf(car.getOrigin());
			int destinationSeq = MapUtil.crossSequence.indexOf(car.getDestination());
			
			Road[][] pathMatrix = FloydUtil.pathMap.get((Integer)car.getMaxSpeed());
			List<Road> roadList = new ArrayList<>();
			
			while(originSeq != destinationSeq) {
				Road road = pathMatrix[originSeq][destinationSeq];
				roadList.add(road);
				// get another road cross sequence
				originSeq = MapUtil.crossSequence.indexOf(
						road.getAnOtherCross(MapUtil.crossSequence.get(originSeq)));
			}
			car.setRoadList(roadList);
			car.setNextRoad(roadList.get(0));
		}
	}

	public static void updateCarRoadList() {
		// To Do
	}
}
