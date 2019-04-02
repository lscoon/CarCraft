package com.huawei.entity;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.huawei.service.JudgeWithFlow;
import com.huawei.util.MapUtil;

public class CarFlow {

	private static final Logger logger = Logger.getLogger(CarFlow.class);
	
	private int origin;
	private int destination;
	private List<Road> roadList;

	private int minTerm = MapUtil.IntMax;
	private int maxSpeed = 0;
	private boolean isRunning = false;
	private boolean isFinished = false;
	
//	 should be sorted by speed from max to min
	private int totalCarCount = 0;
	private ArrayList<Car> outRoadCars;
	private ArrayList<Car> runCars;

	public CarFlow() {
		origin = 0;
		destination = 0;
		roadList = null;
		minTerm = MapUtil.IntMax;
		maxSpeed = 0;
		isRunning = false;
		isFinished = false;
		totalCarCount = 0;
		outRoadCars = new ArrayList<>();
		runCars = new ArrayList<>();
	}
	
	public CarFlow(Car car) {
		origin = car.getOrigin();
		destination = car.getDestination();
		roadList = null;
		minTerm = MapUtil.IntMax;
		maxSpeed = 0;
		isRunning = false;
		isFinished = false;
		totalCarCount = 0;
		outRoadCars = new ArrayList<>();
		runCars = new ArrayList<>();
		addCar(car);
	}
	
	public CarFlow(List<Road> newRoadList, CarFlow lastCarFlow, int num) {
		origin = lastCarFlow.getOrigin();
		destination = lastCarFlow.getDestination();
		roadList = newRoadList;
		minTerm = MapUtil.IntMax;
		maxSpeed = 0;
		isRunning = false;
		isFinished = false;
		totalCarCount = 0;
		outRoadCars = new ArrayList<>();
		runCars = new ArrayList<>();
		
		for(int i=lastCarFlow.outRoadCars.size()-num; i<lastCarFlow.outRoadCars.size(); i++) {
			Car car = lastCarFlow.getOutRoadCars().get(i);
			addCar(car);
		}
	}
	
	public CarFlow split(List<Road> newRoadList, int num) {
		CarFlow newCarFlow = new CarFlow(newRoadList, this, num);
		while(num > 0) {
			outRoadCars.remove(outRoadCars.get(outRoadCars.size()-1));
			num--;
		}
		return newCarFlow;
	}
	
	public void startoff() {
		isRunning = true;
		updateLoad(outRoadCars.size());
		for(Car car : outRoadCars) {
			ArrayList<Road> list = new ArrayList<>();
			list.addAll(roadList);
			car.setRoadList(list);
			car.setNextRoad(list.get(0));
		}
	}
	
	public void updateLoad(int change) {
		roadList.get(0).changeLoad(origin, change);
		for(int i=1; i<roadList.size(); i++)
			roadList.get(i).changeLoad(roadList.get(i-1), change);
	}
	
	public boolean isLoadFree() {
		if(outRoadCars.size() + roadList.get(0).getLoad(origin) 
				> MapUtil.RoadMaxLoad * roadList.get(0).getLanesNum())
			return false;
		for(int i=1; i<roadList.size(); i++)
			if(outRoadCars.size() + roadList.get(i).getLoad(roadList.get(i-1)) 
					> MapUtil.RoadMaxLoad * roadList.get(i).getLanesNum())
				return false;
		return true;
	}
	
