package com.huawei.sa;

import org.apache.log4j.Logger;

import com.huawei.entity.Car;
import com.huawei.entity.Cross;
import com.huawei.entity.OneWayRoad;
import com.huawei.entity.Road;
import com.huawei.entity.Car.Direction;
import com.huawei.service.Judge;
import com.huawei.util.MapUtil;

public class RuleHandleWithSA {

	private static final Logger logger = Logger.getLogger(RuleHandleWithSA.class);
	public static int MapMaxCarNum = 2000;
	
	public static int driveCarJustOnRoadToEndState(Judge judge, Road road) {
		int count = 0;
		count = driveCarJustOnRoadToEndState(judge, road.getForwardRoad());
		if(road.isBiDirect())
			count += driveCarJustOnRoadToEndState(judge, road.getBackwardRoad());
		return count;
	}
	
	private static int driveCarJustOnRoadToEndState(Judge judge, OneWayRoad oneWayRoad) {
		int count =0;
		for(int i=0; i<oneWayRoad.getLanesNum(); i++)
			count += driveCarJustOnLaneToEndState(judge, oneWayRoad, i);
		return count;
	}
	
	private static int driveCarJustOnLaneToEndState(Judge judge, OneWayRoad oneWayRoad, int lane) {
		if(oneWayRoad.getCarNum()==0)
			return 0;
		int count = 0;
		for(int j=oneWayRoad.getLen()-1; j>=0; j--) {
			Car car = oneWayRoad.getStatus(lane,j);
			if(car == null || !car.isWaited())
				continue;
			int s1 = oneWayRoad.getLen()-1-j;
			car.computeNowDistance(s1);
			int k=j+1;
			for(; k<=j+car.getNowDistance(); k++)
				if(oneWayRoad.getStatus(lane,k)!=null) {
					Car aheadCar = oneWayRoad.getStatus(lane,k);
					if(!aheadCar.isWaited()) {
						driveCar(judge,car);
						if(k!=j+1) {
							oneWayRoad.setStatus(oneWayRoad.getStatus(lane,j),lane,k-1);
							oneWayRoad.setStatus(null,lane,j);
						}
						count++;
					}
					break;
				}
			if(k==j+car.getNowDistance()+1) {
				// will not try to pass the cross
				if(car.getNextDistance()==0) {
					driveCar(judge,car);
					if(k!=j+1) {
						oneWayRoad.setStatus(oneWayRoad.getStatus(lane,j),lane,k-1);
						oneWayRoad.setStatus(null,lane,j);
					}
					count++;
				}
			}
		}
		return count;
	}
	
	public static int updateCross(Judge judge, Cross cross) {
		cross.newInitFirstCarDirection();
		int count = 0;
		for(int i=0; i<4; i++) {
			if(cross.getSequenceRoadIds()[i]==-1)
				break;
			Road road = cross.getRoads().get(cross.getSequenceRoadIds()[i]);
			if(road.getOrigin().getCrossId()==cross.getCrossId() && !road.isBiDirect())
				continue;
			count += updateRoad(judge, cross, road);
		}
		return count;
	}
	
	private static int updateRoad(Judge judge, Cross cross, Road road) {
		if(cross.getCrossId() == road.getDestination().getCrossId())
			return updateOneWayRoad(judge, cross, road.getForwardRoad(), road.getNeighbourDirections(cross));
		else if(road.isBiDirect())
			return updateOneWayRoad(judge, cross, road.getBackwardRoad(), road.getNeighbourDirections(cross));
		logger.error("step2: update invalid road cars " + road.getRoadId());
		return -1;
	}
	
