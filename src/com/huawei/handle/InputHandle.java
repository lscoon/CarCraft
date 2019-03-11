package com.huawei.handle;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import org.apache.log4j.Logger;

import com.huawei.data.Car;
import com.huawei.data.Cross;
import com.huawei.data.Road;
import com.huawei.data.RoadMap;
import com.huawei.view.MapPanel;

public class InputHandle {
	
	private static final Logger logger = Logger.getLogger(InputHandle.class);
	private static final String inputFilePath = "inputs" + File.separator;
	private static final String roadFile = inputFilePath + "Road.txt";
	private static final String carFile = inputFilePath + "Car.txt";
	private static final String crossFile = inputFilePath + "Cross.txt";
	
	public static void readInputs() {
		inputRoad();
		inputCar();
		inputCross();
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
				RoadMap.crosses.put(Integer.valueOf(crossStrings[0].trim()).intValue(), new Cross(crossStrings));
			}
			reader.close();
			br.close();
		}
		catch (IOException e) {
			logger.error("cross input stream problem");
			e.printStackTrace();
		}
	}

}
