package com.huawei.entity;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.huawei.util.MapUtil;

public class Cross{
	
	private static final Logger logger = Logger.getLogger(Cross.class);
	
	private int crossId;
	private int[] roadIds = new int[4];
	private int[] sequenceRoadIds = new int[] 
			{MapUtil.roadIdMax, MapUtil.roadIdMax, MapUtil.roadIdMax, MapUtil.roadIdMax};;
	private List<Road> roads = null;
	
	public List<Car> outRoadCars = new ArrayList<>();
	public List<Car> priOutRoadCars = new ArrayList<>();
	
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
		int temp = 0;
		for(int i=0; i<4; i++)
			if(roadIds[i] != -1) {
				sequenceRoadIds[temp] = roadIds[i];
				temp++;
			}
		for(int i=temp; i<4; i++)
			sequenceRoadIds[temp] = -1;
		
		int i,j,v;
		for (i=1; i<temp; i++) {
			for (v=sequenceRoadIds[i], j=i-1; j>=0&&v<sequenceRoadIds[j]; j--)
	            sequenceRoadIds[j+1]=sequenceRoadIds[j];
	        sequenceRoadIds[j+1]=v;
		}
		
		for(i=0; i<temp; i++)
			for(j=0; j<4; j++)
				if(roadIds[j] == sequenceRoadIds[i]) {
					sequenceRoadIds[i] = j;
					break;
				}	
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
	
	public void initFirstCarDirection() {
		for(int i=0; i<4; i++)
			if(roads.get(i)!=null) {
				Road road = roads.get(i);
				if(road.getOrigin().getCrossId()==crossId && !road.isBiDirect())
					continue;
				road.updateRoadDirections(this);
			}
	}
	
	public void newInitFirstCarDirection() {
		for(int i=0; i<4; i++)
			if(roads.get(i)!=null) {
				Road road = roads.get(i);
				if(road.getOrigin().getCrossId()==crossId && !road.isBiDirect())
					continue;
				road.newUpdateRoadDirections(this);
			}
	}
	
	public Road findLinkedRoad(Cross crossTwo) {
		for(Road road :roads) {
			if(road != null)
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

	public int[] getSequenceRoadIds() {
		return sequenceRoadIds;
	}

	public void setSequenceRoadIds(int[] sequenceRoadIds) {
		this.sequenceRoadIds = sequenceRoadIds;
	}

	public void setCrossId(int crossId) {
		this.crossId = crossId;
	}

	public void setRoadIds(int[] roadIds) {
		this.roadIds = roadIds;
	}

	public void setRoads(List<Road> roads) {
		this.roads = roads;
	}
	
	
}
