package com.huawei.data;

import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;

import com.huawei.util.Util;

public class Road {

	private static final Logger logger = Logger.getLogger(Road.class);
	
	private int roadId;
	private int limitSpeed;
	private Cross origin;
	private Cross destination;
	
	// 1 means yes, 0 means no
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
	/*
	
	public int getLinkedCross(int crossId) {
		if(crossId==origin)
			return destination;
		else return origin;
	}
	
	// check if car carId on this road
	public boolean carOnRoad(int carId) {
		for (int i=0; i<lanesNum*(isBiDirect+1); i++)
			for (int j=0; j<length; j++)
				if(status[i][j]==carId)
					return true;
		return false;
	}
	
	// return car lane list from carId to cross
	public List<Integer> getCarLane(int carId) {
		for (int i=0; i<lanesNum*(isBiDirect+1); i++)
			for (int j=0; j<length; j++)
				if(status[i][j]==carId) {
					List<Integer> temp = new LinkedList<Integer>();
					if(isBiDirect==1) {
						if(i<lanesNum) {
							for(int k=j;k>=0;k--)
								temp.add(status[i][k]);
						} else {
							for(int k=j;k<length;k++)
								temp.add(status[i][k]);
						}
					} else {
						for(int k=j;k<length;k++)
							temp.add(status[i][k]);
					}
				}
		return null;
	}
	
	// order road car sequence while passing the cross
	public List<Integer> getOrderPassCrossCarSequence(){
		if(isBiDirect==0) {
			List<Integer> temp = new LinkedList<Integer>();
			for(int j=length-1; j>=0; j--)
				for(int i=0; i<lanesNum; i++)
					temp.add(status[i][j]);
			return temp;
		}
		else {
			List<Integer> temp = new LinkedList<Integer>();
			for(int j=length-1; j>=0; j--)
				for(int i=lanesNum; i<2*lanesNum; i++)
					temp.add(status[i][j]);
			return temp;
		}	
	}
	
	// reverse road car sequence while passing the cross
	public List<Integer> getReversePassCrossCarSequence(){
		if(isBiDirect==0) {
			logger.error("get reverse car sequence from single road " + roadId);
			return null;
		}
		List<Integer> temp = new LinkedList<Integer>();
		for(int j=0; j<length; j++)
			for(int i=lanesNum-1; i>=0; i--)
				temp.add(status[i][j]);
		return temp;
	}
	
	public void updateRunnableCars() {
		updateForwardRunnableCars();
		if(isBiDirect==1)
			updateBackwardRunnableCars();
	}
	
	private void updateForwardRunnableCars() {
		for(int i=0+(isBiDirect)*lanesNum; i<(isBiDirect+1)*lanesNum; i++)
			for(int j=length-1;j>=0;j--) {
				int carId = status[i][j];
				Car car = RoadMap.cars.get(carId);
				int s1 = length-1-j;
				int nowSpeed = Math.min(car.getMaxSpeed(), limitSpeed);
				//if(nowSpeed>s1)
			}
	}
	
	private void updateBackwardRunnableCars() {
		
	}*/
	
	public int getRoadId() {
		return roadId;
	}
}
