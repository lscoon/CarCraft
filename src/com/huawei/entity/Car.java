package com.huawei.entity;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.huawei.service.MapSimulator;
import com.huawei.util.MapUtil;

public class Car {
	
	private static final Logger logger = Logger.getLogger(Car.class);
	// left right direct no
	public static enum Direction {left,right,direct,unknown};
	
	private int carId;
	private int origin;
	private int destination;
	private int maxSpeed;
	private int startTime;
	
	private int realStartTime = 0;
	private int realEndTime = 0;
	
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
		
		return info;
	}
	
	public boolean startOff() {
		nextDistance = Math.min(maxSpeed, nextRoad.getLimitSpeed());
		if(updateCarWhilePassCross(MapUtil.crosses.get(origin))>=0) {
			//logger.info("car " + carId + " start off in Time " + MapSimulator.term);
			isRunning = true;
			realStartTime = MapSimulator.term;
			MapSimulator.outRoadCars.remove(this);
			MapSimulator.nowRunCars.add(this);
			return true;
		}
		realStartTime++;
		return false;
	}
	
	protected void arrive() {
		realEndTime = MapSimulator.term+1;
		nowRoad = null;
		nextRoad = null;
		isRunning = false;
		isWaited = false;
		MapSimulator.nowRunCars.remove(this);
		MapSimulator.finishCars.add(this);
		MapSimulator.nowWaitedCars.remove(this);
		MapSimulator.stepFinishCount++;
	}
	
	protected void stepForward() {
		isWaited = false;
		MapSimulator.nowWaitedCars.remove(this);
	}
	
	private void stepPassCross() {
		nowRoad = nextRoad;
		int index = roadList.indexOf(nowRoad);
		if(index == roadList.size()-1)
			nextRoad = null;
		else nextRoad = roadList.get(index+1);
		direction = computeDirection();
		isWaited = false;
		MapSimulator.nowWaitedCars.remove(this);
	}
	
	private Direction computeDirection() {
		if(nextRoad == null)
			return Direction.direct;
		Cross cross = null;
		if(nowRoad.getOrigin().getRoads().contains(nextRoad))
			cross = nowRoad.getOrigin();
		else cross = nowRoad.getDestination();
		switch((cross.getRoads().indexOf(nowRoad)-
				cross.getRoads().indexOf(nextRoad)+4)%4) {
			case 1: return Direction.right;
			case 2: return Direction.direct;
			case 3: return Direction.left;
			default: logger.error("something error while computing directions");
		}
		return Direction.unknown;
	}
	
	protected void computeNowAndNextDistance(int s1) {
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
				nextDistance = 0;
			else nextDistance = nextSpeed-s1;
		}
	}
	
	// from nowRoad to nextRoad
	protected int updateCarWhilePassCross(Cross cross) {
		if(nextRoad==null)
			logger.error("next road " + nextRoad.getRoadId() + " not exists");
		int laneNum = nextRoad.getInRoadLaneNum(cross, nextDistance);
		
		// could pass the cross
		if(laneNum >= 0){
			nextRoad.updateRoadWhilePassCross(cross, this, laneNum);
			stepPassCross();
		}
		// will not pass the cross because no enough space
		else if(laneNum == -2)
			stepForward();
		
		return laneNum;
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
	
}
