package com.huawei.entity;

import org.apache.log4j.Logger;

import com.huawei.entity.Car.Direction;
import com.huawei.util.MapUtil;

/**
 * @author sc
 *
 */
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
					temp = temp.concat(status[i][j].getCarId()+"");
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
					temp = temp.concat(status[i][j].getCarId()+"");
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
	
	private int countNum(int input) {
		if(input/10==0)
			return 1;
		else 
			return countNum(input/10) + 1;
	}
	
	protected int updateRunnableCars() {
		int count =0;
		for(int i=0; i<lanesNum; i++)
			count += updateLaneRunnableCars(i);
		return count;
	}
	
	// only consider and update cars that will not pass the cross
	private int updateLaneRunnableCars(int i) {
		int count = 0;
		if(carNum==0)
			 return count;
		for(int j=len-1;j>=0;j--) {
			Car car = status[i][j];
			if(car == null || !car.isWaited())
				continue;
			int s1 = len-1-j;
			car.computeNowAndNextDistance(s1);
			
			int k=j+1;
			for(; k<=j+car.getNowDistance(); k++)
				if(status[i][k]!=null) {
					Car aheadCar = status[i][k];
					
					// aheadCar is updated will update nowCar, otherwise not consider
					if(!aheadCar.isWaited()) {
						// forward k-j-1 step
						car.stepForward();
						if(k!=j+1) {
							status[i][k-1] = status[i][j];
							status[i][j]=null;
						}
						count++;
					}
					break;
				}
			
			// no ahead car
			if(k==j+car.getNowDistance()+1) {
//				if(car.getNextDistance()==-1) {
//					car.arrive();
//					carNum--;
//					status[i][j]=null;
//					count++;
//				}
//				else 
				if(car.getNextDistance()==0) {
					car.stepForward();
					if(k!=j+1) {
						status[i][k-1] = status[i][j];
						status[i][j]=null;
					}
					count++;
				}
			}
		}
		return count;
	}
	
	protected int updateWaitedCars(Cross cross, Direction[] directions) {
		int count = 0;
		while(firstCarDirection!=Direction.unknown) {
			Car car = status[firstCarLocation[0]][firstCarLocation[1]];
			boolean jammedTag = false;
			switch(firstCarDirection) {
				case direct: break;
				case right: 
					if(directions[0]==Direction.direct || directions[1]==Direction.left)
						jammedTag = true;
					break;
				case left:
					if(directions[2]==Direction.direct)
						jammedTag = true;
					break;
				default: break;
			}
			// jammed means car could not pass the cross because of other roads' directs
			if(jammedTag)
				return count;
			int flag = car.updateCarWhilePassCross(cross);
			// >=0 could step in
			// -1 waited ahead car
			// -2 no enough space
			// -3 arrive
			if(flag == -1)
				return count;
			else if(flag == -2) {
				if(firstCarLocation[1] != len-1) {
					status[firstCarLocation[0]][len-1] = status[firstCarLocation[0]][firstCarLocation[1]];
					status[firstCarLocation[0]][firstCarLocation[1]] = null;
				}
				count++;
				count += updateLaneRunnableCars(firstCarLocation[0]);
				updateRoadDirection();
			}
			else if(flag == -3) {
				carNum--;
				status[firstCarLocation[0]][firstCarLocation[1]] = null;
				count++;
				count += updateLaneRunnableCars(firstCarLocation[0]);
				updateRoadDirection();
			}
			else{
				status[firstCarLocation[0]][firstCarLocation[1]] = null;
				carNum--;
				count++;
				count += updateLaneRunnableCars(firstCarLocation[0]);
				updateRoadDirection();
			}
		}
		return count;
	}
	
	// pass cross order, means z
	protected void updateRoadDirection() {
		if(carNum==0) {
			firstCarLocation[0] = 0;
			firstCarLocation[1] = len-1;
			firstCarDirection = Direction.unknown;
			return;
		}
		for(int j=firstCarLocation[1]; j>=0; j--)
			for(int i=firstCarLocation[0]; i<lanesNum; i++) {
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
	
	// -1 means ahead car is waited
	// -2 means all road have no enough space
	protected int getInRoadLaneNum(int nextDistance) {
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
	
	protected void updateRoadWhilePassCross(Car car, int lanenum) {
		for(int j=0; j<car.getNextDistance(); j++)
			if(status[lanenum][j]!=null) {
				if(j==0 || status[lanenum][j].isWaited())
					logger.error("invalid car "+car.getCarId()+" in road ");
				status[lanenum][j-1] = car;
				carNum++;
				return;
			}
		status[lanenum][car.getNextDistance()-1] = car;
		carNum++;
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
	
	protected Direction getFirstCarDirection() {
		return firstCarDirection;
	}

	public int getCarNum() {
		return (int) load;
	}
	
	protected int getLanesNum() {
		return lanesNum;
	}
	
	protected int getLength() {
		return len;
	}

	public int getLoad() {
		return load;
	}

	public void changeLoad(int change) {
		this.load += change;
	}
}
