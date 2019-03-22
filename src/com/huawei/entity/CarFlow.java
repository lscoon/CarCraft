package com.huawei.entity;

import java.util.ArrayList;
import java.util.List;

import com.huawei.service.MapSimulator;

public class CarFlow {

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

		for(int i=0; i<outRoadCars.size(); i++) {
			Car car = outRoadCars.get(i);
			if()
			if(car.getStartTime() <= MapSimulator.term) {
				startOffCarList.add(car);
				outRoadCars.set(i, null);
				blankNum--;
			}
			if(blankNum==0)
				break;
		}
		

		outRoadCars.removeAll(startOffCarList);
		runCars.addAll(startOffCarList);
		return startOffCarList;
	}

	
	/**
	 * @version: v1.0
	 * @modified: 2019年3月22日 下午7:21:42
	 * @description: maxSpeed car problem???????????????????
	 * @param car
	 * @return: void
	 */
	public void putback(Car car) {
		outRoadCars.add(car);
		runCars.remove(car);
		if (car.getMaxSpeed() > maxSpeed)
			maxSpeed = car.getMaxSpeed();
	}

	public boolean checkIfFinished() {
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
