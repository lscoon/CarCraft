package com.huawei.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import org.apache.log4j.Logger;

import com.huawei.entity.Car;
import com.huawei.entity.CarFlow;
import com.huawei.entity.Road;
import com.huawei.util.DijkstraUtil;
import com.huawei.util.FloydUtil;
import com.huawei.util.MapUtil;

public class SolverWithFlow {
	private static final Logger logger = Logger.getLogger(SolverWithFlow.class);

	/**
	 * @version: v1.0
	 * @modified: 2019年3月20日 下午6:40:25
	 * @description: cluster conflict-free cars for concurrent dispatching, must be
	 *               invoked after initCarRoadList();
	 * @return: save results in variable carFlows
	 */
	public static void initCarClusters() {

		for (Car car : MapUtil.cars.values()) {
			for (CarFlow carflow : MapUtil.carFlows) {
				if (carflow.getOrigin() == car.getOrigin() && carflow.getDestination() == car.getDestination()) {
					carflow.addCar(car);
					car = null;
					break;
				}
			}
			if (car != null) {
				CarFlow carFlow = new CarFlow(car);
				MapUtil.carFlows.add(carFlow);
			}
		}

		initCarFlowRoadList(0);
		splitCarFlows();
		
		Collections.sort(MapUtil.carFlows, new Comparator<CarFlow>() {

			@Override
			public int compare(CarFlow o1, CarFlow o2) {
				// TODO Auto-generated method stub
				if(o2.getRoadList().size() == o1.getRoadList().size())
					return o2.getCarFlowSize() - o1.getCarFlowSize();
				return o2.getRoadList().size() - o1.getRoadList().size();
				
			}
		});

		for (CarFlow carFlow : MapUtil.carFlows) {
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
			if(carFlow.getOutRoadCars().get(0).getStartTime() > carFlow.getMinTerm())
				carFlow.setMinTerm(carFlow.getOutRoadCars().get(0).getStartTime());
		}
		
//		logger.info(MapUtil.cars.values().size() + " cars to " + MapUtil.carFlows.size() + " clusters!");
//		logger.info("max cluster: " + MapUtil.carFlows.get(0).getCarFlowSize() + " cars");
	}

	private static void splitCarFlows() {
		int size = MapUtil.carFlows.size();
		for(int i=0; i<size; i++) {
			CarFlow carflow = MapUtil.carFlows.get(i);
			while(carflow.getOutRoadCars().size() > MapUtil.ExpectedFlowSize) {
				List<Road> newRoadList = DijkstraUtil.Dijkstra(carflow.getOrigin(), 
						carflow.getDestination(), MapUtil.CarMaxSpeed, carflow.getRoadList());
				CarFlow newCarFlow = carflow.split(newRoadList, MapUtil.ExpectedFlowSize);
				MapUtil.carFlows.add(newCarFlow);
			}
		}
	}
	
//	calculate shortest path road sequence
//	0 means global, 1 means local
	private static void initCarFlowRoadList(int globalOrLocalTag) {
		for (CarFlow carflow : MapUtil.carFlows) {
			int originSeq = MapUtil.crossSequence.indexOf(carflow.getOrigin());
			int destinationSeq = MapUtil.crossSequence.indexOf(carflow.getDestination());
			int speed = MapUtil.CarMaxSpeed;
			if(globalOrLocalTag == 1)
				speed = carflow.getMaxSpeed();
			Road[][] pathMatrix = FloydUtil.pathMap.get((Integer) speed);
			List<Road> roadList = new ArrayList<>();
			
			while (originSeq != destinationSeq) {
				Road road = pathMatrix[originSeq][destinationSeq];
				roadList.add(road);
				// get another road cross sequence
				originSeq = MapUtil.crossSequence.indexOf(road.getAnOtherCross(MapUtil.crossSequence.get(originSeq)));
			}
			carflow.setRoadList(roadList);
		}
	}

