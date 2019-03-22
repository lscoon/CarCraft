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

	public boolean startoff() {
		if (MapSimulator.term < minTerm)
			return false;
		// compute if conflict or not
		// if true
		isRunning = true;
		return true;

		// if false
		// return false;
	}

	public ArrayList<Car> getNowStartOffCars() {
		ArrayList<Car> startOffCarList = new ArrayList<>();
		int blankNum = roadList.get(0).getBlankNum(origin, maxSpeed);

		// select cars

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
		if (runCars.size() == 0) {
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

}
