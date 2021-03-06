package com.huawei.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import org.apache.log4j.Logger;

import com.huawei.entity.Cross;
import com.huawei.entity.Road;
import com.huawei.sa.BPRLinkPerformance;

public class DijkSAUtil {

	private static final Logger logger = Logger.getLogger(DijkSAUtil.class);
	public static float[][] dist = null;
	
	private static void refreshDistMatrix(int speed) {
		if(dist==null)
			dist = new float[MapUtil.crosses.size()][MapUtil.crosses.size()];
		for(int i=0; i<MapUtil.crosses.size(); i++)
			for(int j=0; j<MapUtil.crosses.size(); j++) {
				Road road = existComputeRoad(MapUtil.crosses.get(MapUtil.crossSequence.get(i)),
						MapUtil.crosses.get(MapUtil.crossSequence.get(j)));
				if(road==null) {
					if(i==j)
						dist[i][j]=0;
					else dist[i][j] = MapUtil.FloatMax;
				}
				else {
					int nowSpeed = Math.min(speed, road.getLimitSpeed());
					dist[i][j] = ((float) road.getLength())/nowSpeed;
				}
			}
	}
	
	private static Road existComputeRoad(Cross crossOne, Cross crossTwo) {
		Road road = crossOne.findLinkedRoad(crossTwo);
		if (road == null)
			return null;
		if (road.getDestination() != crossTwo && !road.isBiDirect())
			return null;
		return road;
	}
	
	// tag=0 means exist, tag=1 means fill
	public static List<Road> Dijkstra(int crossOneId, int crossTwoId, int speed) {
//		refreshDistMatrix(speed);
		dist = BPRLinkPerformance.AdjMatrixR;
		
		int startSeq = MapUtil.crossSequence.indexOf(crossOneId);
		int endSeq = MapUtil.crossSequence.indexOf(crossTwoId);
		int nearSeq = -1;
		
		float[] mindist = new float[MapUtil.crosses.size()];
		//Road[] path = new Road[MapUtil.crosses.size()];
		int[] prenode = new int[MapUtil.crosses.size()];
		boolean[] find = new boolean[MapUtil.crosses.size()];
		
		for(int i=0; i< MapUtil.crosses.size(); i++) {
			prenode[i] = startSeq; 
			find[i] = false;
			mindist[i] = dist[startSeq][i];
		}
		find[startSeq] = true;
		
		
		int newNearSeq;
		// i=1 because each iter we will find one min dist, and startSeq has been finded
		for(int v=1; v<MapUtil.crosses.size(); v++) {
			
			float min = MapUtil.FloatMax;
			newNearSeq = nearSeq;
			for (int j=0; j<MapUtil.crosses.size(); j++) {
				if(!find[j] && mindist[j]<min) {
					min = mindist[j];
					newNearSeq = j;
				}
			}
			if(min != MapUtil.FloatMax)
				find[newNearSeq] = true;
			if(newNearSeq==nearSeq)
				break;
			if(newNearSeq==endSeq)
				break;
			else nearSeq = newNearSeq;
			
			for (int k=0; k<MapUtil.crosses.size(); k++) {
				if(!find[k] && (min+dist[nearSeq][k])<mindist[k]) {
					prenode[k] = nearSeq;
					mindist[k] = min + dist[nearSeq][k];
				}
			}
		}
		if(find[endSeq]==false)
			return null;
		Stack<Road> roads = new Stack<>();
		while(endSeq != startSeq) {
			Cross cross1 = MapUtil.crosses.get(MapUtil.crossSequence.get(prenode[endSeq]));
			Cross cross2 = MapUtil.crosses.get(MapUtil.crossSequence.get(endSeq));
			Road road = existComputeRoad(cross1, cross2);
			if(road==null)
				logger.error("some thing wrong in dijkstra");
			roads.push(road);
			endSeq = prenode[endSeq];
		}
		List<Road> roadList = new ArrayList<>();
		while(!roads.isEmpty())
			roadList.add(roads.pop());
		return roadList;
	}
}
