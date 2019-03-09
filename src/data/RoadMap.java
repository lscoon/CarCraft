package data;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public class RoadMap {
	
	public static Map<Integer, Road> roads = new LinkedHashMap<Integer, Road>();
	public static Map<Integer, Cross> crosses = new LinkedHashMap<Integer, Cross>();
	public static Map<Integer, Car> cars = new LinkedHashMap<Integer, Car>();
	
	public static void printMapSize() {
		System.out.println("map size: ");
		System.out.println(roads.size() + " roads");
		System.out.println(crosses.size() + " crosses");
		System.out.println(cars.size() + " cars");
	}
}
