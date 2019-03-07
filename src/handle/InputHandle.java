package handle;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import data.Car;
import data.Cross;
import data.Map;
import data.Road;

public class InputHandle {
	
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
				Map.roads.add(new Road(roadStrings));
			}
			reader.close();
			br.close();
		}
		catch (IOException e) {
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
				Map.cars.add(new Car(carStrings));
			}
			reader.close();
			br.close();
		}
		catch (IOException e) {
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
				Map.crosses.add(new Cross(crossStrings));
			}
			reader.close();
			br.close();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		readInputs();
		Map.printMapSize();
	}

}
