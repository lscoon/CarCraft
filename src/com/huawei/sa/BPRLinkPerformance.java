package com.huawei.sa;


import com.huawei.entity.Car;
import com.huawei.entity.OneWayRoad;
import com.huawei.entity.Road;
import com.huawei.util.FloydUtil;
import com.huawei.util.MapUtil;


public class BPRLinkPerformance {
	public static float beta = 4.0f;
	public static float alphaDivisor = 10.0f;
	
	public static float[][] AdjMatrixR = new float[MapUtil.crosses.size()][MapUtil.crosses.size()];
	
	
	/*
	 * 公式：t = t_0*(1 + j*(q/c))
	 */
	public static float getLinkResistance(Car car, Road road, int fromIndex) {
		float[][] dist = FloydUtil.distMap.get(car.getMaxSpeed());
		Road[][] pathMatrix = FloydUtil.pathMap.get(car.getMaxSpeed());
		
		// 使用fromIndex区分车从road的哪一端上路,fromIndex为MapUtil中cross的序号，并非正式crossId
		
		int fromId = MapUtil.crosses.get(MapUtil.crossSequence.get(fromIndex)).getCrossId();
		int toId = road.getAnOtherCross(fromId);
		int toIndex = MapUtil.crossSequence.indexOf(toId);
		
		OneWayRoad oneWayRoad = null;
		if(road.getOrigin().getCrossId() == fromId)
			oneWayRoad = road.getForwardRoad();
		else oneWayRoad = road.getBackwardRoad();
		
		/* 此处需要调用函数，参数为Road和toIndex*/
		int outCarNum = oneWayRoad.outLoad;	//获取(term-1)时间片从road的to端出去的车数目
		
		/* 此处需要调用函数，参数为Road和fromIndex*/
		int inCarNum = oneWayRoad.inLoad; //获取(term-1)时间片从road的from端尝试进入的车数目
		
		
		float alpha = 1.0f - (float)road.getLanesNum()/alphaDivisor;
		
		// free-flow travel time on link a per unit of time
		float t_0 = (float)road.getLength()/((float)(Math.min(road.getLimitSpeed(), car.getMaxSpeed())));
		
		float inOutRatio = (float)inCarNum/((float)outCarNum + 1.0f);
		float result = (t_0*(1.0f + alpha*(float)Math.pow(inOutRatio, beta)));
		
		//newly add code
		int destIndex = MapUtil.crossSequence.indexOf(car.getDestination());
		float res = dist[toIndex][destIndex];
		float roadLength = 0;
		while (toIndex != destIndex) {
			Road road1 = pathMatrix[toIndex][destIndex];
			roadLength += 1.0f;
			// get another road cross sequence
			toIndex = MapUtil.crossSequence.indexOf(road1.getAnOtherCross(MapUtil.crossSequence.get(toIndex)));
		}
		if(roadLength<=3)
			return res;
		else return result + (res < 0.1?0:res);
	}

	/*
	 * 公式：t = t_0*(1 + j*(q/c))
	 */
	public static float getLinkResistance(Road road, int fromIndex) {
		// 使用fromIndex区分车从road的哪一端上路,fromIndex为MapUtil中cross的序号，并非正式crossId
	
		int fromId = MapUtil.crosses.get(MapUtil.crossSequence.get(fromIndex)).getCrossId();
		int toId = road.getAnOtherCross(fromId);
		int toIndex = MapUtil.crossSequence.indexOf(toId);
		
		OneWayRoad oneWayRoad = null;
		if(road.getOrigin().getCrossId() == fromId)
			oneWayRoad = road.getForwardRoad();
		else oneWayRoad = road.getBackwardRoad();
		
		if(oneWayRoad==null)
			return MapUtil.FloatMax;
		
		/* 此处需要调用函数，参数为Road和toIndex*/
		int outCarNum = oneWayRoad.outLoad;	//获取(term-1)时间片从road的to端出去的车数目
		
		/* 此处需要调用函数，参数为Road和fromIndex*/
		int inCarNum = oneWayRoad.inLoad; //获取(term-1)时间片从road的from端尝试进入的车数目
			
		float alpha = 1.0f - (float)road.getLanesNum()/alphaDivisor;
		
		float avgSpeed = Math.min(road.getLimitSpeed(), (MapUtil.AllCarMaxSpeed+MapUtil.AllCarMinSpeed)/2);
		// free-flow travel time on link a per unit of time
		float t_0 = (float)road.getLength()/avgSpeed;
		
		float inOutRatio = 0.0f;
		if(outCarNum == 0) {
			inOutRatio=0.0f;
		}else {
			inOutRatio = (float)inCarNum/((float)outCarNum);
		}
		float result = t_0*(1.0f + alpha*(float)Math.pow(inOutRatio, beta));
		
		return result;
	}

	
	//初始化必须在FloydUtil之后进行初始化
	public static void initAdjMatrixR() {
		AdjMatrixR = new float[MapUtil.crosses.size()][MapUtil.crosses.size()];
		int avgSpeed = (MapUtil.AllCarMaxSpeed + MapUtil.AllCarMinSpeed)/2;
		//获取平均速度下弗洛伊德距离矩阵
		float[][] dist = FloydUtil.distMap.get(avgSpeed);
		
		for(int i=0; i<MapUtil.crosses.size(); i++) {
			for(int j=0; j<MapUtil.crosses.size(); j++) {
				Road road = FloydUtil.computeRoad(MapUtil.crosses.get(MapUtil.crossSequence.get(i)),
						MapUtil.crosses.get(MapUtil.crossSequence.get(j)));
				if(road != null) {
					AdjMatrixR[i][j] = dist[i][j];
				}else {
					//road == null;
					if(i == j) {
						AdjMatrixR[i][j] = 0;
					}else {
						AdjMatrixR[i][j] = MapUtil.FloatMax;
					}
				}
				
			}
		}
	}
	
	public static void updateAdjMatrixR() {
		
		for(Road road:MapUtil.roads.values()) {
			int fromIndex = MapUtil.crossSequence.indexOf(road.getOrigin().getCrossId());
			int toIndex = MapUtil.crossSequence.indexOf(road.getDestination().getCrossId());
			
			AdjMatrixR[fromIndex][toIndex] = getLinkResistance(road, fromIndex);
			AdjMatrixR[toIndex][fromIndex] = getLinkResistance(road, toIndex);
		}
		
//		//第二种实现
//		for(int i=0; i<MapUtil.crosses.size(); i++) {
//			for(int j=0; j<MapUtil.crosses.size(); j++) {
//				Road road = FloydUtil.computeRoad(MapUtil.crosses.get(MapUtil.crossSequence.get(i)),
//						MapUtil.crosses.get(MapUtil.crossSequence.get(j)));
//				if(road != null) {
//					AdjMatrixR[i][j] = getLinkResistance(road, i);
//				}
//				
//			}
//		}
	}
}
