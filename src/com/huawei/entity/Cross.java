package com.huawei.entity;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.huawei.util.MapUtil;

public class Cross{
	
	private static final Logger logger = Logger.getLogger(Cross.class);
	private static final int roadIdMax = 1000000;
	
	private int crossId;
	private int[] roadIds = new int[4];
	private int[] sequenceRoadIds = new int[] 
			{roadIdMax, roadIdMax, roadIdMax, roadIdMax};;
	private List<Road> roads = null;
	
	private int rotationStatus=0;

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
		
		// compute id sequence from min to max
		for(int i=0; i<4; i++)
			for(int j=0; j<4; j++) {
				if(roadIds[i]!=-1 && roadIds[i] < sequenceRoadIds[j]) {
					for(int k=3; k>j; k--)
						sequenceRoadIds[k] = sequenceRoadIds[k-1];
					sequenceRoadIds[j] = roadIds[i];
					break;
				}
			}
		// change sequenceRoadIds elements to roadId index or -1
		for(int i=0; i<4; i++)
			if(sequenceRoadIds[i]!=roadIdMax) {
				for(int j=0; j<4; j++)
					if(roadIds[j] == sequenceRoadIds[i]) {
						sequenceRoadIds[i] = j;
						break;
					}
			}
			else sequenceRoadIds[i] = -1;
					
	}
	
	public void initRoads() {
		roads = new ArrayList<Road>(4);
		for(int i=0; i<4; i++)
			if(roadIds[i]!=-1)
				roads.add(MapUtil.roads.get(roadIds[i]));
			else roads.add(null);
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
	
	public void updateCross() {
		initFirstCarDirection();
		int count, num=0;
		do {
			count = 0;
			num++;
			for(int i=0; i<4; i++) {
				if(sequenceRoadIds[i]==-1)
					break;
				Road road = roads.get(sequenceRoadIds[i]);
				count += road.updateWaitedCars(this);
			}
			logger.info("step2: cross " + crossId + ", " + count + " cars passed in iterator " + num);
		} while(count!=0);
		
	}
	
	private void initFirstCarDirection() {
		for(int i=0; i<4; i++)
			if(roads.get(i)!=null)
				roads.get(i).updateRoadDirections(this);
	}
	
	public Road findLinkedRoad(Cross crossTwo) {
		for(Road road :roads) {
			if(road.getAnOtherCross(crossId) == crossTwo.crossId)
				return road;
		}
		return null;
	}
	
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
