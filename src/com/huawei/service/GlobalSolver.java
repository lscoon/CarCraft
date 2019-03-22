package com.huawei.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
			for (CarFlow carflow : MapSimulator.carFlows) {

				if (carflow.getOrigin() == car.getOrigin() && carflow.getDestination() == car.getDestination()) {
					carflow.addCar(car);
					if (carflow.getMaxSpeed() < car.getMaxSpeed()) {
						carflow.setMaxSpeed(car.getMaxSpeed());
					}
					if (carflow.getMinTerm() > car.getStartTime()) {
						carflow.setMinTerm(car.getStartTime());
					}
					car.setCarFlow(carflow);
					car = null;
					break;
				}
			}

			if (car != null) {
				CarFlow carFlow = new CarFlow();
				carFlow.setOrigin(car.getOrigin());
				carFlow.setDestination(car.getDestination());

				// using independent roadList
				ArrayList<Road> list = new ArrayList<>();
				list.addAll(car.getRoadList());
				carFlow.setRoadList(list);
				carFlow.setMaxSpeed(car.getMaxSpeed());
				carFlow.setMinTerm(car.getStartTime());
				carFlow.addCar(car);
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

		for (CarFlow carFlow : MapSimulator.carFlows) {
			Collections.sort(carFlow.getOutRoadCars(), new Comparator<Car>() {

				@Override
				public int compare(Car arg0, Car arg1) {
					// TODO Auto-generated method stub
					if (arg0.getMaxSpeed() == arg1.getMaxSpeed()) {
						return arg0.getStartTime() - arg1.getStartTime();
					}
					return arg1.getMaxSpeed() - arg0.getMaxSpeed();
				}
			});
		}

//		logger.info("size::"+MapSimulator.carFlows.get(MapSimulator.carFlows.size()-1).getCarFlowSize());
		logger.info("max cluster: " + MapSimulator.carFlows.get(0).getCarFlowSize() + " cars");
		logger.info(MapUtil.cars.values().size() + " cars to " + MapSimulator.carFlows.size() + " clusters!");
	}

	/**
	 * @version: v1.0
	 * @modified: 2019年3月22日 下午8:00:11
	 * @description: key function, do not try to change anything!
	 * @return: void
	 */
	public static boolean isDeadLockFree(CarFlow queryPath, List<CarFlow> pathSets) {
		
		// Map<roadList id, road index>
		Map<Integer, Set<Integer>> visitedPoints = new HashMap<>();
		// Map<roadList id, road index>
		Map<Integer, Integer> contradictingSets = new HashMap<>();
		// linkedList to simulate stack
		LinkedList<AccessPoint> stack = new LinkedList<>();

		List<Road> mainRoad = queryPath.getRoadList();		
		

		for (Road road : mainRoad) {
			//visitedPoints.clear();
			stack.clear();
			for (int i = 0;i<pathSets.size();++i) {
				CarFlow carFlow = pathSets.get(i);
				int priority = road.computePriority(queryPath, carFlow);
				int index = carFlow.getRoadList().indexOf(road);
				if(priority  > 0) {
					// below relation	
					if(index == -1) logger.error("unexpected error");
					contradictingSets.put(i,index);
				}else if(priority < 0) {
					// above relation
					stack.addFirst(new AccessPoint(carFlow.getRoadList(), index, i));
				}
			}
			// iterative invoke
			while (stack.size() > 0) {
				AccessPoint node = stack.getFirst();
				stack.removeFirst();

				if(visitedPoints.get(node.roadId) == null) {
					// not visited, label it
					Set<Integer> sets = new HashSet<>();
					sets.add(node.index);
					visitedPoints.put(node.roadId, sets);
				}else if(visitedPoints.get(node.roadId).contains(node.index)) {
					// visited
					continue;
				}else {
					// not visited, label it
					visitedPoints.get(node.roadId).add(node.index);
				}
				
				//check if loop exist
				if(contradictingSets.get(node.roadId) != null && node.index <= contradictingSets.get(node.roadId)) {
					return false;
				}
				
//				if(node.index == node.roadList.size() - 1) {
//					//last road of roadList
//					continue;
//				}
				CarFlow carFlow = pathSets.get(node.roadId);
				Road road1 = carFlow.getRoadList().get(node.index);
				for (int i = 0;i<pathSets.size();++i) {
					int priority = road1.computePriority(carFlow, pathSets.get(i));
					int index = pathSets.get(i).getRoadList().indexOf(road1);
					
					if(priority < 0 && (visitedPoints.get(i) == null || !visitedPoints.get(i).contains(index))) {
						// above relation
						stack.addFirst(new AccessPoint(carFlow.getRoadList(), index, i));
					}
				}
				// add itself's successor
				if(node.index < (node.roadList.size()-1)) {
					stack.addFirst(new AccessPoint(carFlow.getRoadList(), node.index + 1, node.roadId));
				}
				
			}
		}

		return true;
	}

	public static void main(String[] args) {
		Map<Integer, Integer> dict = new HashMap<>();
		dict.put(new Integer(10), 5);

		System.out.println(dict.get(10));
		System.out.println(dict.get(new Integer(10)));
	}
}

class AccessPoint {
	public List<Road> roadList = null;
	public int index = 0;
	public int roadId = 0;

	public AccessPoint(List<Road> roadList, int index, int roadId) {
		// TODO Auto-generated constructor stub
		this.roadList = roadList;
		this.index = index;
		this.roadId = roadId;
	}
}
