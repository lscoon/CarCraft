package com.huawei.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;

public class Cross{
	
	private static final Logger logger = Logger.getLogger(Cross.class);
	
	private int crossId;
	private int[] roadIds = new int[4];;
	private List<Road> roads = null;
	
	private int rotationStatus=0;
	
	// means road direction: north, east, south, west
	//public enum Direction{n,e,s,w};
	
	// key matrix, don't ask me why
	//private static final int[][] rotationMatrix = {{0,3,2,1},{1,0,3,2},{2,3,0,1},{1,2,3,0}};
		
	public Cross (String[] strs) {
		if (strs.length != 5) {
			logger.error("cross create format error: " + strs);
			return;
		}
		crossId = Integer.valueOf(strs[0].trim()).intValue();
		roadIds[0] = Integer.valueOf(strs[1].trim()).intValue();
		roadIds[1] = Integer.valueOf(strs[2].trim()).intValue();
		roadIds[2] = Integer.valueOf(strs[3].trim()).intValue();
		roadIds[3] = Integer.valueOf(strs[4].trim()).intValue();
	}
	
	public void initRoads() {
		roads = new ArrayList<Road>(4);
		for(int i=0; i<4; i++)
			roads.add(RoadMap.roads.get(roadIds[i]));
	}
	
	public String info() {
		String info = "\n";
		info = info.concat(crossId + "\n");
		info = info.concat(roadIds[0] + "\n");
		info = info.concat(roadIds[1] + "\n");
		info = info.concat(roadIds[2] + "\n");
		info = info.concat(roadIds[3] + "\n");
		return info;
	}

	public int getRotation(int roadId) {
		for(int i=0; i<4; i++)
			if(roadId==roadIds[i])
				return i;
		return -1;
	}
	
	
	/*
	
	public int getDirectRoadId(int rId) {
		for(int i=0; i<4; i++)
			if(rId == roadId[i])
				return roadId[(i+2)%4];
		logger.error("Road not in Cross");
		return -1;
	}
	
	public int getLeftRoadId(int rId) {
		for(int i=0; i<4; i++)
			if(rId == roadId[i])
				return roadId[(i+1)%4];
		logger.error("Road not in Cross");
		return -1;
	}
	
	public int getRightRoadId(int rId) {
		for(int i=0; i<4; i++)
			if(rId == roadId[i])
				return roadId[(i+3)%4];
		logger.error("Road not in Cross");
		return -1;
	}
	
	public int[] getRoadIds() {
		return roadId;
	}
	
	// sequence by road id from max to min
	public List<Integer> getOrderedRoadIds() {
		List<Integer> list = new ArrayList<Integer>();
		for(int i=0; i<4; i++)
			if(roadId[i]!=-1)
				list.add(roadId[i]);
		Collections.sort(list);
		return list;
	}
	
	
	// clockwise order since rId'next road
	public List<Integer> getOtherRoadIds(int rId){
		List<Integer> list = new ArrayList<Integer>();
		for(int i=0; i<4; i++)
			if(rId == roadId[i]) {
				for(int j=i+1; j<4; j++)
					list.add(roadId[j]);
				for(int j=0; j<i; j++)
					list.add(roadId[j]);
			}
		return list;
	}
	*/
	
	public int getCrossId() {
		return crossId;
	}

	public int[] getRoadIds() {
		return roadIds;
	}
	
	public List<Road> getRoads() {
		return roads;
	}

	public int getRotationStatus() {
		return rotationStatus;
	}

	public void setRotationStatus(int rotation) {
		this.rotationStatus = rotation;
	}
	
}
