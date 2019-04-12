package com.huawei.util;

import java.util.LinkedHashMap;
import java.util.Map;

import com.huawei.entity.Cross;
import com.huawei.entity.Road;

public class FloydUtil {
	
	public static Map<Integer, Road[][]> pathMap = null;
	public static Map<Integer, float[][]> distMap = null;
	
	public static void initPathAndDistMatrixMap() {
		pathMap = new LinkedHashMap<>();
		distMap = new LinkedHashMap<>();
		for(int i=MapUtil.AllCarMinSpeed; i<=MapUtil.AllCarMaxSpeed; i++)
			floyd(i);
	}
	
	private static void floyd(int speed) {
		Road[][] path = new Road[MapUtil.crosses.size()][MapUtil.crosses.size()];
		float[][] dist = new float[MapUtil.crosses.size()][MapUtil.crosses.size()];
		for(int i=0; i<MapUtil.crosses.size(); i++)
			for(int j=0; j<MapUtil.crosses.size(); j++) {
				Road road = computeRoad(MapUtil.crosses.get(MapUtil.crossSequence.get(i)),
						MapUtil.crosses.get(MapUtil.crossSequence.get(j)));
				if(road==null) {
					if(i==j)
						dist[i][j]=0;
					else dist[i][j] = MapUtil.FloatMax;
					path[i][j] = null;
				}
				else {
					int nowSpeed = Math.min(speed, road.getLimitSpeed());
					dist[i][j] = ((float) road.getLength())/nowSpeed;
					path[i][j] = road;
				}
			}
		
		for (int k=0; k<MapUtil.crosses.size(); k++) {
	        for (int i=0; i<MapUtil.crosses.size(); i++) {
	            for (int j=0; j<MapUtil.crosses.size(); j++) {
	            	float tmp = MapUtil.FloatMax;
	            	if(dist[i][k]!=MapUtil.FloatMax && dist[k][j]!=MapUtil.FloatMax)
	            		tmp = dist[i][k]+dist[k][j];
	                if (dist[i][j] > tmp) {
	                    dist[i][j] = tmp;
	                    // maybe error
	                    path[i][j] = path[i][k];
	                }
	            }
	        }
	    }
		pathMap.put(speed, path);
		distMap.put(speed, dist);
	}
	
	public static Road computeRoad(Cross crossOne, Cross crossTwo) {
		Road road = crossOne.findLinkedRoad(crossTwo);
		if(road==null)
			return null;
		if(road.getDestination()!=crossTwo && !road.isBiDirect())
			return null;
		return road;
	}
}
