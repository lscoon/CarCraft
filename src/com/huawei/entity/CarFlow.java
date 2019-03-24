package com.huawei.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import com.huawei.service.MapSimulator;
import com.huawei.util.MapUtil;

public class CarFlow {

	private static final Logger logger = Logger.getGlobal();
	
	private int origin;
	private int destination;
	private List<Road> roadList;

	private int minTerm = 0;
	private int maxSpeed = 0;
	private boolean isRunning = false;
	private boolean isFinished = false;

	// should be sorted by speed from max to min
	private ArrayList<Car> outRoadCars = new ArrayList<>();
	private ArrayList<Car> runCars = new ArrayList<>();

	public void startoff() {
		isRunning = true;
	}

	public ArrayList<Car> getNowStartOffCars() {
		ArrayList<Car> startOffCarList = new ArrayList<>();
		int blankNum = roadList.get(0).getBlankNum(origin, maxSpeed);
		if(blankNum==0)
			return startOffCarList;
		int nowSize = getNowSize();
		if(nowSize!=1)
			blankNum = (blankNum / nowSize)+1;
		//if(blankNum==0)
		//	blankNum = 1;
		// find blankNum cars to start
		for(int i=0; i<outRoadCars.size(); i++) {
			Car car = outRoadCars.get(i);
			if(car != null) {
				if (car.getStartTime() <= MapSimulator.term) {
					startOffCarList.add(car);
					outRoadCars.set(i, null);
					blankNum--;
				}
			}
			if(blankNum==0)
				break;
		}
		// reset maxSpeed
		for(int i=0; i<outRoadCars.size(); i++)
			if(outRoadCars.get(i)!=null) {
				Car car = outRoadCars.get(i);
				if(car.getMaxSpeed() < maxSpeed)
					maxSpeed = car.getMaxSpeed();
				break;
			}
		for(Car car:startOffCarList) {
			if(car.isRunning())
				System.out.println("11");
		}
		runCars.addAll(startOffCarList);
		return startOffCarList;
	}

	private int getNowSize() {
		if(!MapSimulator.carFlowOriginRoadNummap.containsKey(roadList.get(0)))
			logger.info("error in car flow get now size");
		int num = MapSimulator.carFlowOriginRoadNummap.get(roadList.get(0));
		if(roadList.get(0).getOrigin().getCrossId() == origin)
			num = num % MapUtil.CarFlowNumTag;
		else num = num / MapUtil.CarFlowNumTag;
		return num;
	}
	
	/**
	 * @version: v1.0
	 * @modified: 2019年3月22日 下午7:21:42
	 * @description: maxSpeed car problem???????????????????
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
		if (outRoadCars.size() == 0 && runCars.size() == 0) {
			isRunning = false;
			isFinished = true;
			return true;
		} else
			return false;
	}

	public void addCar(Car car) {
		outRoadCars.add(car);
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

	public void setOutRoadCars(ArrayList<Car> outRoadCars) {
		this.outRoadCars = outRoadCars;
	}

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
