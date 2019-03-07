package data;

import java.util.LinkedHashSet;
import java.util.Set;

public class Map {
	
	public static Set<Road> roads = new LinkedHashSet<Road>();
	public static Set<Cross> crosses = new LinkedHashSet<Cross>();
	public static Set<Car> cars = new LinkedHashSet<Car>();
	
	
	public static void printMapSize() {
		System.out.println("map size: ");
		System.out.println(roads.size() + " roads");
		System.out.println(crosses.size() + " crosses");
		System.out.println(cars.size() + " cars");
		
	}
}
