package com.huawei.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.log4j.Logger;

import com.huawei.entity.Car;
import com.huawei.entity.CarFlow;
import com.huawei.entity.Road;
import com.huawei.ui.MapFrame;
import com.huawei.util.DijkstraUtil;
import com.huawei.util.MapUtil;

public class JudgeWithFlow extends Judge {

	private static final Logger logger = Logger.getLogger(JudgeWithFlow.class);

	private static int splitFlowTag = -1;
	private static int carFlowFinishCount = 0;
	
	private static ArrayList<CarFlow> outRoadCarFlows = new ArrayList<>();
	private static List<CarFlow> nowRunCarFlows = new ArrayList<>();
	
	public JudgeWithFlow(int term) {
		this.term = term;
	}
	
	@Override
	protected void init() {
		term = 0;
		realStartTerm = -1;
		
		outRoadCars.clear();
		nowRunCars.clear();
		finishCars.clear();
		nowWaitedCars.clear();
		
		checkParameters();
		
		for(CarFlow carFlow : MapUtil.carFlows)
			outRoadCarFlows.add(carFlow);
		driveCarFlows();
	}
	
	private void checkParameters() {
		if(MapUtil.ExpectedFlowSize > MapUtil.RoadMaxLoad) {
			logger.info("maybe error in road max load");
		}
	}
	
	@Override
	public void runWithView() {
		logger.info("start run judge with flow in " + term);
		init();
		MapUtil.mapView = new MapFrame(this);
	}
	
	@Override
	public void runWithoutView() {
		logger.info("start run judge with flow in " + term);
		init();
		while (finishCars.size() != MapUtil.cars.size()) {
			runInOneTerm();
			term++;
		}
		logger.info("end run judge with flow in " + term);
	}
	
	@Override
	public void runInOneTerm() {
		int count = 0;
		for(CarFlow carFlow : nowRunCarFlows) {
			ArrayList<Car> list = carFlow.getNowStartOffCars(this);
			outRoadCars.addAll(list);
			count += list.size();
		}
//		logger.info("add " + count + " cars to outRoadCars");
		
		Collections.sort(outRoadCars, new Comparator<Car>() {
			@Override
			public int compare(Car o1, Car o2) {
				if(o2.getRealStartTime() == o1.getRealStartTime())
					return o1.getCarId() - o2.getCarId();
				return o1.getRealStartTime() - o2.getRealStartTime();
			}
		});
		
		super.runInOneTerm();
		
//		logger.info("put back " + outRoadCars.size() + " cars");
		for(Car car : outRoadCars)
			car.getCarFlow().putback(car);
		outRoadCars.clear();
		
		handleFlowDuringRun();
	}
	
	private void handleFlowDuringRun() {
		term++;
		if(checkFinishFlow() > 0) {
			if(splitFlowTag >= MapUtil.SplitFlowThreshhold) {
//				splitFlow();
				splitFlowTag = 0;
			}
//			logger.info("finish " + count + " car flows");
			if(carFlowFinishCount >= MapUtil.MaxCarFlowFinishCount || 
					outRoadCarFlows.size() < MapUtil.MaxCarFlowFinishCount) {
				carFlowFinishCount = 0;
				driveCarFlows();
			}	
		}
		else if(nowRunCarFlows.size() == 0) {
//			logger.info("now no car flow");
			driveCarFlows();
		}
		if(splitFlowTag == -1)
			if(outRoadCarFlows.size() ==0) {//< MapUtil.SplitBeginOutRoadCarFlowNum) {
				splitFlowTag = 0;
//				splitFlow();
			}
		term--;
	}
	
	private int checkFinishFlow() {
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
		return count;
	}
	
	
	
	private void driveCarFlows() {
		int count = 0;
		int failCount = 0;
		for(int i=0; i<outRoadCarFlows.size(); i++) {
			CarFlow carflow = outRoadCarFlows.get(i);
			if(carflow.getMinTerm() <= term) {
				if(carflow.isLoadFree()) {
					if(SolverWithFlow.isOverlayLoopFree(carflow, nowRunCarFlows)) {
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
					List<Road> newRoadList = DijkstraUtil.Dijkstra(carflow.getOrigin(), 
							carflow.getDestination(), MapUtil.CarMaxSpeed, null);
					if(newRoadList!=null && newRoadList.size() - carflow.getRoadList().size() <= MapUtil.RoadListMaxIncrease) {
						List<Road> oldRoadList = carflow.getRoadList();
						carflow.setRoadList(newRoadList);
						if(carflow.isLoadFree()) {
							if(SolverWithFlow.isOverlayLoopFree(carflow, nowRunCarFlows)) {
								carflow.startoff();
								outRoadCarFlows.remove(carflow);
								nowRunCarFlows.add(carflow);
								i--;
								count++;
							}
							else{
								carflow.setRoadList(oldRoadList);
								failCount++;
							}
						}
						else carflow.setRoadList(oldRoadList);
					}
				}
				
				if(failCount > MapUtil.MaxFailCount)
					break;
			}
		}
		
//		logger.info("add " + count + " car flows");
	}
	
//	private static void splitFlow() {
//		Collections.sort(nowRunCarFlows, new Comparator<CarFlow>() {
//			@Override
//			public int compare(CarFlow o1, CarFlow o2) {
//				return o2.getOutRoadCars().size() - o1.getOutRoadCars().size();
//			}
//		});
//		int size = nowRunCarFlows.size();
//		for(int i=0; i<MapUtil.SelectedFlowNum && i<size; i++) {
//			CarFlow carflow = nowRunCarFlows.get(i);
//			if(carflow.getOutRoadCars().size() < 20) 
//				break;
//			List<Road> roadList = DijkstraUtil.Dijkstra(carflow.getOrigin(), 
//					carflow.getDestination(), MapUtil.CarMaxSpeed, null);
//			if(roadList!=null) {
//				int num = carflow.getOutRoadCars().size()*carflow.getRoadList().size()/
//						(roadList.size()+carflow.getRoadList().size());
//				CarFlow tempCarFlow = new CarFlow(roadList, carflow, num);
//				if(tempCarFlow.isLoadFree()) {
//					if(SolverWithFlow.isOverlayLoopFree(tempCarFlow, nowRunCarFlows)) {
//						tempCarFlow.refreshOutRoadCarCarFlows();
//						carflow.split(num);
//						System.out.println(tempCarFlow.getOutRoadCars().size()+","+carflow.getOutRoadCars().size());
//						MapUtil.carFlows.add(tempCarFlow);
//						nowRunCarFlows.add(tempCarFlow);
//						System.out.println("split carflow in term " + term);
//					}
//					else System.out.println("overlay error");
//				}
//				else System.out.println("load error");
//			}
//		}
//	}
}
