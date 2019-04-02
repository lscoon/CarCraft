package com.huawei.entity;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.huawei.util.MapUtil;

public class Car {
	
	private static final Logger logger = Logger.getLogger(Car.class);
	// left right direct no
	public static enum Direction {right,left,direct,unknown};
	
	private int carId;
	private int origin;
	private int destination;
	private int maxSpeed;
	private int startTime;
	
	private int realStartTime = 0;
	private int realEndTime = 0;
	
	private CarFlow carFlow = null;
	private List<Road> roadList = new ArrayList<Road>();
	
	private Road nowRoad = null;
	private Road nextRoad = null;
	private Direction direction = Direction.unknown;
	private int nowDistance = 0;
	private int nextDistance = 0;
	private boolean isRunning = false;
	private boolean isWaited = false;
	
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
		realStartTime = startTime;
		
		if(carId/MapUtil.CarIdMaxLength > 0)
			MapUtil.CarIdMaxLength = MapUtil.CarIdMaxLength*10;
		if(maxSpeed>MapUtil.CarMaxSpeed)
			MapUtil.CarMaxSpeed = maxSpeed;
		if(maxSpeed<MapUtil.CarMinSpeed)
			MapUtil.CarMinSpeed = maxSpeed;
	}

	public String info() {
		String info = "\n";
		info = info.concat(carId + "\n");
		info = info.concat(origin + "\n");
		info = info.concat(destination + "\n");
		info = info.concat(maxSpeed + "\n");
		info = info.concat(startTime + "\n");
		info = info.concat(realStartTime + "\n");
		if(nowRoad!=null)
			info = info.concat(nowRoad.getRoadId() + "\n");
		else info = info.concat("n\n");
		if(nextRoad!=null)
			info = info.concat(nextRoad.getRoadId() + "\n");
		else info = info.concat("n\n");
		info.concat(direction + "\n");
		
		return info;
	}
	
	public void computeDirection() {
		if(nextRoad == null) {
			direction =  Direction.direct;
			return;
		}
		Cross cross = null;
		if(nowRoad.getOrigin().getRoads().contains(nextRoad))
			cross = nowRoad.getOrigin();
		else cross = nowRoad.getDestination();
		switch((cross.getRoads().indexOf(nowRoad)-
				cross.getRoads().indexOf(nextRoad)+4)%4) {
			case 1: direction = Direction.right;return;
			case 2: direction = Direction.direct;return;
			case 3: direction = Direction.left;return;
			default: logger.error("something error while computing directions");
		}
		direction =  Direction.unknown;
	}
	
	// -1 means destination
	// -2 means could not arrive because of speed limit speed
	public void computeNowAndNextDistance(int s1) {
		int nowSpeed = Math.min(maxSpeed, nowRoad.getLimitSpeed());
		if(nowSpeed <= s1) {
			nowDistance = nowSpeed;
			nextDistance = 0;
		} else if(nextRoad==null) {
			// will arrive destination
			nowDistance = s1;
			nextDistance = -1;
		} else {
			nowDistance = s1;
			int nextSpeed = Math.min(maxSpeed, nextRoad.getLimitSpeed());
			if(s1 >= nextSpeed)
				nextDistance = -2;
			else nextDistance = nextSpeed-s1;
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

	public int getMaxSpeed() {
		return maxSpeed;
	}
	
	public int getStartTime() {
		return startTime;
	}

	public int getRealStartTime() {
		return realStartTime;
	}

	public void setRealStartTime(int realStTime) {
		this.realStartTime = realStTime;
	}
	
	public int getRealEndTime() {
		return realEndTime;
	}

	public void setRealEndTime(int realEndTime) {
		this.realEndTime = realEndTime;
	}

	public List<Road> getRoadList() {
		return roadList;
	}

	public void setRoadList(List<Road> runRoadList) {
		this.roadList = runRoadList;
		this.nextRoad = roadList.get(0);
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

	public boolean isWaited() {
		return isWaited;
	}

	public void setWaited(boolean isWaited) {
		this.isWaited = isWaited;
	}
	
	public boolean isRunning() {
		return isRunning;
	}

	public void setRunning(boolean isRunning) {
		this.isRunning = isRunning;
	}

	public CarFlow getCarFlow() {
		return carFlow;
	}

	public void setCarFlow(CarFlow carFlow) {
		this.carFlow = carFlow;
	}
	
}
