package com.huawei.handle;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.log4j.Logger;

import com.huawei.data.Car;
import com.huawei.data.Cross;
import com.huawei.data.Road;
import com.huawei.util.Util;

public class RoadMap {

	private static final Logger logger = Logger.getLogger(RoadMap.class);

	public static Map<Integer, Cross> crosses = new HashMap<>();
	public static Map<Integer, Road> roads = new HashMap<>();
	public static Map<Integer, Car> cars = new HashMap<>();

	// ordered sequence from min to max
	public static List<Integer> crossSequence = new ArrayList<>();

	public static int term = 0;
	public static List<Car> outRoadCars = new LinkedList<>();
	public static Set<Car> nowRunCars = new HashSet<>();
	public static Set<Car> finishCars = new HashSet<>();

	public static Set<Car> nowWaitedCars = new HashSet<>();

	public static void runMapWithOutView() {
		initMap();
		while (finishCars.size() != cars.size()) {
			updateMap();
			term++;
		}
	}

	public static void initMap() {
		logger.info("init map in " + term);
		for (Car i : cars.values())
			outRoadCars.add(i);
		Collections.sort(outRoadCars, new Comparator<Car>() {
			@Override
			public int compare(Car o1, Car o2) {
				return o1.getCarId() - o2.getCarId();
			}
		});
	}

	public static void updateMap() {
		logger.info("start updateMap in " + term);
		nowWaitedCars.addAll(nowRunCars);
		for (Car car : nowWaitedCars) {
			car.setWaited(true);
		}
		// step one
		// only consider and update cars that will not pass the cross
		Iterator<Map.Entry<Integer, Road>> iterator = roads.entrySet().iterator();
		while (iterator.hasNext()) {
			Map.Entry<Integer, Road> entry = iterator.next();
			entry.getValue().updateRunnableCars();
		}
		// step two
		while (nowWaitedCars.size() != 0) {
			for (int i = 0; i < crossSequence.size(); i++) {
				Cross cross = crosses.get(crossSequence.get(i));
				cross.updateCross();
			}
		}
		// step three
		for (int i = 0; i < outRoadCars.size(); i++) {
			Car car = outRoadCars.get(i);
			if (car.getStartTime() <= term)
				if (car.startOff())
					i--;
		}
	}

	public static void printMapSize() {
		System.out.println("map size: ");
		System.out.println(roads.size() + " roads");
		System.out.println(crosses.size() + " crosses");
		System.out.println(cars.size() + " cars");
	}

	public static boolean isDispatchFinished() {
		return finishCars.size() == cars.size();
	}
	
	public static void getSpeedPathMatrix() {
		Map<Integer, Road[][]> matrixs = new HashMap<>();
		for(int i=Util.CarMinSpeed; i<=Util.CarIdMaxLength; i++)
			matrixs.put(i, floyd(i));
		
		for(Car car : cars.values()) {
			Road[][] matrix = matrixs.get(car.getMaxSpeed());
			int originSeq = crossSequence.indexOf(car.getOrigin());
			int destinationSeq = crossSequence.indexOf(car.getDestination());
			
			List<Road> roadList = new ArrayList<>();
			// to do
			roadList.add(matrix[originSeq][destinationSeq]);
		}
	}
	
	private static Road[][] floyd(int speed) {
		Road[][] path = new Road[crosses.size()][crosses.size()];
		float[][] dist = new float[crosses.size()][crosses.size()];
		for(int i=0; i<crosses.size(); i++)
			for(int j=0; j<crosses.size(); j++) {
				Road road = computeRoad(crosses.get(crossSequence.get(i)),
						crosses.get(crossSequence.get(j)));
				if(road==null) {
					if(i==j)
						dist[i][j]=0;
					else dist[i][j] = Util.FloatMax;
					path[i][j] = null;
				}
				else {
					speed = Math.min(speed, road.getLimitSpeed());
					dist[i][j] = ((float) road.getLength())/speed;
					path[i][j] = road;
				}
			}
		
		for (int k=0; k<crosses.size(); k++) {
	        for (int i=0; i<crosses.size(); i++) {
	            for (int j=0; j<crosses.size(); j++) {
	            	float tmp = Util.FloatMax;
	            	if(dist[i][k]!=Util.FloatMax && dist[k][j]!=Util.FloatMax)
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