	private static int updateOneWayRoad(Judge judge, Cross cross, OneWayRoad oneWayRoad, Direction[] directions){
		int count = 0;
		while(oneWayRoad.getFirstCarDirection()!=Direction.unknown) {
			Car car = oneWayRoad.getFirstCar();
			boolean jammedTag = false;
			switch(oneWayRoad.getFirstCarDirection()) {
				case priDirect: break;
				case priLeft:
					if(directions[2]==Direction.priDirect)
						jammedTag = true;
					break;
				case priRight: 
					if(directions[0]==Direction.priDirect || directions[1]==Direction.priLeft)
						jammedTag = true;
					break;
				case direct: 
					if(directions[0]==Direction.priLeft || directions[2]==Direction.priRight)
						jammedTag = true;
					break;
				case left:
					if(directions[1]==Direction.priRight || 
						directions[2]==Direction.priDirect ||
						directions[2]==Direction.direct)
						jammedTag = true;
					break;
				case right: 
					if(directions[0]==Direction.priDirect ||
						directions[1]==Direction.priLeft ||
						directions[0]==Direction.direct || 
						directions[1]==Direction.left)
						jammedTag = true;
					break;
				
				default: break;
			}
			// jammed means car could not pass the cross because of other roads' directs
			if(jammedTag)
				return count;
			int flag = updateCar(judge, car, cross);
			int[] firstCarLocation = oneWayRoad.getFirstCarLocation();
			if(flag == -1)
				return count;
			else if(flag == -2) {
				if(firstCarLocation[1] != oneWayRoad.getLen()-1) {
					oneWayRoad.setStatus(oneWayRoad.getStatus(firstCarLocation[0], firstCarLocation[1]), firstCarLocation[0], oneWayRoad.getLen()-1);
					oneWayRoad.setStatus(null, firstCarLocation[0], firstCarLocation[1]);
				}
				count++;
				count += driveCarJustOnLaneToEndState(judge, oneWayRoad, firstCarLocation[0]);
				drivePriCarInGarage(judge,oneWayRoad.getRoad().getAnOtherCross(cross),oneWayRoad.getRoad());
				oneWayRoad.newUpdateRoadDirection(cross);
			}
			else{
				oneWayRoad.setStatus(null, firstCarLocation[0], firstCarLocation[1]);
				oneWayRoad.setCarNum(oneWayRoad.getCarNum()-1);
				count++;
				oneWayRoad.outLoad++;
				count += driveCarJustOnLaneToEndState(judge, oneWayRoad, firstCarLocation[0]);
				drivePriCarInGarage(judge,oneWayRoad.getRoad().getAnOtherCross(cross),oneWayRoad.getRoad());
				oneWayRoad.newUpdateRoadDirection(cross);
			}
		}
		return count;
	}
	
	// have decided next road
	private static int updateCar(Judge judge, Car car, Cross cross) {
		if(car.getDestination() == cross.getCrossId()) {
			arriveCar(judge, car);
			return -3;
		}
		else if(car.getNextDistance() == -2){
			driveCar(judge, car);
			return -2;
		}
		else {
			int laneNum = car.getNextRoad().getInRoadLaneNum(cross, car.getNextDistance());
			
			// could pass the cross
			if(laneNum >= 0){
				updateNextRoad(judge, cross, car, laneNum);
				passCar(judge, car);
			}
			// will not pass the cross because no enough space
			else if(laneNum == -2)
				driveCar(judge, car);
			return laneNum;
		}
	}
	
	private static void updateNextRoad(Judge judge, Cross cross, Car car, int lane) {
		if(cross.getCrossId() == car.getNextRoad().getOrigin().getCrossId())
			updateNextOneWayRoad(judge, car, car.getNextRoad().getForwardRoad(), lane);
		else if(car.getNextRoad().isBiDirect())
			updateNextOneWayRoad(judge, car, car.getNextRoad().getBackwardRoad(), lane);
		else logger.error("step2: invalid car " + car.getCarId());
	}
	
	private static void updateNextOneWayRoad(Judge judge, Car car, OneWayRoad oneWayRoad, int lane) {
		for(int j=0; j<car.getNextDistance(); j++)
			if(oneWayRoad.getStatus(lane,j)!=null) {
				if(j==0 || oneWayRoad.getStatus(lane,j).isWaited())
					logger.error("invalid car "+car.getCarId()+" in road ");
				oneWayRoad.setStatus(car, lane, j-1);
				oneWayRoad.setCarNum(oneWayRoad.getCarNum()+1);
				oneWayRoad.inLoad++;
				return;
			}
		oneWayRoad.inLoad++;
		oneWayRoad.setStatus(car, lane, car.getNextDistance()-1);
		oneWayRoad.setCarNum(oneWayRoad.getCarNum()+1);
	}
	
	private static void driveCar(Judge judge, Car car) {
		car.setWaited(false);
		judge.getNowWaitedCars().remove(car);
	}
	
