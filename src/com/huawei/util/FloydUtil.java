package com.huawei.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.huawei.entity.Car;
import com.huawei.entity.Cross;
import com.huawei.entity.Road;

public class FloydUtil {
	
	public static void 
	
	public static void getSpeedPathMatrix() {
		Map<Integer, Road[][]> matrixs = new HashMap<>();
		for(int i=MapUtil.CarMinSpeed; i<=MapUtil.CarIdMaxLength; i++)
			matrixs.put(i, floyd(i));
		
		for(Car car : MapUtil.cars.values()) {
			Road[][] matrix = matrixs.get(car.getMaxSpeed());
			int originSeq = MapUtil.crossSequence.indexOf(car.getOrigin());
			int destinationSeq = MapUtil.crossSequence.indexOf(car.getDestination());
			
			List<Road> roadList = new ArrayList<>();
			// to do
			roadList.add(matrix[originSeq][destinationSeq]);
		}
	}
	
	private static Road[][] floyd(int speed) {
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
					speed = Math.min(speed, road.getLimitSpeed());
					dist[i][j] = ((float) road.getLength())/speed;
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
		return path;
	}
	
	private static Road computeRoad(Cross crossOne, Cross crossTwo) {
		Road road = crossOne.findLinkedRoad(crossTwo);
		if(road==null)
			return null;
		if(road.getDestination()!=crossTwo && !road.isBiDirect())
			return null;
		return road;
	}
}