	/**
	 * @version: v1.0
	 * @modified: 2019年3月23日 下午3:15:54
	 * @description: more strict than isDeadLockFree, very fast
	 * @param queryPath
	 * @param pathSets
	 * @return: boolean
	 */
	public static boolean isOverlayLoopFree(CarFlow queryPath, List<CarFlow> pathSets) {

		Map<Integer, Integer> contradictingSets = new HashMap<>();
		Stack<AccessPoint> stack = new Stack<>();
		
		Map<Integer, Set<Boolean>> visitedRoads = new HashMap<>();

		List<Road> mainRoad = queryPath.getRoadList();

		if (queryPath.getRoadList().size() == 1) {
			return true;
		}

		for (CarFlow flow : pathSets) {
			if (flow.getRoadList().containsAll(queryPath.getRoadList())) {
				// 同向包含
				if (flow.getRoadList().indexOf(queryPath.getRoadList().get(0)) < flow.getRoadList()
						.indexOf(queryPath.getRoadList().get(1))) {
					return true;
				}
			}
		}

		for (int j = 0; j < mainRoad.size(); ++j) {
			Road road = mainRoad.get(j);

			visitedRoads.clear();
			for(Road r:MapUtil.roads.values()) {
				Set<Boolean> set = new HashSet<>();
				visitedRoads.put(r.getRoadId(), set);
			}

			stack.clear();
			for (int i = 0; i < pathSets.size(); ++i) {
				CarFlow carFlow = pathSets.get(i);
				boolean isOverlay = road.isOverlay(queryPath, carFlow);
				if (isOverlay) {
					int index = carFlow.getRoadList().indexOf(road);
					if (contradictingSets.get(i) == null) {
						// and unvisited
						contradictingSets.put(i, index);
					} else {
						if (index <= contradictingSets.get(i))
							return false;
						else {
							contradictingSets.put(i, index);
						}
					}

					if ((index + 1) < carFlow.getRoadList().size() && ((j + 1) == mainRoad.size()
							|| (carFlow.getRoadList().get(index + 1) != mainRoad.get(j + 1)))) {

						Road r1 = carFlow.getRoadList().get(index);
						Road r2 = carFlow.getRoadList().get(index+1);
						if(!visitedRoads.get(r2.getRoadId()).contains(r2.getForwardOrBackward(r1))) {
							stack.push(new AccessPoint(carFlow.getRoadList(), index + 1, i));
						}
						
					}
				}
			}
			Road r1,r2;
			boolean dir;
			while (stack.size() > 0) {
				AccessPoint node = stack.pop();

				r1 = node.roadList.get(node.index-1);
				r2 = node.roadList.get(node.index);
				dir = r2.getForwardOrBackward(r1);
				if(visitedRoads.get(r2.getRoadId()).contains(dir)) {
					continue;
				}else {
					visitedRoads.get(r2.getRoadId()).add(dir);
				}

				if (contradictingSets.get(node.roadId) != null && node.index <= contradictingSets.get(node.roadId)) {
					return false;
				}

				CarFlow carFlow = pathSets.get(node.roadId);
				List<Road> roadList1 = carFlow.getRoadList();
				Road road1 = roadList1.get(node.index);
				for (int i = 0; i < pathSets.size(); ++i) {
					if (node.roadId == i) {
						continue;
					}
					boolean isOverlay = road1.isOverlay(carFlow, pathSets.get(i));
					List<Road> roadList2 = pathSets.get(i).getRoadList();
					int index = roadList2.indexOf(road1);

					if (isOverlay) {
						// check if loop exist
						if ((index + 1) < roadList2.size() && ((node.index + 1) == roadList1.size()
								|| (roadList2.get(index + 1) != roadList1.get(node.index + 1)))) {

							r2 = roadList2.get(index+1);
							r1 = roadList2.get(index);
							if(!visitedRoads.get(r2.getRoadId()).contains(r2.getForwardOrBackward(r1))) {
								stack.push(new AccessPoint(roadList2, index + 1, i));
							}
						}
					}

				}
				// add itself's successor
				if ((node.index + 1) < node.roadList.size()) {
					r2 = roadList1.get(node.index+1);
					r1 = roadList1.get(node.index);
					if(!visitedRoads.get(r2.getRoadId()).contains(r2.getForwardOrBackward(r1))) {
						stack.push(new AccessPoint(roadList1, node.index + 1, node.roadId));
					}
				}
			}
		}
		return true;
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