	private static boolean startoffCar(Judge judge, Car car, Cross cross) {
		if(car.getOrigin()!=cross.getCrossId())
			logger.error("car start off error");
		car.computeNextDistance(car.getNextRoad());
		if(updateCar(judge, car, MapUtil.crosses.get(car.getOrigin()))>=0) {
			car.setRunning(true);
			if(car.isPriority())
				cross.priOutRoadCars.remove(car);
			else cross.outRoadCars.remove(car);
			judge.getNowRunCars().add(car);
			return true;
		}
		else return false;
	}
	
	private static void arriveCar(Judge judge, Car car) {
		car.setRealEndTime(judge.getTerm());
		car.setNowRoad(null);
		car.setNextRoad(null);
		car.setRunning(false);
		car.setWaited(false);
		judge.getNowWaitedCars().remove(car);
		judge.getNowRunCars().remove(car);
		judge.getFinishCars().add(car);
		judge.setFinishCount(judge.getFinishCount()+1);
	}
	
	private static void passCar(Judge judge, Car car) {
		car.setNowRoad(car.getNextRoad());
		car.setNextRoad(null);
		car.setDirection(Direction.unknown);
		if(!car.isPreset())
			car.getRoadList().add(car.getNowRoad());
		car.setWaited(false);
		judge.getNowWaitedCars().remove(car);
	}
	
	public static int driveCarInGarage(Judge judge) {
		int count = 0;
		for(Cross cross : MapUtil.crosses.values()) {
			for(int i=0; i<4; i++) {
				if(cross.getSequenceRoadIds()[i]==-1)
					break;
				Road road = cross.getRoads().get(cross.getSequenceRoadIds()[i]);
				if(road.getDestination().getCrossId()==cross.getCrossId() && !road.isBiDirect())
					continue;
				count += driveCarInGarage(judge,cross,road);
			}
		}
		return count;
	}
	
	private static int driveCarInGarage(Judge judge, Cross cross, Road road) {
		int count = 0;
		for(int i=0; i<cross.outRoadCars.size(); i++) {
			Car car = cross.outRoadCars.get(i);
			if(car.getRealStartTime() <= judge.getTerm()) {
				if(!car.isPreset() && judge.nowRunCars.size()>MapMaxCarNum)
					continue;
				Road nextRoad = car.findNextRoad(cross);
				if(nextRoad == null || nextRoad.getRoadId()!=road.getRoadId()) {
					car.setNextRoad(null);
					continue;
				}
				if(startoffCar(judge, car, cross)) {
//					logger.info("start off one car, now run " + judge.nowRunCars.size());
					count++;
					i--;
				}
				else if(!car.isPreset())
					car.setNextRoad(null);
			}
			else break;
		}
		return count;
	}
	
	public static int drivePriCarInGarage(Judge judge) {
		int priAddCount = 0;
		for(Cross cross : MapUtil.crosses.values()) {
			for(int i=0; i<4; i++) {
				if(cross.getSequenceRoadIds()[i]==-1)
					break;
				Road road = cross.getRoads().get(cross.getSequenceRoadIds()[i]);
				if(road.getDestination().getCrossId()==cross.getCrossId() && !road.isBiDirect())
					continue;
				priAddCount += drivePriCarInGarage(judge,cross,road);
			}
		}
		return priAddCount;
	}
	
	private static int drivePriCarInGarage(Judge judge, Cross cross, Road road) {
		int count = 0;
		for(int i=0; i<cross.priOutRoadCars.size(); i++) {
			Car car = cross.priOutRoadCars.get(i);
			if(car.getRealStartTime() <= judge.getTerm()) {
				if(!car.isPreset() && judge.nowRunCars.size()>MapMaxCarNum)
					continue;
				Road nextRoad = car.findNextRoad(cross);
				if(nextRoad == null || nextRoad.getRoadId()!=road.getRoadId()) {
					car.setNextRoad(null);
					continue;
				}
				if(startoffCar(judge, car, cross)) {
//					logger.info("start off one pri car, now run " + judge.nowRunCars.size());
					count++;
					i--;
				}
				else if(!car.isPreset())
					car.setNextRoad(null);
				
			}
			else break;
		}
		return count;
	}
}
