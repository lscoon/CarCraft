package com.huawei.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.huawei.entity.Car;
import com.huawei.entity.Cross;
import com.huawei.entity.Road;

public class FileUtil {
	
	private static final Logger logger = Logger.getLogger(FileUtil.class);
	private static String inputFilesPath = "";
	private static String roadFile = inputFilesPath + "road.txt";
	private static String carFile = inputFilesPath + "car.txt";
	private static String crossFile = inputFilesPath + "cross.txt";
	private static String answerFile = "outputs/answer.txt";
	
	public static void readInputs(String inputFiles) {
		inputFilesPath = inputFiles + File.separator;
		roadFile = inputFilesPath + "road.txt";
		carFile = inputFilesPath + "car.txt";
		crossFile = inputFilesPath + "cross.txt";
		inputCross();
		inputRoad();
		bindRoadToCross();
		inputCar();
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
		FileReader reader = null;
		BufferedReader br = null;
		try {
			String line = null;
			reader = new FileReader(crossFile);
			br = new BufferedReader(reader);
			while ((line = br.readLine()) != null) {
				if (line.length()==0 || line.startsWith("#"))
					continue;
				line = line.substring(1, line.length()-1);
				String[] crossStrings = line.split(",");
				int crossId = Integer.valueOf(crossStrings[0].trim()).intValue();
				MapUtil.crossSequence.add(crossId);
				MapUtil.crosses.put(crossId, new Cross(crossStrings));
			}
			Collections.sort(MapUtil.crossSequence);
			reader.close();
			br.close();
		} catch (IOException e) {
			logger.error("cross input stream problem");
			e.printStackTrace();
		} finally {
			if(null != reader){
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if(null != br){
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
		}
	}
	
	private static void inputRoad() {
		FileReader reader = null;
		BufferedReader br = null;
		try {
			String line = null;
			reader = new FileReader(roadFile);
			br = new BufferedReader(reader);
			while ((line = br.readLine()) != null) {
				if (line.length()==0 || line.startsWith("#"))
					continue;
				line = line.substring(1, line.length()-1);
				String[] roadStrings = line.split(",");
				MapUtil.roads.put(Integer.valueOf(roadStrings[0].trim()).intValue(), new Road(roadStrings));
			}
			reader.close();
			br.close();
		} catch (IOException e) {
			logger.error("road input stream problem");
			e.printStackTrace();
		} finally {
			if(null != reader){
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if(null != br){
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
		}
	}
	
	private static void bindRoadToCross() {
		Iterator<Map.Entry<Integer, Cross>> iterator = MapUtil.crosses.entrySet().iterator();
		while (iterator.hasNext()) {
		    Map.Entry<Integer, Cross> entry = iterator.next();
		    entry.getValue().initRoads();
		}
	}
	
	private static void inputCar() {
		FileReader reader = null;
		BufferedReader br = null;
		try {
			String line = null;
			reader = new FileReader(carFile);
			br = new BufferedReader(reader);
			while ((line = br.readLine()) != null) {
				if (line.length()==0 || line.startsWith("#"))
					continue;
				line = line.substring(1, line.length()-1);
				String[] carStrings = line.split(",");
				MapUtil.cars.put(Integer.valueOf(carStrings[0].trim()).intValue(), new Car(carStrings));
			}
			reader.close();
			br.close();
		} catch (IOException e) {
			logger.error("car input stream problem");
			e.printStackTrace();
		} finally {
			if(null != reader){
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if(null != br){
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
		}
	}

	private static void inputAnswer() {
		FileReader reader = null;
		BufferedReader br = null;
		try {
			reader = new FileReader(answerFile);
			br = new BufferedReader(reader);
			String line = null;
			while ((line = br.readLine()) != null) {
				if (line.length()==0 || line.startsWith("#"))
					continue;
				line = line.substring(1, line.length()-1);
				String[] answerStrings = line.split(",");
				int carId = Integer.valueOf(answerStrings[0].trim()).intValue();
				int realStTime = Integer.valueOf(answerStrings[1].trim()).intValue();
				List<Road> runRoadList = new LinkedList<Road>();
				for(int i=2; i<answerStrings.length; i++)
					runRoadList.add(MapUtil.roads.get(
							Integer.valueOf(answerStrings[i].trim()).intValue()));
				Car car = MapUtil.cars.get(carId);
				car.setRealStartTime(realStTime);
				car.setRoadList(runRoadList);
			}
			reader.close();
			br.close();
		} catch (IOException e) {
			logger.error("answer input stream problem");
			e.printStackTrace();
		} finally {
			if(null != reader){
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if(null != br){
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
		}
	}
	
	public static void outputAnswer(String answerPath) {
		//answerFile = answerPath;
		FileWriter output = null;
		BufferedWriter writer = null;
		try {
			output = new FileWriter(answerFile);
			writer = new BufferedWriter(output);
			
			Iterator<Map.Entry<Integer, Car>> iterator = MapUtil.cars.entrySet().iterator();
			while (iterator.hasNext()) {
			    Map.Entry<Integer, Car> entry = iterator.next();
			    String content = "(" + entry.getKey();
			    content = content.concat(", " + entry.getValue().getRealStartTime());
			    for(Road road : entry.getValue().getRoadList()) {
			    	if(road!=null)
			    		content = content.concat(", " +road.getRoadId());
			    	else{
			    		logger.error("exists error in road list");
			    		break;
			    	}
			    }
			    content = content.concat(")");
			    writer.write(content);
			}
		} catch (Exception e) {
			logger.error("answer output stream problem");
			e.printStackTrace();
		} finally {
			if(null != writer){
                try {
                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if(null != output){
                try {
                    output.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
		}
	}
}
