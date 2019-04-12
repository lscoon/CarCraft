package com.huawei.entity;

import java.util.List;

import org.apache.log4j.Logger;

import com.huawei.entity.Car.Direction;
import com.huawei.util.MapUtil;

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
		origin = MapUtil.crosses.get(Integer.valueOf(strs[4].trim()).intValue());
		destination = MapUtil.crosses.get(Integer.valueOf(strs[5].trim()).intValue());
		if(Integer.valueOf(strs[6].trim()).intValue()==1)
			isBiDirect = true;
		else isBiDirect = false;
		
		int length = Integer.valueOf(strs[1].trim()).intValue();
		int lanesNum = Integer.valueOf(strs[3].trim()).intValue();
		forwardRoad = new OneWayRoad(lanesNum, length, this);
		if(isBiDirect)
			backwardRoad = new OneWayRoad(lanesNum, length, this);
	}
	
	public String info() {
		String info = "\n";
		info = info.concat(roadId + "\n");
		info = info.concat(limitSpeed + "\n");
		info = info.concat(origin.getCrossId() + "\n");
		info = info.concat(destination.getCrossId() + "\n");
		info = info.concat(forwardRoad.getLanesNum() + "\n");
		info = info.concat(forwardRoad.getLength() + "\n");
		info = info.concat(forwardRoad.getFirstCarDirection() + "\n");
		if(isBiDirect)
			info = info.concat(backwardRoad.getFirstCarDirection() + "\n");
		else info = info.concat("null\n");
		return info;
	}
	
	public String showStatus() {
		String temp = "";
		if(isBiDirect) {
			temp = temp.concat(backwardRoad.showBackwardStatus());
		}
		temp = temp.concat("---------------------------------------------------\n");
		temp = temp.concat(forwardRoad.showForwardStatus());
		return temp;
	}
	
	public int getAnOtherCross(int crossId) {
		if(crossId == origin.getCrossId())
			return destination.getCrossId();
		else return origin.getCrossId();
	}
	
	public Cross getAnOtherCross(Cross cross) {
		if(origin.getCrossId() == cross.getCrossId())
			return destination;
		else return origin;
	}
	
	public void updateRoadDirections(Cross cross) {
		if(cross.getCrossId() == destination.getCrossId())
			forwardRoad.updateRoadDirection();
		else if(isBiDirect)
			backwardRoad.updateRoadDirection();
		else logger.error("step2: update invalid road directions " + roadId);
	}
	
	public void newUpdateRoadDirections(Cross cross) {
		if(cross.getCrossId() == destination.getCrossId())
			forwardRoad.newUpdateRoadDirection(cross);
		else if(isBiDirect)
			backwardRoad.newUpdateRoadDirection(cross);
		else logger.error("step2: update invalid road directions " + roadId);
	}
	
	public Direction getFirstCarDirection(Cross cross) {
		if(cross.getCrossId() == destination.getCrossId())
			return forwardRoad.getFirstCarDirection();
		else if(isBiDirect)
			return backwardRoad.getFirstCarDirection();
		else 
			logger.error("step2: update invalid road directions " + roadId);
		return Direction.direct;
	}
	
	public Direction[] getNeighbourDirections(Cross cross) {
		Direction[] directions = new Direction[3];
		int index = cross.getRoads().indexOf(this);
		for(int i=1; i<4; i++) {
			Road road = cross.getRoads().get((index+i)%4);
			if(road!=null) {
				if(road.getOrigin().getCrossId()==cross.getCrossId() 
						&& !road.isBiDirect)
					directions[i-1] = Direction.unknown;
				else directions[i-1] = road.getFirstCarDirection(cross);
			}
			else directions[i-1] = Direction.unknown;
		}
		return directions;
	}
	
	public Road[] getNeighbourRoads(Cross cross) {
		Road[] roads = new Road[3];
		int index = cross.getRoads().indexOf(this);
		for(int i=1; i<4; i++) {
			Road road = cross.getRoads().get((index+i)%4);
			roads[i-1]=road;
		}
		return roads;
	}
	
	public boolean getForwardOrBackward(Road roadBefore) {
		for(Road r: origin.getRoads())
			if(r != null)
				if(roadBefore.getRoadId() == r.getRoadId())
					return true;
		return false;
	}
	
	private boolean getForwardOrBackward(int crossId) {
		if(crossId == origin.getCrossId())
			return true;
		else return false;
	}
	
	public Road findNextWaitLinkRoad(Road roadBefore) {
		for(Road r: origin.getRoads())
			if(r != null)
				if(roadBefore.getRoadId() == r.getRoadId())
					return forwardRoad.findNextWaitLinkRoad(getNeighbourRoads(destination));
		return backwardRoad.findNextWaitLinkRoad(getNeighbourRoads(origin));
		
	}
	
	private boolean isSameDirection(CarFlow carFlow1, CarFlow carFlow2, Road roadBefore1, Road roadBefore2) {
		Boolean forOrBack1,forOrBack2;
		if(roadBefore1 == null)
			forOrBack1 = getForwardOrBackward(carFlow1.getOrigin());
		else forOrBack1 = getForwardOrBackward(roadBefore1);
		if(roadBefore2 == null)
			forOrBack2 = getForwardOrBackward(carFlow2.getOrigin());
		else forOrBack2 = getForwardOrBackward(roadBefore2);
		
		if(forOrBack1 == forOrBack2)
			return true;
		else return false;
	}
	
	public boolean isOverlay(CarFlow carFlow1, CarFlow carFlow2) {
		List<Road> roadList1= carFlow1.getRoadList();
		List<Road> roadList2= carFlow2.getRoadList();
		if(!roadList2.contains(this)) //|| (roadList2.indexOf(this)==(roadList2.size()-1)))
			return false;
		
		int index1 = roadList1.indexOf(this);
		int index2 = roadList2.indexOf(this);
		Road roadBefore1 = null;
		Road roadBefore2 = null;
		if(index1 != 0)
			roadBefore1 = roadList1.get(index1-1);
		if(index2 != 0)
			roadBefore2 = roadList2.get(index2-1);
		
		if(!isSameDirection(carFlow1, carFlow2, roadBefore1, roadBefore2))
			return false;
		
		return true;
	}
	
	// 0, 1... in road lan num
	// -1, waited, means road has no enough space or ahead car is not updated
	// -2, error, invalid
	public int getInRoadLaneNum(Cross cross, int nextDistance) {
		if(cross.getCrossId() == origin.getCrossId())
			return forwardRoad.getInRoadLaneNum(nextDistance);
		else if(isBiDirect)
			return backwardRoad.getInRoadLaneNum(nextDistance);
		else 
			logger.error("step2: get invalid road lane nums " + roadId);
		return -2;
	}
	
	public int getBlankNum(int crossId, int speed) {
		speed = Math.min(speed, limitSpeed);
		if(origin.getCrossId() == crossId)
			return forwardRoad.getBlankNum(speed);
		else return backwardRoad.getBlankNum(speed);
	}
	
	public int getRoadId() {
		return roadId;
	}
	
	public int getLimitSpeed() {
		return limitSpeed;
	}

	public Cross getOrigin() {
		return origin;
	}

	public Cross getDestination() {
		return destination;
	}
	
	public boolean isBiDirect() {
		return isBiDirect;
	}
	
	public OneWayRoad getForwardRoad() {
		return forwardRoad;
	}

	public OneWayRoad getBackwardRoad() {
		return backwardRoad;
	}

	public int getLength() {
		return forwardRoad.getLength();
	}

	public int getLanesNum() {
		return forwardRoad.getLanesNum();
	}
	
	public int getCarNum() {
		if(isBiDirect)
			return backwardRoad.getCarNum() + forwardRoad.getCarNum();
		return forwardRoad.getCarNum();
	}
	
	public void changeLoad(Road roadBefore, int change) {
		if(getForwardOrBackward(roadBefore))
			forwardRoad.changeLoad(change);
		else backwardRoad.changeLoad(change);
	}
	
	public void changeLoad(int crossId, int change) {
		if(getForwardOrBackward(crossId))
			forwardRoad.changeLoad(change);
		else backwardRoad.changeLoad(change);;
	}
	
	public float getLoad(Road roadBefore) {
		if(getForwardOrBackward(roadBefore))
			return forwardRoad.getLoad();
		else return backwardRoad.getLoad();
	}
	
	public int getLoad(int crrossId) {
		if(getForwardOrBackward(crrossId))
			return forwardRoad.getLoad();
		else return backwardRoad.getLoad();
	}
	
	public boolean containsCarFlow(Road roadBefore, CarFlow carflow) {
		if(getForwardOrBackward(roadBefore))
			return forwardRoad.containsCarFlow(carflow);
		else return backwardRoad.containsCarFlow(carflow);
	}
	
	public boolean containsCarFlow(int crossId, CarFlow carflow) {
		if(getForwardOrBackward(crossId))
			return forwardRoad.containsCarFlow(carflow);
		else return backwardRoad.containsCarFlow(carflow);
	}
	
	
}
