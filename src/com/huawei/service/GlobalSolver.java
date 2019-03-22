package com.huawei.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import org.apache.log4j.Logger;

import com.huawei.entity.Car;
import com.huawei.entity.CarFlow;
import com.huawei.entity.Road;
import com.huawei.util.FloydUtil;
import com.huawei.util.MapUtil;

public class GlobalSolver {
	private static final Logger logger = Logger.getLogger(GlobalSolver.class);

//	public static ArrayList<CarFlow> carFlows = new ArrayList<CarFlow>();
//	public static ArrayList<List<Road>> conflictFreePathSets = new ArrayList<List<Road>>();

	private static LinkedList<ArrayList<Car>> startTimeSortedCars = null;

	public static void initSolver() {
//		MapSimulator.initMap();
//		initSortedRoads();
		// initialize with shortest path
		initCarRoadList();
		initCarClusters();
		logger.info("Solver init finished!");
	}

	/**
	 * @version: v1.0
	 * @modified: 2019年3月19日 上午11:10:36
	 * @description: the only entrance of GlobalSolver class
	 * @return: a feasible solution directly written to answer.txt
	 */
	public static void invokeSolver() {
		initSolver();
		/*
		 * while (!MapSimulator.isDispatchFinished()) { MapSimulator.updateMap();
		 * MapSimulator.term++; updateCarRoadList(); }
		 */

//		for(ArrayList<Car> carList : conflictFreeCarClusters) {
//			if(carList == null)
//				continue;
//			MapSimulator.runMapWithCarList(carList);
//		}
	}

	// calculate shortest path road sequence
	public static void initCarRoadList() {
		FloydUtil.initPathAndDistMatrixMap();

		for (Car car : MapUtil.cars.values()) {
			int originSeq = MapUtil.crossSequence.indexOf(car.getOrigin());
			int destinationSeq = MapUtil.crossSequence.indexOf(car.getDestination());

			Road[][] pathMatrix = FloydUtil.pathMap.get((Integer) car.getMaxSpeed());
			List<Road> roadList = new ArrayList<>();

			while (originSeq != destinationSeq) {
				Road road = pathMatrix[originSeq][destinationSeq];
				roadList.add(road);
				// get another road cross sequence
				originSeq = MapUtil.crossSequence.indexOf(road.getAnOtherCross(MapUtil.crossSequence.get(originSeq)));
			}
			car.setRoadList(roadList);
			car.setNextRoad(roadList.get(0));
		}
	}

	public static void updateCarRoadList() {
		// To Do
	}

	/**
	 * @version: v1.0
	 * @modified: 2019年3月19日 上午11:45:46
	 * @description: sort roads by startTime and speed
	 * @return: void
	 */
	public static void initSortedRoads() {
		// first sort road by startTime only
		ArrayList<Car> sortedCars = new ArrayList<Car>();
		for (Car car : MapUtil.cars.values()) {
			sortedCars.add(car);
		}
		Collections.sort(sortedCars, new Comparator<Car>() {
			@Override
			public int compare(Car o1, Car o2) {
				if (o1.getStartTime() == o2.getStartTime()) {
					// ascending sort
					// return o1.getMaxSpeed() - o2.getMaxSpeed();
					// descending sort
					return o2.getMaxSpeed() - o1.getMaxSpeed();
				}
				// ascending sort;
				return o1.getStartTime() - o2.getStartTime();
			}
		});

		startTimeSortedCars = new LinkedList<ArrayList<Car>>();

		int index = 0, startTime = 0;
		ArrayList<Car> cars = null;
		while (index < sortedCars.size()) {
			cars = new ArrayList<>();
			cars.add(sortedCars.get(index++));
			startTime = cars.get(0).getStartTime();

			while (index < sortedCars.size() && sortedCars.get(index).getStartTime() == startTime) {
				cars.add(sortedCars.get(index));
				++index;
			}
			startTimeSortedCars.addLast(cars);
		}

	}

	/**
	 * @version: v1.0
	 * @modified: 2019年3月20日 下午6:40:25
	 * @description: cluster conflict-free cars for concurrent dispatching, must be
	 *               invoked after initCarRoadList();
	 * @return: save results in variable carFlows
	 */
	public static void initCarClusters() {

		for (Car car : MapUtil.cars.values()) {
			for(CarFlow carflow : MapSimulator.carFlows) {
				
				if(carflow.getOrigin() == car.getOrigin() && carflow.getDestination() == car.getDestination()) {
					carflow.addCar(car);
					car.setCarFlow(carflow);
					car = null;
					break;
				}
			}
			
			if(car != null) {
				CarFlow carFlow = new CarFlow();
				carFlow.setOrigin(car.getOrigin());
				carFlow.setDestination(car.getDestination());
				
				//using independent roadList
				ArrayList<Road> list = new ArrayList<>();
				list.addAll(car.getRoadList());
				carFlow.setRoadList(list);
				
				car.setCarFlow(carFlow);
				MapSimulator.carFlows.add(carFlow);
			}
		}
		
		Collections.sort(MapSimulator.carFlows, new Comparator<CarFlow>() {

			@Override
			public int compare(CarFlow o1, CarFlow o2) {
				// TODO Auto-generated method stub
				return o2.getCarFlowSize() - o1.getCarFlowSize();
			}
		});
		
//		for(CarFlow carFlow:MapSimulator.carFlows) {
//			Collections.sort(carFlow.get, new Comparator<>() {
//			});
//		}
		
		logger.info("max cluster: " + MapSimulator.carFlows.get(0).getCarFlowSize() + " cars");
		logger.info(MapUtil.cars.values().size() + " cars to " + MapSimulator.carFlows.size() + " clusters!");
	}

	/**
	 * @version: v1.0
	 * @modified: 2019年3月22日 下午8:00:11
	 * @description: key function
	 * @return: void
	 */
	public static boolean isDeadLockFree(CarFlow queryPath, List<CarFlow> pathSets) {
		
		return true;
	}


	public static void main(String[] args) {
		Integer a = new Integer(3);
		ArrayList<Integer> list = new ArrayList<>();
		list.add(a);
		System.out.println(list.size());
		list.clear();
		System.out.println(a + " list size: " + list.size());
	}
}
