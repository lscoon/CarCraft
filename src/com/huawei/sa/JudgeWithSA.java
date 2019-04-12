package com.huawei.sa;

import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.huawei.entity.Car;
import com.huawei.entity.Cross;
import com.huawei.entity.Road;
import com.huawei.service.Judge;
import com.huawei.ui.MapFrame;
import com.huawei.util.FileUtil;
import com.huawei.util.FloydUtil;
import com.huawei.util.MapUtil;

public class JudgeWithSA extends Judge{
	
	private static final Logger logger = Logger.getLogger(JudgeWithSA.class);
	
	public JudgeWithSA(int term) {
		this.term = term;
	}
	
	@Override
	protected void init() {
		term = 0;
		nowRunCars.clear();
		finishCars.clear();
		nowWaitedCars.clear();
		
		List<Integer> carIds = new LinkedList<>();
		for(Car car : MapUtil.cars.values()) {
			if(car.isPreset())
				carIds.add(car.getCarId());
		}
		for(Integer id : carIds)
			MapUtil.cars.remove(id);
		
		for(Car car : MapUtil.cars.values())
			if(car.isPreset())
				MapUtil.cars.remove(car.getCarId());
		
		for(Car car : MapUtil.cars.values()) {
			Cross cross = MapUtil.crosses.get(car.getOrigin());
			if(car.isPriority())
				cross.priOutRoadCars.add(car);
			else cross.outRoadCars.add(car);
		}
		
		for(Cross cross : MapUtil.crosses.values()) {
			Collections.sort(cross.priOutRoadCars, new Comparator<Car>() {
				@Override
				public int compare(Car o1, Car o2) {
					if(o2.getRealStartTime() == o1.getRealStartTime())
						return o1.getCarId() - o2.getCarId();
					return o1.getRealStartTime() - o2.getRealStartTime();
				}
			});
			Collections.sort(cross.outRoadCars, new Comparator<Car>() {
				@Override
				public int compare(Car o1, Car o2) {
					if(o2.getRealStartTime() == o1.getRealStartTime())
						return o1.getCarId() - o2.getCarId();
					return o1.getRealStartTime() - o2.getRealStartTime();
				}
			});
		}
		
			
	}
	
	@Override
	public void runWithView() {
		logger.info("start run judge with SA in " + term);
		init();
		MapUtil.mapView = new MapFrame(this);
	}
	
	@Override
	public void runWithoutView() {
		logger.info("start run judge with SA in " + term);
		init();
		while(finishCars.size() != MapUtil.cars.size()) {
			runInOneTerm();
		}
		logger.info("end run judge with SA in " + term);
	}
	
	@Override
	public void runInOneTerm() {
		if(term % 10 == 0)
			logger.info("Judge reach " + term + ",nowRun " + nowRunCars.size() +",finish"+ finishCars.size() );
		nowWaitedCars.addAll(nowRunCars);
		for(Car car : nowWaitedCars)
			car.setWaited(true);
		
		for(Road road: MapUtil.roads.values()) {
			road.getForwardRoad().inLoad = 0;
			road.getForwardRoad().outLoad = 0;
			if(road.isBiDirect()) {
				road.getBackwardRoad().inLoad = 0;
				road.getBackwardRoad().outLoad = 0;
			}
		}
		for(Cross cross : MapUtil.crosses.values()) {
			for(Car car : cross.outRoadCars) {
				if(car.getRealStartTime()<term) {
					if(!car.isPreset())
						car.setRealStartTime(term);
				}
				else break;
			}
			for(Car car : cross.priOutRoadCars) {
				if(car.getRealStartTime()<term) {
					if(!car.isPreset())
						car.setRealStartTime(term);
				}
				else break;
			}
		}
		
		
//		logger.info("start step one");
		int stepOneCount = 0;
		Iterator<Map.Entry<Integer, Road>> iterator = MapUtil.roads.entrySet().iterator();
		while (iterator.hasNext()) {
			Map.Entry<Integer, Road> entry = iterator.next();
			stepOneCount += RuleHandleWithSA.driveCarJustOnRoadToEndState(this,entry.getValue());
		}
//		logger.info("end step one: update " + stepOneCount + " cars");
//		logger.info("leave " + nowWaitedCars.size() + " cars waited");
		
		int priAddCount = RuleHandleWithSA.drivePriCarInGarage(this);
//		logger.info("add " + priAddCount + " pri cars");
		
//		logger.info("start step two");
		int stepTwoCount = 0;
		int stepTwoTimes = 0;
		while (nowWaitedCars.size() != 0) {
			
			int count = 0;
			for (int i = 0; i < MapUtil.crossSequence.size(); i++) {
				Cross cross = MapUtil.crosses.get(MapUtil.crossSequence.get(i));
				count += RuleHandleWithSA.updateCross(this,cross);	
			}
//			logger.info("count:" + count);
			if(count==0) {
				for(Car car :nowWaitedCars)
					if(!car.checkWaitLink())
						logger.info("car:" + car.getCarId());
				findDeadLock();
			}
			stepTwoCount += count;
			stepTwoTimes++;
		}
//		logger.info("end step two: update " + stepTwoCount + " cars in "
//				+ stepTwoTimes + " iterators");
		
//		logger.info("start step three");
		priAddCount = RuleHandleWithSA.drivePriCarInGarage(this);
//		logger.info("end step three: start off " + priAddCount + " pri cars");
		int stepThreeCount = RuleHandleWithSA.driveCarInGarage(this);
//		logger.info("end step three: start off " + stepThreeCount + " cars");
		
		if(nowRunCars.size()!=0)
			if(realStartTerm == -1)
				realStartTerm = term;
		
		term++;
		BPRLinkPerformance.updateAdjMatrixR();
	}
	
	public static void main(String[] args) {
		args = FileUtil.initFiles("maps/2-map-training-1/");
		String carPath = args[0];
        String roadPath = args[1];
        String crossPath = args[2];
        String presetAnswerPath = args[3];
        String answerPath = args[4];
        FileUtil.readInputs(carPath, roadPath, crossPath, presetAnswerPath);
        
        FloydUtil.initPathAndDistMatrixMap();
        BPRLinkPerformance.initAdjMatrixR();
        
        JudgeWithSA judgeWithSA = new JudgeWithSA(0);
        judgeWithSA.runWithoutView();
//        judgeWithSA.runWithView();
	}
	
}
