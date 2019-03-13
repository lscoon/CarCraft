package com.huawei.data;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;

import com.huawei.util.Util;

public class Car {
	
	private static final Logger logger = Logger.getLogger(Car.class);
	
	private int carId;
	private int origin;
	private int destination;
	private int maxSpeed;
	private int startTime;
	
	private int realStTime = 0;
	private int realEndTime = 0;
	
	private List<Integer> runRoadList = new ArrayList<Integer>();
	
	private int nowRoad = 0;
	private int nextRoad = 0;
	private int nowDistance = 0;
	private int nextDistance = 0;
	private boolean isRunning = false;
	private boolean isUpdated = false;
	
	public Car (String[] strs) {
		if (strs.length != 5) {
			logger.error("car create format error: " + strs);
			return;
		}
		carId = Integer.valueOf(strs[0].trim()).intValue();
		origin = Integer.valueOf(strs[1].trim()).intValue();
		destination = Integer.valueOf(strs[2].trim()).intValue();
		maxSpeed = Integer.valueOf(strs[3].trim()).intValue();
		startTime = Integer.valueOf(strs[4].trim()).intValue();
		
		if(carId/Util.CarIdMaxLength > 0)
			Util.CarIdMaxLength = Util.CarIdMaxLength*10;
	}
	
	public int getOrigin() {
		return origin;
	}

	public int getDestination() {
		return destination;
	}

	public int getMaxSpeed() {
		return maxSpeed;
	}

	public int getStartTime() {
		return startTime;
	}

	public int getRealStTime() {
		return realStTime;
	}

	public void setRealStTime(int realStTime) {
		this.realStTime = realStTime;
	}
	
	public int getRealEndTime() {
		return realEndTime;
	}

	public void setRealEndTime(int realEndTime) {
		this.realEndTime = realEndTime;
	}

	public List<Integer> getRunRoadList() {
		return runRoadList;
	}

	public void setRunRoadList(List<Integer> runRoadList) {
		this.runRoadList = runRoadList;
	}

	public int getNowRoad() {
		return nowRoad;
	}

	public void setNowRoad(int nowRoad) {
		this.nowRoad = nowRoad;
	}
	
	public int getNextRoad() {
		return nextRoad;
	}

	public void setNextRoad(int nextRoad) {
		this.nextRoad = nextRoad;
	}

	public int getNowDistance() {
		return nowDistance;
	}

	public void setNowDistance(int nowDistance) {
		this.nowDistance = nowDistance;
	}

	public int getNextDistance() {
		return nextDistance;
	}

	public void setNextDistance(int nextDistance) {
		this.nextDistance = nextDistance;
	}

	public boolean isUpdated() {
		return isUpdated;
	}

	public void setUpdated(boolean isUpdated) {
		this.isUpdated = isUpdated;
	}
	
	public boolean isRunning() {
		return isRunning;
	}

	public void setRunning(boolean isRunning) {
		this.isRunning = isRunning;
	}

	public String info() {
		String info = "\n";
		info = info.concat(carId + "\n");
		info = info.concat(origin + "\n");
		info = info.concat(destination + "\n");
		info = info.concat(maxSpeed + "\n");
		info = info.concat(startTime + "\n");
		info = info.concat(realStTime + "\n");
		info = info.concat(nowRoad + "\n");
		
		return info;
	}
	
	public void computeNowAndNextDistance() {
		//RoadMap.roads.get
	}
	
	
}
