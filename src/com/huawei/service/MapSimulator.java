package com.huawei.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
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
import com.huawei.util.DijkstraUtil;
import com.huawei.util.MapUtil;

public class MapSimulator {

	private static final Logger logger = Logger.getLogger(MapSimulator.class);

	public static int term = 5;
	public static int realStartTerm = -1;
	public static int splitFlowTag = -1;
	public static List<Car> outRoadCars = new LinkedList<>();
	public static Set<Car> nowRunCars = new HashSet<>();
	public static Set<Car> finishCars = new HashSet<>();
	
	private static ArrayList<CarFlow> outRoadCarFlows = new ArrayList<>();
	public static List<CarFlow> nowRunCarFlows = new ArrayList<>();
	
	public static int carFlowFinishCount = 0;
	
	public static void runMapWithView() {
		logger.info("start run map in " + term);
		finishCars.clear();
		initMap();
		MapUtil.mapView = new MapFrame();
	}
	
	public static void runMapWithOutView() {
//		logger.info("start run map in " + term);
		finishCars.clear();
		initMap();
		while (finishCars.size() != MapUtil.cars.size()) {
			MapUpdate.updateMap();
//			for(Car car: outRoadCars)
//				if(car.getRealStartTime() < term)
//					System.out.println("11");
			term++;
		}
		logger.info("end run map in " + term);
	}
	
	public static void runMapWithCarFlow() {
		MapUtil.checkParameters();
//		logger.info("start run map with car flow in " + term);
		for(CarFlow carFlow : MapUtil.carFlows) {
			outRoadCarFlows.add(carFlow);
		}
		addRunCarFlows();
		while (finishCars.size() != MapUtil.cars.size()) {
			updateMapWithCarFlow();
			term++;
		}
//		logger.info("end run map with car flow in " + term + "");
	}
	
	public static void runMapWithCarFlowWithView() {
		logger.info("start run map with car flow in " + term + "\n");
		for(CarFlow carFlow : MapUtil.carFlows) {
			outRoadCarFlows.add(carFlow);
		}
		addRunCarFlows();
		MapUtil.mapView = new MapFrame();
		logger.info("end run map with car set in " + term + "\n");
	}
	
	private static void addRunCarFlows() {
//		for(Road road: MapUtil.roads.values()) {
//			if(road.isBiDirect())
//				if(road.getLoad(road.getDestination().getCrossId())!=0)
//					logger.error("road load not zero");
//			if(road.getLoad(road.getOrigin().getCrossId())!=0)
//				logger.error("road load not zero");
//		}
		int count = 0;
		int failCount = 0;
		for(int i=0; i<outRoadCarFlows.size(); i++) {
			CarFlow carflow = outRoadCarFlows.get(i);
			if(carflow.getMinTerm() <= term) {
				if(carflow.isLoadFree()) {
					if(GlobalSolver.isOverlayLoopFree(carflow, nowRunCarFlows)) {
						carflow.startoff();
						outRoadCarFlows.remove(carflow);
						nowRunCarFlows.add(carflow);
						i--;
						count++;
					}
					else{
						failCount++;
					}
				}
				else {
					List<Road> roadList = DijkstraUtil.Dijkstra(carflow.getOrigin(), 
							carflow.getDestination(), MapUtil.CarMaxSpeed, null);
					if(roadList!=null && roadList.size() - carflow.getRoadList().size() <= MapUtil.RoadListMaxIncrease) {
						CarFlow tempCarFlow = new CarFlow(roadList, carflow);
						if(tempCarFlow.isLoadFree()) {
							if(GlobalSolver.isOverlayLoopFree(tempCarFlow, nowRunCarFlows)) {
								carflow.newStartoff(roadList);
								outRoadCarFlows.remove(carflow);
								nowRunCarFlows.add(carflow);
								i--;
								count++;
							}
							else{
								failCount++;
							}
						}
					}
				}
				
				if(failCount > MapUtil.MaxFailCount)
					break;
			}
		}
		
//		logger.info("add " + count + " car flows");
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


	
	public static void updateMapWithCarFlow() {
		MapUpdate.updateMap();
//		System.out.println(nowRunCarFlows.size());
//		logger.info("put back " + outRoadCars.size() + " cars");
		for(Car car : outRoadCars)
			car.getCarFlow().putback(car);
		outRoadCars.clear();
		
		int count=0;
		for(int i=0; i<nowRunCarFlows.size(); i++) {
			CarFlow carflow = nowRunCarFlows.get(i);
			if(carflow.checkIfFinished()) {
				nowRunCarFlows.remove(i);
				i--;
				count++;
				carFlowFinishCount++;
				if(splitFlowTag != -1)
					splitFlowTag++;
			}
		}
		term++;
		
		// these car flows will start at next term
		if(count > 0) {
			if(splitFlowTag >= MapUtil.SplitFlowThreshhold) {
//				splitFlow();
				splitFlowTag = 0;
			}
//			logger.info("finish " + count + " car flows");
			if(carFlowFinishCount >= MapUtil.MaxCarFlowFinishCount || outRoadCarFlows.size() < MapUtil.MaxCarFlowFinishCount) {
				carFlowFinishCount = 0;
				
				addRunCarFlows();
			}	
		}
		else if(nowRunCarFlows.size() == 0) {
//			logger.info("now no car flow");
			addRunCarFlows();
		}
		if(splitFlowTag == -1)
			if(outRoadCarFlows.size() ==0) {//< MapUtil.SplitBeginOutRoadCarFlowNum) {
				splitFlowTag = 0;
//				splitFlow();
//				System.out.println(nowRunCarFlows.size());
			}
		term--;
	}
	
	private static void splitFlow() {
		Collections.sort(nowRunCarFlows, new Comparator<CarFlow>() {
			@Override
			public int compare(CarFlow o1, CarFlow o2) {
				return o2.getOutRoadCars().size() - o1.getOutRoadCars().size();
			}
		});
		int size = nowRunCarFlows.size();
		for(int i=0; i<MapUtil.SelectedFlowNum && i<size; i++) {
			CarFlow carflow = nowRunCarFlows.get(i);
			if(carflow.getOutRoadCars().size() < 20) 
				break;
			List<Road> roadList = DijkstraUtil.Dijkstra(carflow.getOrigin(), 
					carflow.getDestination(), MapUtil.CarMaxSpeed, null);
			if(roadList!=null) {
				int num = carflow.getOutRoadCars().size()*carflow.getRoadList().size()/
						(roadList.size()+carflow.getRoadList().size());
				CarFlow tempCarFlow = new CarFlow(roadList, carflow, num);
				if(tempCarFlow.isLoadFree()) {
					if(GlobalSolver.isOverlayLoopFree(tempCarFlow, nowRunCarFlows)) {
						tempCarFlow.refreshOutRoadCarCarFlows();
						carflow.split(num);
						System.out.println(tempCarFlow.getOutRoadCars().size()+","+carflow.getOutRoadCars().size());
						MapUtil.carFlows.add(tempCarFlow);
						nowRunCarFlows.add(tempCarFlow);
						System.out.println("split carflow in term " + term);
					}
					else System.out.println("overlay error");
				}
				else System.out.println("load error");
			}
		}
	}
	
	public static boolean isDispatchFinished() {
		return finishCars.size() == MapUtil.cars.size();
	}

}