	public ArrayList<Car> getNowStartOffCars(JudgeWithFlow judgeWithFlow) {
		ArrayList<Car> startOffCarList = new ArrayList<>();
		if(outRoadCars.size()==0)
			return startOffCarList;
		int blankNum = roadList.get(0).getBlankNum(origin, maxSpeed);
		if(blankNum==0)
			return startOffCarList;
		int nowLoad = roadList.get(0).getLoad(origin);
		blankNum = totalCarCount * blankNum / nowLoad + MapUtil.NowStartOffCarsAdd;
//		 find blankNum cars to start
		for(int i=0; i<outRoadCars.size(); i++) {
			Car car = outRoadCars.get(i);
			if(car != null) {
				if (car.getRealStartTime() <= judgeWithFlow.getTerm()) {
					startOffCarList.add(car);
					outRoadCars.set(i, null);
					blankNum--;
				}
			}
			if(blankNum==0)
				break;
		}
//		 reset maxSpeed
		for(int i=0; i<outRoadCars.size(); i++)
			if(outRoadCars.get(i)!=null) {
				Car car = outRoadCars.get(i);
				if(car.getMaxSpeed() < maxSpeed)
					maxSpeed = car.getMaxSpeed();
				break;
			}
		for(Car car:startOffCarList) {
			if(car.isRunning())
				logger.error("start off running cars");;
			car.setRealStartTime(judgeWithFlow.getTerm());
		}
		runCars.addAll(startOffCarList);
		return startOffCarList;
	}
	
	/**
	 * @version: v1.0
	 * @modified: 2019年3月22日 下午7:21:42
	 * @description: maxSpeed car problem
	 * @param car
	 * @return: void
	 */
	public void putback(Car car) {
		for(int i=0; i<outRoadCars.size(); i++)
			if(outRoadCars.get(i)==null) {
				outRoadCars.set(i, car);
				break;
			}
		runCars.remove(car);
		if (car.getMaxSpeed() > maxSpeed)
			maxSpeed = car.getMaxSpeed();
	}

	public boolean checkIfFinished() {
		for(int i=0; i<outRoadCars.size(); i++) {
			Car car = outRoadCars.get(i);
			if(car==null) {
				outRoadCars.remove(car);
				i--;
			}
		}
		
		if(outRoadCars.size() == 0) {
			if(!roadList.get(0).containsCarFlow(origin, this)) {
				origin = roadList.get(0).getAnOtherCross(origin);
				roadList.remove(roadList.get(0));
			}
		}
		
		if (outRoadCars.size() == 0 && runCars.size() == 0) {
			isRunning = false;
			isFinished = true;
			return true;
		} else
			return false;
	}

	public void addCar(Car car) {
		outRoadCars.add(car);
		totalCarCount++;
		if(totalCarCount > MapUtil.CarFlowMaxCarCount)
			MapUtil.CarFlowMaxCarCount = totalCarCount;
		if(maxSpeed < car.getMaxSpeed())
			maxSpeed = car.getMaxSpeed();
		if(minTerm > car.getStartTime())
			minTerm = car.getStartTime();
		car.setCarFlow(this);
	}
	
	public int getDestination() {
		return destination;
	}

	public void setDestination(int destination) {
		this.destination = destination;
	}

	public void setOrigin(int origin) {
		this.origin = origin;
	}
	
	public int getOrigin() {
		return origin;
	}

	public boolean isRunning() {
		return isRunning;
	}

	public boolean isFinished() {
		return isFinished;
	}

	public void setRoadList(List<Road> roadList) {
		this.roadList = roadList;
	}

	public List<Road> getRoadList() {
		return roadList;
	}
	
	public int getCarFlowSize() {
		return outRoadCars.size();
	}

	public int getMinTerm() {
		return minTerm;
	}

	public void setMinTerm(int minTerm) {
		this.minTerm = minTerm;
	}

	public int getMaxSpeed() {
		return maxSpeed;
	}

	public void setMaxSpeed(int maxSpeed) {
		this.maxSpeed = maxSpeed;
	}

	public ArrayList<Car> getOutRoadCars() {
		return outRoadCars;
	}

//	public void setOutRoadCars(ArrayList<Car> outRoadCars) {
//		this.outRoadCars = outRoadCars;
//		totalCarCount = outRoadCars.size();
//		if(totalCarCount > MapUtil.CarFlowMaxCarCount)
//			MapUtil.CarFlowMaxCarCount = totalCarCount;
//	}

	public ArrayList<Car> getRunCars() {
		return runCars;
	}

	public void setRunCars(ArrayList<Car> runCars) {
		this.runCars = runCars;
	}

	public void setRunning(boolean isRunning) {
		this.isRunning = isRunning;
	}

	public void setFinished(boolean isFinished) {
		this.isFinished = isFinished;
	}
}
