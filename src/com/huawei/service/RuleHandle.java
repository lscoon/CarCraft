package com.huawei.service;

import java.util.ArrayList;

import org.apache.log4j.Logger;

import com.huawei.entity.Car;
import com.huawei.entity.CarFlow;
import com.huawei.entity.Cross;
import com.huawei.entity.OneWayRoad;
import com.huawei.entity.Road;
import com.huawei.entity.Car.Direction;
import com.huawei.util.MapUtil;

public class RuleHandle {

	private static final Logger logger = Logger.getLogger(RuleHandle.class);
	
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
			car.computeNowAndNextDistance(s1);
			int k=j+1;
			for(; k<=j+car.getNowDistance(); k++)
				if(oneWayRoad.getStatus(lane,k)!=null) {
					Car aheadCar = oneWayRoad.getStatus(lane,k);
					// aheadCar is updated will update nowCar, otherwise not consider
					if(!aheadCar.isWaited()) {
						// forward k-j-1 step
						driveCar(judge,car);
						if(k!=j+1) {
							oneWayRoad.setStatus(oneWayRoad.getStatus(lane,j),lane,k-1);
							oneWayRoad.setStatus(null,lane,j);
						}
						count++;
					}
					break;
				}
			
			// no ahead car
			if(k==j+car.getNowDistance()+1) {
//				next distance = -1/-2 will not consider
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
		cross.initFirstCarDirection();
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
				drivePriCarInGarage(judge,oneWayRoad.getRoad());
				oneWayRoad.updateRoadDirection();
			}
			else{
				oneWayRoad.setStatus(null, firstCarLocation[0], firstCarLocation[1]);
				oneWayRoad.setCarNum(oneWayRoad.getCarNum()-1);
				count++;
				count += driveCarJustOnLaneToEndState(judge, oneWayRoad, firstCarLocation[0]);
				drivePriCarInGarage(judge,oneWayRoad.getRoad());
				oneWayRoad.updateRoadDirection();
			}
		}
		return count;
	}
	
	// >=0 could step in
	// -1 waited ahead car
	// -2 no enough space or no enough speed
	// -3 arrive
	private static int updateCar(Judge judge, Car car, Cross cross) {
		if(car.getNextRoad()==null) {
			arriveCar(judge, car);
			return -3;
		}
		else if(car.getNextDistance() == -2) {
			driveCar(judge, car);
			return -2;
		}
		else {
			if(car.getNextRoad().getRoadId()==5264)
				logger.debug("111");
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
				return;
			}
		oneWayRoad.setStatus(car, lane, car.getNextDistance()-1);
		oneWayRoad.setCarNum(oneWayRoad.getCarNum()+1);
	}
	
	private static void driveCar(Judge judge, Car car) {
		car.setWaited(false);
		judge.getNowWaitedCars().remove(car);
	}
	
	private static void passCar(Judge judge, Car car) {
		if(car.getCarFlow()!=null && car.getNowRoad() != null) {
			if(car.getRoadList().indexOf(car.getNowRoad())==0)
				car.getNowRoad().changeLoad(car.getOrigin(), -1);
			else car.getNowRoad().changeLoad(car.getRoadList().get(
					car.getRoadList().indexOf(car.getNowRoad())-1), -1);
		}
		car.setNowRoad(car.getNextRoad());
		int index = car.getRoadList().indexOf(car.getNowRoad());
		if(index == car.getRoadList().size()-1)
			car.setNextRoad(null);
		else car.setNextRoad(car.getRoadList().get(index+1));
		car.computeDirection();
		car.setWaited(false);
		judge.getNowWaitedCars().remove(car);
	}
	
	private static void arriveCar(Judge judge, Car car) {
		if(car.getCarFlow()!=null) {
			if(car.getRoadList().size() == 1)
				car.getNowRoad().changeLoad(car.getOrigin(), -1);
			else car.getNowRoad().changeLoad(car.getRoadList().get(
					car.getRoadList().indexOf(car.getNowRoad())-1), -1);
			car.getCarFlow().getRunCars().remove(car);
		}
		
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
	
	private static boolean startoffCar(Judge judge, Car car) {
		car.setNextDistance(Math.min(car.getMaxSpeed(), car.getNextRoad().getLimitSpeed()));
		if(updateCar(judge, car, MapUtil.crosses.get(car.getOrigin())) >=0 ) {
			if(judge instanceof JudgeWithFlow && car.isPreset())
				startoffPresetCar((JudgeWithFlow)judge, car);
			car.setRunning(true);
			car.setRealStartTime(judge.getTerm());
			if(car.isPriority())
				judge.getPriOutRoadCars().remove(car);
			else judge.getOutRoadCars().remove(car);
			judge.getNowRunCars().add(car);
			return true;
		}
		car.setRealStartTime(car.getRealStartTime()+1);
		return false;
	}
	
	private static void startoffPresetCar(JudgeWithFlow judge, Car car) {
//		if(car.getCarId()==94604)
//			logger.debug("11");
		
		CarFlow presetCarFlow = new CarFlow(car);
		presetCarFlow.setRunning(true);
		presetCarFlow.setPreset(true);
		ArrayList<Road> list = new ArrayList<>();
		list.addAll(car.getRoadList());
		presetCarFlow.setRoadList(list);
		presetCarFlow.updateLoad(1);
		presetCarFlow.getOutRoadCars().remove(car);
		presetCarFlow.getRunCars().add(car);
		judge.addnowRunCarFlows(presetCarFlow);
	}
	
	public static int driveCarInGarage(Judge judge){
		int count = 0;
		for (int i = 0; i < judge.getOutRoadCars().size(); i++) {
			Car car = judge.getOutRoadCars().get(i);
			if (car.getStartTime() <= judge.getTerm()) {
				if (startoffCar(judge, car)) {
					count++;
					i--;
				}
			}
			else break;
		}
		return count;
	}
	
	public static int drivePriCarInGarage(Judge judge) {
		int count = 0;
		for (int i = 0; i < judge.getPriOutRoadCars().size(); i++) {
			Car car = judge.getPriOutRoadCars().get(i);
			if (car.getStartTime() <= judge.getTerm()) {
				if (startoffCar(judge, car)) {
					count++;
					i--;
				}
			}
			else break;
		}
		return count;
	}
	
	public static int drivePriCarInGarage(Judge judge, Road road) {
		int count = 0;
		for (int i = 0; i < judge.getPriOutRoadCars().size(); i++) {
			Car car = judge.getPriOutRoadCars().get(i);
			if(car.getRoadList().get(0).getRoadId() != road.getRoadId())
				continue;
			if (car.getStartTime() <= judge.getTerm()) {
				if (startoffCar(judge, car)) {
					count++;
					i--;
				}
			}
			else break;
		}
		return count;
	}
}
