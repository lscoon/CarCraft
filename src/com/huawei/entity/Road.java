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
	
	public int updateRunnableCars() {
		int count = 0;
		count = forwardRoad.updateRunnableCars();
		if(isBiDirect)
			count += backwardRoad.updateRunnableCars();
		//logger.info("step1: road " + roadId + ", " + count + " cars updated");
		return count;
	}
	
	public int updateWaitedCars(Cross cross) {
		if(cross.getCrossId() == destination.getCrossId())
			return forwardRoad.updateWaitedCars(cross, getNeighbourDirections(cross));
		else if(isBiDirect)
			return backwardRoad.updateWaitedCars(cross,getNeighbourDirections(cross));
		logger.error("step2: update invalid road cars " + roadId);
		return -1;
	}
	
	public void updateRoadDirections(Cross cross) {
		if(cross.getCrossId() == destination.getCrossId())
			forwardRoad.updateRoadDirection();
		else if(isBiDirect)
			backwardRoad.updateRoadDirection();
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
	
	private Direction[] getNeighbourDirections(Cross cross) {
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
	
//	private Direction computeDirection(Road roadBefore) {
//		if(roadBefore == null)
//			return Direction.unknown;
//		Cross cross = null;
//		if(origin.getRoads().contains(roadBefore))
//			cross = origin;
//		else cross = destination;
//		switch((cross.getRoads().indexOf(roadBefore)-
//				cross.getRoads().indexOf(this)+4)%4) {
//			case 1: return Direction.right;
//			case 2: return Direction.direct;
//			case 3: return Direction.left;
//			default: logger.error("something error while computing directions");
//		}
//		return Direction.unknown;
//	}
	
//	private int compareDirection(Direction direct1, Direction direct2) {
//		if(direct1 == Direction.unknown)
//			return 1;
//		else if (direct1 == Direction.direct) {
//			if(direct2 == Direction.unknown)
//				return -1;
//		}
//		else if (direct1 == Direction.left) {
//			if(direct2 == Direction.right)
//				return 1;
//		}
//		return -1;
//	}
	
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
	
//	public int computePriority(CarFlow carFlow1, CarFlow carFlow2) {
//		List<Road> roadList1= carFlow1.getRoadList();
//		List<Road> roadList2= carFlow2.getRoadList();
//		if(!roadList2.contains(this)) //|| (roadList2.indexOf(this)==(roadList2.size()-1)))
//			return 0;
//		int index1 = roadList1.indexOf(this);
//		int index2 = roadList2.indexOf(this);
//		Road roadBefore1 = null;
//		Road roadBefore2 = null;
//		if(index1 != 0)
//			roadBefore1 = roadList1.get(index1-1);
//		if(index2 != 0)
//			roadBefore2 = roadList2.get(index2-1);
//		if(!isSameDirection(carFlow1, carFlow2, roadBefore1, roadBefore2))
//			return 0;
//		Direction direct1 = computeDirection(roadBefore1);
//		Direction direct2 = computeDirection(roadBefore2);
//		if(direct1==direct2)
//			return 0;
//		return compareDirection(direct1, direct2);
//	}
	
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
	protected int getInRoadLaneNum(Cross cross, int nextDistance) {
		if(cross.getCrossId() == origin.getCrossId())
			return forwardRoad.getInRoadLaneNum(nextDistance);
		else if(isBiDirect)
			return backwardRoad.getInRoadLaneNum(nextDistance);
		else 
			logger.error("step2: get invalid road lane nums " + roadId);
		return -2;
	}
	
	// num means car in road lane num
	protected void updateRoadWhilePassCross(Cross cross, Car car, int num) {
		if(cross.getCrossId() == origin.getCrossId())
			forwardRoad.updateRoadWhilePassCross(car, num);
		else if(isBiDirect)
			backwardRoad.updateRoadWhilePassCross(car, num);
		else
			logger.error("step2: invalid car " + car.getCarId() + " in " + roadId);
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
	
	public int getLength() {
		return forwardRoad.getLength();
	}

	public int getCarNum() {
		if(isBiDirect)
			return backwardRoad.getCarNum() + forwardRoad.getCarNum();
		return forwardRoad.getCarNum();
	}
	
	public void setOccupied(Road roadBefore, int occupy) {
		if(getForwardOrBackward(roadBefore))
			forwardRoad.setOccupied(occupy);
		else backwardRoad.setOccupied(occupy);
	}
	
	public void setOccupied(int crossId, int occupy) {
		if(getForwardOrBackward(crossId))
			forwardRoad.setOccupied(occupy);
		else backwardRoad.setOccupied(occupy);
	}
	
	public boolean isOccupied(int crrossId) {
		int occupy = 0;
		if(getForwardOrBackward(crrossId))
			occupy =  forwardRoad.isOccupied();
		else occupy =  backwardRoad.isOccupied();
		if(occupy == 0)
			return true;
		else return false;
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
