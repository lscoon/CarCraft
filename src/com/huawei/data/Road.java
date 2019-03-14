package com.huawei.data;

import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;

import com.huawei.data.Car.Direction;
import com.huawei.util.Util;

public class Road {

	private static final Logger logger = Logger.getLogger(Road.class);
	
	private int roadId;
	private int limitSpeed;
	private Cross origin;
	private Cross destination;
	
	private boolean isBiDirect;
	private OneWayRoad forwardRoad;
	private OneWayRoad backwardRoad;
	
	public Road (String[] strs) {
		if (strs.length != 7) {
			logger.error("road create format error: " + strs);
			return;
		}
		roadId = Integer.valueOf(strs[0].trim()).intValue();
		limitSpeed = Integer.valueOf(strs[2].trim()).intValue();
		origin = RoadMap.crosses.get(Integer.valueOf(strs[4].trim()).intValue());
		destination = RoadMap.crosses.get(Integer.valueOf(strs[5].trim()).intValue());
		if(Integer.valueOf(strs[6].trim()).intValue()==1)
			isBiDirect = true;
		else isBiDirect = false;
		
		int length = Integer.valueOf(strs[1].trim()).intValue();
		int lanesNum = Integer.valueOf(strs[3].trim()).intValue();
		forwardRoad = new OneWayRoad(lanesNum, length);
		if(isBiDirect)
			backwardRoad = new OneWayRoad(lanesNum, length);
	}
	
	public String info() {
		String info = "\n";
		info = info.concat(roadId + "\n");
		info = info.concat(limitSpeed + "\n");
		info = info.concat(origin.getCrossId() + "\n");
		info = info.concat(destination.getCrossId() + "\n");
		return info;
	}
	
	public String showStatus() {
		String temp = "";
		if(isBiDirect) {
			temp = temp.concat(backwardRoad.showBackwardStatus());
			temp = temp.concat("------------------------\n");
		}
		temp = temp.concat(forwardRoad.showForwardStatus());
		return temp;
	}
	
	public int getAnOtherCross(int crossId) {
		if(crossId == origin.getCrossId())
			return destination.getCrossId();
		else return origin.getCrossId();
	}
	
	public void updateRunnableCars() {
		logger.info("step1: update road " + roadId );
		forwardRoad.updateRunnableCars();
		if(isBiDirect)
			backwardRoad.updateRunnableCars();
	}
	
	public int updateWaitedCars(Cross cross) {
		if(cross.getCrossId() == destination.getCrossId())
			return forwardRoad.updateWaitedCars(cross, getNeighbourDirections(cross));
		else if(isBiDirect)
			return backwardRoad.updateWaitedCars(cross,getNeighbourDirections(cross));
		else logger.error("step2: update invalid road cars " + roadId);
		return -1;
	}
	
	public void updateRoadDirections(Cross cross) {
		if(cross.getCrossId() == destination.getCrossId())
			forwardRoad.updateRoadDirection();
		else if(isBiDirect)
			backwardRoad.updateRoadDirection();
		else logger.error("step2: update invalid road directions " + roadId);
	}
	
	public Direction getRoadDirection(Cross cross) {
		if(cross.getCrossId() == destination.getCrossId())
			return forwardRoad.getRoadDirection();
		else if(isBiDirect)
			return backwardRoad.getRoadDirection();
		else 
			logger.error("step2: update invalid road directions " + roadId);
		return Direction.d;
	}
	
	private Direction[] getNeighbourDirections(Cross cross) {
		Direction[] directions = new Direction[3];
		int index = cross.getRoads().indexOf(this);
		for(int i=1; i<4; i++)
			if(cross.getRoads().get((index+i)%4)!=null)
				directions[i-1] = cross.getRoads().get((index+i)%4).getRoadDirection(cross);
			else directions[i-1] = Direction.n;
		return directions;
	}
	
	// 0, 1... in road lan num
	// -1, waited, means road has no enough space or ahead car is not updated
	// -2, error, invalid
	protected int getInRoadLaneNum(Cross cross, int nextDistance) {
		if(cross.getCrossId() == destination.getCrossId())
			return forwardRoad.getInRoadLaneNum(nextDistance);
		else if(isBiDirect)
			return backwardRoad.getInRoadLaneNum(nextDistance);
		else 
			logger.error("step2: get invalid road lane nums " + roadId);
		return -2;
	}
	
	// num means car in road lane num
	protected void updateRoadWhilecarInNextRoad(Cross cross, Car car, int num) {
		if(cross.getCrossId() == destination.getCrossId())
			forwardRoad.updateRoadWhilecarInNextRoad(car, num);
		else if(isBiDirect)
			backwardRoad.updateRoadWhilecarInNextRoad(car, num);
		else
			logger.error("step2: invalid car " + car.getCarId() + " in " + roadId);
	}
	
	public int getRoadId() {
		return roadId;
	}
	
	public int getLimitSpeed() {
		return limitSpeed;
	}
}
