package com.huawei.entity;

import org.apache.log4j.Logger;

import com.huawei.entity.Car.Direction;
import com.huawei.util.MapUtil;

public class OneWayRoad {
	
	private static final Logger logger = Logger.getLogger(OneWayRoad.class);
	
	private int lanesNum;
	private int len;
	private Road road;
	
	private Car[][] status;
	private int[] firstCarLocation;
	private int carNum = 0;
	private Direction firstCarDirection = Direction.unknown;
	
	private int load = 0;
	
	public int inLoad = 0;
	public int outLoad = 0;
	
	public OneWayRoad(int lanesNumber, int length, Road r) {
		lanesNum = lanesNumber;
		len = length;
		road = r;
		status = new Car[lanesNumber][length];
		firstCarLocation = new int[]{0,len-1};
	}
	
	protected String showForwardStatus() {
		int carIdMaxLength = countNum(MapUtil.CarIdMaxLength);
		String temp = "";
		for(int i=0; i<lanesNum; i++) {
			for(int j=0; j<len; j++) {
				if(status[i][j]!=null){
					int blankCount = carIdMaxLength-countNum(status[i][j].getCarId())-1;
					for(int k=0; k<blankCount; k++)
						temp = temp.concat("0");
					temp = temp.concat(status[i][j].getCarId()+directToStr(status[i][j].getDirection()));
				}
				else {
					for(int k=0; k<carIdMaxLength-1; k++)
						temp = temp.concat("0");
				}
				temp = temp.concat(" ");
			}
			temp = temp.concat("\n");
		}
		return temp;
	}
	
	protected String showBackwardStatus() {
		int carIdMaxLength = countNum(MapUtil.CarIdMaxLength);
		String temp = "";
		for(int i=lanesNum-1; i>=0; i--) {
			for(int j=len-1; j>=0; j--) {
				if(status[i][j]!=null){
					int blankCount = carIdMaxLength-countNum(status[i][j].getCarId())-1;
					for(int k=0; k<blankCount; k++)
						temp = temp.concat("0");
					temp = temp.concat(status[i][j].getCarId()+directToStr(status[i][j].getDirection()));
				}
				else {
					for(int k=0; k<carIdMaxLength-1; k++)
						temp = temp.concat("0");
				}
				temp = temp.concat(" ");
			}
			temp = temp.concat("\n");
		}
		return temp;
	}
	
	private String directToStr(Direction direct) {
		switch (direct) {
			case right:return "r";
			case left: return "l";
			case direct: return "d";
			case unknown: return "u";
			default:return "e";
		}
	}
	
	private int countNum(int input) {
		if(input/10==0)
			return 1;
		else 
			return countNum(input/10) + 1;
	}
	
	// pass cross order, means z
	public void updateRoadDirection() {
		if(carNum==0) {
			firstCarLocation[0] = 0;
			firstCarLocation[1] = len-1;
			firstCarDirection = Direction.unknown;
			return;
		}
		
		for(int j=len-1; j>=0; j--)
			for(int i=0; i<lanesNum; i++) {
				if(status[i][j]!=null && status[i][j].isWaited() && status[i][j].isPriority()) {
					int k=j+1;
					for(; k<len; k++)
						if(status[i][k]!=null && status[i][k].isWaited())
							break;
					if(k==len) {
						firstCarDirection = status[i][j].getDirection();
						firstCarLocation[0] = i;
						firstCarLocation[1] = j;
						return;
					}
				}
			}
		
		for(int j=len-1; j>=0; j--)
			for(int i=0; i<lanesNum; i++) {
				if(status[i][j]!=null && status[i][j].isWaited()) {
					firstCarDirection = status[i][j].getDirection();
					firstCarLocation[0] = i;
					firstCarLocation[1] = j;
					return;
				}
			}
		// all cars have been updated
		firstCarLocation[0] = 0;
		firstCarLocation[1] = len-1;
		firstCarDirection = Direction.unknown;
	}
	
