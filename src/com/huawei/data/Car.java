package com.huawei.data;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;

import com.huawei.util.Util;

public class Car {
	
	private static final Logger logger = Logger.getLogger(Car.class);
	// left right direct no
	public static enum Direction {l,r,d,n};
	
	private int carId;
	private int origin;
	private int destination;
	private int maxSpeed;
	private int startTime;
	
	private int realStTime = 0;
	private int realEndTime = 0;
	
	private List<Road> runRoadList = new ArrayList<Road>();
	
	private Road nowRoad = null;
	private Road nextRoad = null;
	private Direction direction = Direction.n;
	private int nowDistance = 0;
	private int nextDistance = 0;
	private boolean isRunning = false;
	private boolean isUpdated = true;
	
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
	
	protected void startOff() {
		nextRoad = runRoadList.get(0);
		nextDistance = Math.min(maxSpeed, nextRoad.getLimitSpeed());
		if(updateCarWhileInNextRoad(RoadMap.crosses.get(origin))) {
			logger.info("car " + carId + " start off in " + RoadMap.nowTime);
			isRunning = true;
			RoadMap.outRoadCars.remove((Integer)carId);
			RoadMap.nowRunCars.add(carId);
		}
		
	}
	
	protected void arrive() {
		realEndTime = RoadMap.nowTime+1;
		nowRoad = null;
		nextRoad = null;
		isRunning = false;
		isUpdated = true;
		RoadMap.nowRunCars.remove(carId);
		RoadMap.finishCars.add(carId);
		RoadMap.nowWaitedCars.remove(carId);
	}
	
	protected void stepForward() {
		isUpdated = true;
		RoadMap.nowWaitedCars.remove(carId);
	}
	
	protected void computeNowAndNextDistance(int s1) {
		int nowSpeed = Math.min(maxSpeed, nowRoad.getLimitSpeed());
		if(nowSpeed < s1) {
			nowDistance = nowSpeed;
			nextDistance = 0;
		} else if(nextRoad==null) {
			// will arrive destination
			nowDistance = s1;
			nextDistance = 0;
		} else {
			nowDistance = s1;
			int nextSpeed = Math.min(maxSpeed, nextRoad.getLimitSpeed());
			if(s1 >= nextSpeed)
				nextDistance = 0;
			else nextDistance = nextSpeed-s1;
		}
	}
	
	private void stepNextRoad() {
		nowRoad = nextRoad;
		int index = runRoadList.indexOf(nowRoad);
		if(index == runRoadList.size()-1)
			nextRoad = null;
		else nextRoad = runRoadList.get(index+1);
		isUpdated = true;
		RoadMap.nowWaitedCars.remove(carId);
	}
	
	// from nowRoad to nextRoad
	protected boolean updateCarWhileInNextRoad(Cross cross) {
		if(nextRoad==null)
			logger.error("next road " + nextRoad.getRoadId() + " not exists");
		int laneNum = nextRoad.getInRoadLaneNum(cross, nextDistance);
		if(laneNum < 0)
			return false;
		else {
			nextRoad.updateRoadWhilecarInNextRoad(cross, this, laneNum);
			stepNextRoad();
			return true;
		}
	}
	
	public int getCarId() {
		return carId;
	}
	
	public int getOrigin() {
		return origin;
	}

	public int getDestination() {
		return destination;
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

	public List<Road> getRunRoadList() {
		return runRoadList;
	}

	public void setRunRoadList(List<Road> runRoadList) {
		this.runRoadList = runRoadList;
	}

	public Road getNowRoad() {
		return nowRoad;
	}

	public void setNowRoad(Road nowRoad) {
		this.nowRoad = nowRoad;
	}
	
	public Road getNextRoad() {
		return nextRoad;
	}

	public void setNextRoad(Road nextRoad) {
		this.nextRoad = nextRoad;
	}

	public Direction getDirection() {
		return direction;
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
	
}
