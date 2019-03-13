package com.huawei.data;

import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;

import com.huawei.util.Util;

public class Road {

	private static final Logger logger = Logger.getLogger(Road.class);
	
	private int roadId;
	private int length;
	private int limitSpeed;
	private int lanesNum;
	private int origin;
	private int destination;
	
	// 1 means yes, 0 means no
	private int isBiDirect;
	private int[][] status;
	
	public Road (String[] strs) {
		if (strs.length != 7) {
			logger.error("road create format error: " + strs);
			return;
		}
		roadId = Integer.valueOf(strs[0].trim()).intValue();
		length = Integer.valueOf(strs[1].trim()).intValue();
		limitSpeed = Integer.valueOf(strs[2].trim()).intValue();
		lanesNum = Integer.valueOf(strs[3].trim()).intValue();
		origin = Integer.valueOf(strs[4].trim()).intValue();
		destination = Integer.valueOf(strs[5].trim()).intValue();
		isBiDirect = Integer.valueOf(strs[6].trim()).intValue();
		
		status = new int[lanesNum*(isBiDirect+1)][length];
	}
	
	public int getRoadId() {
		return roadId;
	}

	public void setRoadId(int id) {
		this.roadId = id;
	}

	public int getLength() {
		return length;
	}

	public void setLength(int length) {
		this.length = length;
	}

	public int getLimitSpeed() {
		return limitSpeed;
	}

	public void setLimitSpeed(int limitSpeed) {
		this.limitSpeed = limitSpeed;
	}

	public int getLanesNum() {
		return lanesNum;
	}

	public void setLanesNum(int lanesNum) {
		this.lanesNum = lanesNum;
	}

	public int getOrigin() {
		return origin;
	}

	public void setOrigin(int origin) {
		this.origin = origin;
	}

	public int getDestination() {
		return destination;
	}

	public void setDestination(int destination) {
		this.destination = destination;
	}

	public int getIsBiDirect() {
		return isBiDirect;
	}

	public void setIsBiDirect(int isBiDirectional) {
		this.isBiDirect = isBiDirectional;
	}
	
	public String info() {
		String info = "\n";
		info = info.concat(roadId + "\n");
		info = info.concat(limitSpeed + "\n");
		info = info.concat(origin + "\n");
		info = info.concat(destination + "\n");
		return info;
	}
	
	// show road matrix status in map panel
	public String showStatus() {
		int carIdMaxLength = countNum(Util.CarIdMaxLength);
		String temp = "";
		for(int i=0; i<lanesNum*(isBiDirect+1); i++) {
			for(int j=0; j<length; j++) {
				int blankCount = carIdMaxLength-countNum(status[i][j]);
				for(int k=0; k<blankCount; k++)
					temp = temp.concat(" ");
				temp = temp.concat(status[i][j]+"");
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
	
}