	public void newUpdateRoadDirection(Cross cross) {
		if(carNum==0) {
			firstCarLocation[0] = 0;
			firstCarLocation[1] = len-1;
			firstCarDirection = Direction.unknown;
			return;
		}
		
		for(int j=len-1; j>=0; j--)
			for(int i=0; i<lanesNum; i++) {
				if(status[i][j]!=null && status[i][j].isWaited() && status[i][j].isPriority()) {
					int k=j+1;
					for(; k<len; k++)
						if(status[i][k]!=null && status[i][k].isWaited())
							break;
					if(k==len) {
						status[i][j].findNextRoad(cross);
						firstCarDirection = status[i][j].getDirection();
						firstCarLocation[0] = i;
						firstCarLocation[1] = j;
						return;
					}
				}
			}
		
		for(int j=len-1; j>=0; j--)
			for(int i=0; i<lanesNum; i++) {
				if(status[i][j]!=null && status[i][j].isWaited()) {
					status[i][j].findNextRoad(cross);
					firstCarDirection = status[i][j].getDirection();
					firstCarLocation[0] = i;
					firstCarLocation[1] = j;
					return;
				}
			}
		// all cars have been updated
		firstCarLocation[0] = 0;
		firstCarLocation[1] = len-1;
		firstCarDirection = Direction.unknown;
	}
	
	// -1 means ahead car is waited
	// -2 means all road have no enough space
	protected int getInRoadLaneNum(int nextDistance) {
		if(nextDistance<=0)
			return nextDistance;
		for(int i=0; i<lanesNum; i++) {
			int j=0;
			for(; j<nextDistance; j++)
				if(status[i][j]!=null) {
					// ahead car is waited
					if(status[i][j].isWaited())
						return -1;
					// ahead car is not waited
					else if(j!=0)
						return i;
					// this lane has no enough space
					else break;
				}
			// no ahead car
			if(nextDistance == 0)
				logger.error("arrive happen, not supposed to stepin this function");
			if(j==nextDistance)
				return i;
		}
		// all road has no enough space
		return -2;
	}
	
	protected int getBlankNum(int speed) {
		int count = 0;
		for(int i=0; i<lanesNum; i++) {
			int j;
			for(j=0; j<speed; j++)
				if(status[i][j] != null)
					break;
			count += j;
		}
		return count;
	}
	
	protected boolean containsCarFlow(CarFlow carflow) {
		for(int i=0; i<lanesNum; i++)
			for(int j=0; j<len; j++)
				if(status[i][j]!=null)
					if(status[i][j].getCarFlow()==carflow)
						return true;
		return false;
	}
	
	protected Road findNextWaitLinkRoad(Road[] roads) {
		if(firstCarDirection==Direction.unknown)
			return null;
		else if(firstCarDirection==Direction.direct || firstCarDirection==Direction.priDirect)
			return roads[1];
		else if(firstCarDirection==Direction.left || firstCarDirection==Direction.priLeft)
			return roads[0];
		else if(firstCarDirection==Direction.right || firstCarDirection==Direction.priRight)
			return roads[2];
		logger.error("error in wait link");
		return null;
	}
	
	public Direction getFirstCarDirection() {
		return firstCarDirection;
	}

	public int getCarNum() {
		return (int) carNum;
	}
	
	public int getLanesNum() {
		return lanesNum;
	}
	
	public int getLength() {
		return len;
	}

	public int getLoad() {
		return load;
	}

	public void changeLoad(int change) {
		this.load += change;
	}

	public int getLen() {
		return len;
	}

	public void setLen(int len) {
		this.len = len;
	}

	public Road getRoad() {
		return road;
	}

	public void setRoad(Road road) {
		this.road = road;
	}

	public Car[][] getStatus() {
		return status;
	}
	
	public Car getStatus(int i, int j) {
		return status[i][j];
	}
	
	public void setStatus(Car[][] status) {
		this.status = status;
	}
	
	public void setStatus(Car newCar, int i, int j) {
		status[i][j] = newCar; 
	}

	public int[] getFirstCarLocation() {
		return firstCarLocation;
	}

	public void setFirstCarLocation(int[] firstCarLocation) {
		this.firstCarLocation = firstCarLocation;
	}

	public Car getFirstCar() {
		return status[firstCarLocation[0]][firstCarLocation[1]];
	}
	
	public void setLanesNum(int lanesNum) {
		this.lanesNum = lanesNum;
	}

	public void setCarNum(int carNum) {
		this.carNum = carNum;
	}

	public void setFirstCarDirection(Direction firstCarDirection) {
		this.firstCarDirection = firstCarDirection;
	}

	public void setLoad(int load) {
		this.load = load;
	}
	
}
