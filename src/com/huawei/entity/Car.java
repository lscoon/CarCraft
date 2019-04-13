package com.huawei.entity;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import com.huawei.sa.BPRLinkPerformance;
import com.huawei.util.MapUtil;

public class Car {
	
	private static final Logger logger = Logger.getLogger(Car.class);
	// left right direct no
	public static enum Direction {right,left,direct,unknown,priRight,priLeft,priDirect};
	
	private int carId;
	private int origin;
	private int destination;
	private int maxSpeed;
	private int startTime;
	private boolean isPriority = false;
	private boolean isPreset = false;
//	public boolean tempPreset = false;
	
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
		if (strs.length != 7) {
			logger.error("car create format error: " + strs);
			return;
		}
		carId = Integer.valueOf(strs[0].trim()).intValue();
		origin = Integer.valueOf(strs[1].trim()).intValue();
		destination = Integer.valueOf(strs[2].trim()).intValue();
		maxSpeed = Integer.valueOf(strs[3].trim()).intValue();
		startTime = Integer.valueOf(strs[4].trim()).intValue();
		if(Integer.valueOf(strs[5].trim()).intValue()==1)
			isPriority = true;
		else isPriority = false;
		if(Integer.valueOf(strs[6].trim()).intValue()==1)
			isPreset = true;
		else isPreset = false;
			
		realStartTime = startTime;
		
		if(carId/MapUtil.CarIdMaxLength > 0)
			MapUtil.CarIdMaxLength = MapUtil.CarIdMaxLength*10;
		
		MapUtil.AllCarNum++;
		if(maxSpeed > MapUtil.AllCarMaxSpeed)
			MapUtil.AllCarMaxSpeed = maxSpeed;
		if(maxSpeed < MapUtil.AllCarMinSpeed)
			MapUtil.AllCarMinSpeed = maxSpeed;
		if(startTime < MapUtil.AllCarEarliestStartTime)
			MapUtil.AllCarEarliestStartTime = startTime;
		if(startTime > MapUtil.AllCarLatestStartTime)
			MapUtil.AllCarLatestStartTime = startTime;
		
