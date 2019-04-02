package com.huawei.service;

import java.sql.Time;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import com.huawei.entity.Car;
import com.huawei.entity.Cross;
import com.huawei.entity.Road;
import com.huawei.ui.MapFrame;
import com.huawei.util.MapUtil;

public class Judge {
	
	private static final Logger logger = Logger.getLogger(Judge.class);
	
	protected int term = 0;
	protected int realStartTerm = -1;
	
	protected List<Car> outRoadCars = new LinkedList<>();
	protected Set<Car> nowRunCars = new HashSet<>();
	protected Set<Car> finishCars = new HashSet<>();
	
	protected Set<Car> nowWaitedCars = new HashSet<>();
	
	private int finishCount = 0;
	
	public Judge() {
	}
	
	public Judge(int term) {
		this.term = term;
	}
	
	protected void init() {
		term = 0;
		realStartTerm = -1;
		
		outRoadCars.clear();
		nowRunCars.clear();
		finishCars.clear();
		nowWaitedCars.clear();
		
		for (Car i : MapUtil.cars.values())
			outRoadCars.add(i);
		
		Collections.sort(outRoadCars, new Comparator<Car>() {
			@Override
			public int compare(Car o1, Car o2) {
				if(o2.getRealStartTime() == o1.getRealStartTime())
					return o1.getCarId() - o2.getCarId();
				return o1.getRealStartTime() - o2.getRealStartTime();
			}
		});
	}
	
	public void runWithView() {
		logger.info("start run judge in " + term);
		init();
		MapUtil.mapView = new MapFrame(this);
	}
	
	public void runWithoutView() {
		logger.info("start run judge in " + term);
		init();
		while (finishCars.size() != MapUtil.cars.size()) {
			runInOneTerm();
			term++;
		}
		logger.info("end run judge in " + term);
	}
	
	public void runInOneTerm() {
//		logger.info("start in " + term);
		if(term % 1000 == 0)
			logger.info("Judge reach " + term);
		nowWaitedCars.addAll(nowRunCars);
		for (Car car : nowWaitedCars) {
			car.setWaited(true);
		}
		
//		logger.info("start step one");
		int stepOneCount = 0;
		Iterator<Map.Entry<Integer, Road>> iterator = MapUtil.roads.entrySet().iterator();
		while (iterator.hasNext()) {
			Map.Entry<Integer, Road> entry = iterator.next();
			stepOneCount += RuleHandle.driveCarJustOnRoadToEndState(this,entry.getValue());
		}
//		logger.info("end step one: update " + stepOneCount + " cars");
//		logger.info("leave " + nowWaitedCars.size() + " cars waited");
		
//		logger.info("start step two");
		int stepTwoCount = 0;
		int stepTwoTimes = 0;
		while (nowWaitedCars.size() != 0) {
			
			int count = 0;
			for (int i = 0; i < MapUtil.crossSequence.size(); i++) {
				Cross cross = MapUtil.crosses.get(MapUtil.crossSequence.get(i));
				count += RuleHandle.updateCross(this,cross);
			}
			if(count==0) {
				findDeadLock();
				System.exit(term);
			}
			stepTwoCount += count;
			stepTwoTimes++;
		}
//		logger.info("end step two: update " + stepTwoCount + " cars in "
//				+ stepTwoTimes + " iterators");
		
//		logger.info("start step three");
		int stepThreeCount = RuleHandle.driveCarInGarage(this);
//		logger.info("end step three: start off " + stepThreeCount + " cars");
		
		if(nowRunCars.size()!=0)
			if(realStartTerm == -1)
				realStartTerm = term;
	}
	
	private void findDeadLock() {
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
		logger.info("dead lock happen in " + term);
	}

	public List<Car> getOutRoadCars() {
		return outRoadCars;
	}
	
	public Set<Car> getNowWaitedCars() {
		return nowWaitedCars;
	}

	public Set<Car> getNowRunCars() {
		return nowRunCars;
	}

	public Set<Car> getFinishCars() {
		return finishCars;
	}

	public int getFinishCount() {
		return finishCount;
	}

	public void setFinishCount(int finishCount) {
		this.finishCount = finishCount;
	}
	
	public int getRealStartTerm() {
		return realStartTerm;
	}
	
	public int getTerm() {
		return term;
	}

	public void setTerm(int term) {
		this.term = term; 
	}

	public boolean isDispatchFinished() {
		return finishCars.size() == MapUtil.cars.size();
	}
}
