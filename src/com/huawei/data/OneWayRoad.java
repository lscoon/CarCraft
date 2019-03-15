package com.huawei.data;

import org.apache.log4j.Logger;

import com.huawei.data.Car.Direction;
import com.huawei.util.Util;

public class OneWayRoad {
	
	private static final Logger logger = Logger.getLogger(OneWayRoad.class);
	
	private int lanesNum;
	private int len;
	private Road road;
	
	private Car[][] status;
	private int[] firstCar;
	private int carNum = 0;
	private Direction roadDirection = Direction.n; 
	
	public OneWayRoad(int lanesNumber, int length, Road r) {
		lanesNum = lanesNumber;
		len = length;
		road = r;
		status = new Car[lanesNumber][length];
		firstCar = new int[]{0,len-1};
	}
	
	protected String showForwardStatus() {
		int carIdMaxLength = countNum(Util.CarIdMaxLength);
		String temp = "";
		for(int i=0; i<lanesNum; i++) {
			for(int j=0; j<len; j++) 
				if(status[i][j]!=null){
					int blankCount = carIdMaxLength-countNum(status[i][j].getCarId());
					for(int k=0; k<blankCount; k++)
						temp = temp.concat(" ");
					temp = temp.concat(status[i][j].getCarId()+"");
				}
				else {
					for(int k=0; k<carIdMaxLength; k++)
						temp = temp.concat(" ");
				}
			temp = temp.concat("\n");
		}
		return temp;
	}
	
	protected String showBackwardStatus() {
		int carIdMaxLength = countNum(Util.CarIdMaxLength);
		String temp = "";
		for(int i=lanesNum-1; i>=0; i--) {
			for(int j=len-1; j>=0; j--) 
				if(status[i][j]!=null){
					int blankCount = carIdMaxLength-countNum(status[i][j].getCarId());
					for(int k=0; k<blankCount; k++)
						temp = temp.concat(" ");
					temp = temp.concat(status[i][j].getCarId()+"");
				}
				else {
					for(int k=0; k<carIdMaxLength; k++)
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
			if(car == null)
				continue;
			int s1 = len-1-j;
			car.computeNowAndNextDistance(s1);
			
			int k=j+1;
			for(; k<=j+car.getNowDistance(); k++)
				if(status[i][k]!=null) {
					Car aheadCar = status[i][k];
					
					// aheadCar is updated will update nowCar, otherwise not consider
					if(aheadCar.isUpdated()) {
						// forward k-j-1 step
						car.stepForward();
						if(k-j!=1) {
							status[i][k-1] = status[i][j];
							status[i][j]=null;
						}
						count++;
					}
					break;
				}
			
			// no ahead car
			if(k==j+car.getNowDistance()+1) {
				if(car.getNextDistance()==-1) {
					car.arrive();
					carNum--;
					status[i][j]=null;
					count++;
				}
				else if(car.getNextDistance()==0) {
					car.stepForward();
					if(k-j!=1) {
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
		while(roadDirection!=Direction.n) {
			Car car = status[firstCar[0]][firstCar[1]];
			boolean jammedTag = false;
			switch(roadDirection) {
				case d: break;
				case r: 
					if(directions[0]==Direction.d || directions[1]==Direction.l)
						jammedTag = true;
					break;
				case l:
					if(directions[2]==Direction.d)
						jammedTag = true;
					break;
				default: break;
			}
			// jammed means car could not pass the cross because of other roads' directs
			if(jammedTag)
				return count;
			// false means could not pass the cross because of ahead not updated cars
			if(!car.updateCarWhilePassCross(cross))
				return count;
			// true means could in nextRoad
			status[firstCar[0]][firstCar[1]] = null;
			updateLaneRunnableCars(firstCar[0]);
			updateRoadDirection();
			carNum--;
			count++;
		}
		return count;
	}
	
	// pass cross order, means z
	protected void updateRoadDirection() {
		if(carNum==0)
			return;
		for(int j=firstCar[1]; j>=0; j--)
			for(int i=firstCar[0]; i<lanesNum; i++) {
				if(status[i][j]!=null && !status[i][j].isUpdated()) {
					roadDirection = status[i][j].getDirection();
					firstCar[0] = i;
					firstCar[1] = j;
					return;
				}
			}
		// all cars have been updated
		firstCar[0] = 0;
		firstCar[1] = len-1;
		roadDirection = Direction.n;
	}
	
	protected int getInRoadLaneNum(int nextDistance) {
		for(int i=0; i<lanesNum; i++) {
			int j=0;
			for(; j<nextDistance; j++)
				if(status[i][j]!=null) {
					// ahead car is not updated, wait
					if(!status[i][j].isUpdated())
						return -1;
					// ahead car is updated
					else if(j!=0)
						return i;
					// this lane has no enough space
					else break;
				}
			// no ahead car
			if(j==nextDistance)
				return i;
		}
		// all road has no enough space
		return -1;
	}
	
	protected void updateRoadWhilePassCross(Car car, int lanenum) {
		for(int j=0; j<car.getNextDistance(); j++)
			if(status[lanenum][j]!=null) {
				if(j==0)
					logger.error("invalid car "+car.getCarId()+" in road ");
				status[lanenum][j-1] = car;
				carNum++;
				return;
			}
		status[lanenum][car.getNextDistance()-1] = car;
		carNum++;
	}
	
	protected Direction getRoadDirection() {
		return roadDirection;
	}

	public int getCarNum() {
		return carNum;
	}
}