		if(isPriority) {
			MapUtil.PriorityCarNum++;
			if(maxSpeed > MapUtil.PriorityCarMaxSpeed)
				MapUtil.PriorityCarMaxSpeed = maxSpeed;
			if(maxSpeed < MapUtil.PriorityCarMinSpeed)
				MapUtil.PriorityCarMinSpeed = maxSpeed;
			if(startTime < MapUtil.PriorityCarEarliestStartTime)
				MapUtil.PriorityCarEarliestStartTime = startTime;
			if(startTime > MapUtil.PriorityCarLatestStartTime)
				MapUtil.PriorityCarLatestStartTime = startTime;
		}
	}

	public void preset(String[] strs) {
		startTime = Integer.valueOf(strs[1].trim()).intValue();
		realStartTime = startTime;
		for(int i=2; i<strs.length; i++) {
			Road road = MapUtil.roads.get(Integer.valueOf(strs[i].trim()).intValue());
			roadList.add(road);
		}
		nextRoad = roadList.get(0);
	}
	
	public String info() {
		String info = "\n";
		info = info.concat(carId + "\n");
		info = info.concat(origin + "\n");
		info = info.concat(destination + "\n");
		info = info.concat(maxSpeed + "\n");
		info = info.concat(startTime + "\n");
		info = info.concat(realStartTime + "\n");
		info = info.concat(isPreset + "\n");
		info = info.concat(isPriority + "\n");
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
			case 1: 
				if(isPriority)
					direction = Direction.priRight;
				else direction = Direction.right;
				return;
			case 2: 
				if(isPriority)
					direction = Direction.priDirect;
				else direction = Direction.direct;
				return;
			case 3: 
				if(isPriority)
					direction = Direction.priLeft;
				else direction = Direction.left;
				return;
			default: 
				{
					Car car = null;
					car.getCarId();
					logger.error("something error while computing directions");
				}
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
	
	public void computeNowDistance(int s1) {
		int nowSpeed = Math.min(maxSpeed, nowRoad.getLimitSpeed());
		if(nowSpeed <= s1) {
			// will not try
			nowDistance = nowSpeed;
			nextDistance = 0;
		}
		else{
			// will try to pass the cross
			nowDistance = s1;
			nextDistance = 1;
		}
	}
	
	public void computeNextDistance(Road road) {
		if(!isRunning) {
			nowDistance = 0;
			nextDistance = 1;
		}
		if(nextDistance <= 0)
			logger.error("logic error in com next distance");
		int nextSpeed = Math.min(maxSpeed, road.getLimitSpeed());
		if(nowDistance >= nextSpeed)
			nextDistance = -2;
		else nextDistance = nextSpeed-nowDistance;
	}
	
	public Road findNextRoad(Cross cross) {
		if(nextRoad != null) {
			if(nowRoad != null) {
				if(checkWaitLink())
					logger.info(carId + ":preset dead lock");
				computeDirection();
			}
			return nextRoad;
		}
		else if(cross.getCrossId()==destination) {
			computeDirection();
			return null;
		}
		else {
			int crossIndex = MapUtil.crossSequence.indexOf(cross.getCrossId());
			float[] linkResistances = {MapUtil.FloatMax,MapUtil.FloatMax,MapUtil.FloatMax,MapUtil.FloatMax};
			int[] flags = {MapUtil.IntMax,MapUtil.IntMax,MapUtil.IntMax,MapUtil.IntMax};
			int i=0;
			for(; i<4; i++) {
				if(cross.getSequenceRoadIds()[i]==-1) {
					break;
				}
				Road road = cross.getRoads().get(cross.getSequenceRoadIds()[i]);
				if(nowRoad!=null && nowRoad.getRoadId()==road.getRoadId()) {
					continue;
				}
				if(road.getDestination().getCrossId()==cross.getCrossId() && !road.isBiDirect()) {
					continue;
				}
				
				computeNextDistance(road);
				int flag = road.getInRoadLaneNum(cross, nextDistance);
				nextDistance = 1;
				
				flags[i] = flag; 
				// -2 不能上 -1 等待 >=0 能上
				float tempLR = BPRLinkPerformance.getLinkResistance(this, road, crossIndex);
				linkResistances[i] = tempLR; 
			}
			
			while(nextRoad == null) {
				float minLR = MapUtil.FloatMax;
				int minIndex = -1;
				for(int j=0; j<i; j++) {
					if(linkResistances[j]<minLR) {
						minLR = linkResistances[j];
						minIndex = j;
					}
				}
				
				if(minIndex==-1) {
					if(nowRoad==null)
						break;
					logger.info(carId + ":must choose a dead lock");
					for(int j=0; j<i; j++)
						if(flags[j]==-1) {
							nextRoad = cross.getRoads().get(cross.getSequenceRoadIds()[j]);
							break;
						}
					if(nextRoad==null)
						logger.info(carId + ":still have no way to go");
					else break;
				}
				
				if(flags[minIndex]>=0) {
					nextRoad = cross.getRoads().get(cross.getSequenceRoadIds()[minIndex]);
					break;
				}
				else if(flags[minIndex]==-1) {
					if(nowRoad==null) {
						linkResistances[minIndex] = MapUtil.FloatMax;
						nextRoad=null;
						continue;
					}
					else {
//						nextRoad = cross.getRoads().get(cross.getSequenceRoadIds()[minIndex]);
//						if(!checkWaitLink())
//							break;
//						else {
							linkResistances[minIndex] = MapUtil.FloatMax;
							nextRoad=null;
							continue;
//						}
					}
				}
				else if(flags[minIndex]==-2) {
					if(nowRoad==null){
						linkResistances[minIndex] = MapUtil.FloatMax;
						nextRoad=null;
						continue;
					}
					else {
						nextRoad = cross.getRoads().get(cross.getSequenceRoadIds()[minIndex]);
						if(!checkWaitLink())
							break;
						else {
							linkResistances[minIndex] = MapUtil.FloatMax;
							nextRoad=null;
							continue;
						}
					}
				}
			}
		}
		
		if(nowRoad!=null) {
			computeNextDistance(nextRoad);
			computeDirection();
		}
		return nextRoad;
	}
	
	public boolean checkWaitLink() {
		Road road1 = nowRoad;
		Road road2 = nextRoad;
		Set<Road> visitedRoads = new HashSet<>();
		while(road2!=null) {
			visitedRoads.add(road1);
			if(visitedRoads.contains(road2))
				return true;
			Road temp = road2.findNextWaitLinkRoad(road1);
			road1 = road2;
			road2 = temp;
		}
		return false;
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
	
	public void setStartTime(int startTime) {
		this.startTime = startTime;
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

	public void setDirection(Direction d) {
		direction = d;
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

	public boolean isPriority() {
		return isPriority;
	}

	public boolean isPreset() {
		return isPreset;
	}
	
	public void setPreset(Boolean bool) {
		isPreset = bool;
	}
	
}
