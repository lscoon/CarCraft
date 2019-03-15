package com.huawei.handle;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.huawei.data.Car;
import com.huawei.data.Cross;
import com.huawei.data.Road;
import com.huawei.data.RoadMap;

public class InputHandle {
	
	private static final Logger logger = Logger.getLogger(InputHandle.class);
	private static String inputFilesPath = "";
	private static String roadFile = inputFilesPath + "road.txt";
	private static String carFile = inputFilesPath + "car.txt";
	private static String crossFile = inputFilesPath + "cross.txt";
	private static String answerFile = inputFilesPath + "answer.txt";
	
	public static void readInputs(String inputFiles) {
		inputFilesPath = inputFiles + File.separator;
		roadFile = inputFilesPath + "road.txt";
		carFile = inputFilesPath + "car.txt";
		crossFile = inputFilesPath + "cross.txt";
		answerFile = inputFilesPath + "answer.txt";
		inputCross();
		inputRoad();
		bindRoadToCross();
		
		inputCar();
		inputAnswer();
	}
	
	public static void readInputs(String carPath, String roadPath, String crossPath) {
		crossFile = crossPath;
		roadFile = roadPath;
		carFile = carPath;
		inputCross();
		inputRoad();
		bindRoadToCross();
		
		inputCar();
	}
	
	private static void inputCross() {
		try {
			String line = null;
			FileReader reader = new FileReader(crossFile);
			BufferedReader br = new BufferedReader(reader);
			while ((line = br.readLine()) != null) {
				if (line.length()==0 || line.startsWith("#"))
					continue;
				line = line.substring(1, line.length()-1);
				String[] crossStrings = line.split(",");
				int crossId = Integer.valueOf(crossStrings[0].trim()).intValue();
				RoadMap.crossSequence.add(crossId);
				RoadMap.crosses.put(crossId, new Cross(crossStrings));
			}
			Collections.sort(RoadMap.crossSequence);
			reader.close();
			br.close();
		}
		catch (IOException e) {
			logger.error("cross input stream problem");
			e.printStackTrace();
		}
	}
	
	private static void inputRoad() {
		try {
			String line = null;
			FileReader reader = new FileReader(roadFile);
			BufferedReader br = new BufferedReader(reader);
			while ((line = br.readLine()) != null) {
				if (line.length()==0 || line.startsWith("#"))
					continue;
				line = line.substring(1, line.length()-1);
				String[] roadStrings = line.split(",");
				RoadMap.roads.put(Integer.valueOf(roadStrings[0].trim()).intValue(), new Road(roadStrings));
			}
			reader.close();
			br.close();
		}
		catch (IOException e) {
			logger.error("road input stream problem");
			e.printStackTrace();
		}
	}
	
	private static void bindRoadToCross() {
		Iterator<Map.Entry<Integer, Cross>> iterator = RoadMap.crosses.entrySet().iterator();
		while (iterator.hasNext()) {
		    Map.Entry<Integer, Cross> entry = iterator.next();
		    entry.getValue().initRoads();
		}
	}
	
	private static void inputCar() {
		try {
			String line = null;
			FileReader reader = new FileReader(carFile);
			BufferedReader br = new BufferedReader(reader);
			while ((line = br.readLine()) != null) {
				if (line.length()==0 || line.startsWith("#"))
					continue;
				line = line.substring(1, line.length()-1);
				String[] carStrings = line.split(",");
				RoadMap.cars.put(Integer.valueOf(carStrings[0].trim()).intValue(), new Car(carStrings));
			}
			reader.close();
			br.close();
		}
		catch (IOException e) {
			logger.error("car input stream problem");
			e.printStackTrace();
		}
	}

	private static void inputAnswer() {
		try {
			String line = null;
			FileReader reader = new FileReader(answerFile);
			BufferedReader br = new BufferedReader(reader);
			while ((line = br.readLine()) != null) {
				if (line.length()==0 || line.startsWith("#"))
					continue;
				line = line.substring(1, line.length()-1);
				String[] answerStrings = line.split(",");
				int carId = Integer.valueOf(answerStrings[0].trim()).intValue();
				int realStTime = Integer.valueOf(answerStrings[1].trim()).intValue();
				List<Road> runRoadList = new LinkedList<Road>();
				for(int i=2; i<answerStrings.length; i++)
					runRoadList.add(RoadMap.roads.get(
							Integer.valueOf(answerStrings[i].trim()).intValue()));
				Car car = RoadMap.cars.get(carId);
				car.setRealStTime(realStTime);
				car.setRunRoadList(runRoadList);
			}
			reader.close();
			br.close();
		}
		catch (IOException e) {
			logger.error("answer input stream problem");
			e.printStackTrace();
		}
	}
	
}
