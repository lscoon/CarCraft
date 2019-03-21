package com.huawei.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import com.huawei.entity.Car;
import com.huawei.entity.CarFlow;
import com.huawei.entity.Road;
import com.huawei.util.FloydUtil;
import com.huawei.util.MapUtil;

public class GlobalSolver {
	private static final Logger logger = Logger.getLogger(GlobalSolver.class);

	public static LinkedList<CarFlow> carFlows = new LinkedList<>();
	
	private static LinkedList<ArrayList<Car>> startTimeSortedCars = null;

	private static ArrayList<ArrayList<Car>> conflictFreeCarClusters = null;
	private static ArrayList<Set<Integer>> conflictFreeCarPathSets = null;

	public static void initSolver() {
		//MapSimulator.initMap();
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
		while (!MapSimulator.isDispatchFinished()) {
			MapSimulator.updateMap();
			MapSimulator.term++;
			updateCarRoadList();
		}*/
		
		for(ArrayList<Car> carList : conflictFreeCarClusters) {
			if(carList == null)
				continue;
			MapSimulator.runMapWithCarList(carList);
		}
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
	 * @return: save results in variable conflictFreeCarClusters
	 */
	public static void initCarClusters() {
		conflictFreeCarClusters = new ArrayList<ArrayList<Car>>();
		conflictFreeCarPathSets = new ArrayList<Set<Integer>>();

		ArrayList<Car> carCluster = null;
		Set<Integer> roadIdSet = null;
		Set<Integer> currentCarRoadIdSet = null;
		List<Road> roadSeq = null;
		for (Car car : MapUtil.cars.values()) {
			currentCarRoadIdSet = new HashSet<Integer>();
			roadSeq = car.getRoadList();
			for (Road road : roadSeq) {
				currentCarRoadIdSet.add(road.getRoadId());
			}

			for (int i = 0; i < conflictFreeCarClusters.size(); ++i) {
				// check if current car belongs to any already existed cluster
				roadIdSet = conflictFreeCarPathSets.get(i);

				// case 1: current car roadList belongs to current carCluster
				if (roadIdSet.contains(roadSeq.get(0).getRoadId())
						&& roadIdSet.contains(roadSeq.get(roadSeq.size() - 1).getRoadId())) {

					conflictFreeCarClusters.get(i).add(car);
					car = null;
					break;
				}
				// case 2: carCluster belongs to current car roadList
				if (currentCarRoadIdSet.containsAll(roadIdSet)) {
					conflictFreeCarClusters.get(i).add(car);
					conflictFreeCarPathSets.set(i, currentCarRoadIdSet);
					roadIdSet.clear();
					car = null;
					break;
				}
			}
			// create new group to save current car
			if (car != null) {
				carCluster = new ArrayList<Car>();
				carCluster.add(car);
				conflictFreeCarClusters.add(carCluster);
				conflictFreeCarPathSets.add(currentCarRoadIdSet);
			}
		}

		// merge disjoint path
		for (int i = 0; i < conflictFreeCarClusters.size(); ++i) {
			roadIdSet = conflictFreeCarPathSets.get(i);
			for (int j = i + 1; j < conflictFreeCarClusters.size(); ++j) {
				currentCarRoadIdSet = conflictFreeCarPathSets.get(j);
				if (roadIdSet != null && currentCarRoadIdSet != null
						&& Collections.disjoint(roadIdSet, currentCarRoadIdSet)) {
					// merge to carCluster
					conflictFreeCarClusters.get(i).addAll(conflictFreeCarClusters.get(j));
					conflictFreeCarPathSets.get(i).addAll(conflictFreeCarPathSets.get(j));
					conflictFreeCarClusters.set(j, null);
					conflictFreeCarPathSets.set(j, null);
				}
			}
		}

		Collections.sort(conflictFreeCarClusters, new Comparator<ArrayList<Car>>() {

			@Override
			public int compare(ArrayList<Car> arg0, ArrayList<Car> arg1) {
				if (arg0 == null && arg1 == null) {
					return 0;
				} else if (arg1 == null) {
					return -1;
				} else if (arg0 == null) {
					return 1;
				} else {
					return arg1.size() - arg0.size();
				}
			}
		});
		
		// debug only;
		int counter = 0;
		for(ArrayList<Car> Cluster: conflictFreeCarClusters) {
			if(Cluster !=null) {
				++counter;
			}
		}

		logger.info("max cluster: " + conflictFreeCarClusters.get(0).size() + " cars");
		logger.info(MapUtil.cars.values().size() + " cars to " + counter + " clusters!");
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
