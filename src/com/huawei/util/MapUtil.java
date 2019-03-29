package com.huawei.util;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.huawei.entity.Car;
import com.huawei.entity.CarFlow;
import com.huawei.entity.Cross;
import com.huawei.entity.Road;
import com.huawei.service.GlobalSolver;
import com.huawei.service.MapSimulator;
import com.huawei.ui.MapFrame;

public class MapUtil {
	
	private static final Logger logger = Logger.getLogger(MapUtil.class);
	public static final int IntMax = Integer.MAX_VALUE;
	public static final float FloatMax = Float.MAX_VALUE;
	public static final int roadIdMax = Integer.MAX_VALUE;
	
	// id max = 312 -> CarIdMaxLength = 1000
	// will init while handle input data
	public static int CarIdMaxLength = 10;
	public static int CarMinSpeed = 10;
	public static int CarMaxSpeed = 0;
	public static int CarFlowMaxCarCount = 0;
	
	public static int NowStartOffCarsAdd = 1;
	public static int DelayTerm = 0;
	public static double LoadParameter = 0.8;
	public static int RoadMaxLoad = 170;
	public static int RoadListMaxIncrease = 2;
	// when fail this nums, will not try judge overlay
	public static int MaxFailCount = 200;
	// when finish this nums carflows, will try to add carflows
	public static int MaxCarFlowFinishCount = 5;
	// select this num reverse carflows to fill map
	//public static int CarFlowCountLeaveForFill = 18;
	
	public static MapFrame mapView = null;
	
	public static Map<Integer, Cross> crosses = new HashMap<>();
	public static Map<Integer, Road> roads = new HashMap<>();
	public static Map<Integer, Car> cars = new HashMap<>();

	public static ArrayList<CarFlow> carFlows = new ArrayList<CarFlow>();
	
	// ordered sequence from min to max
	public static List<Integer> crossSequence = new ArrayList<>();
	
	public static void printMapSize() {
		System.out.println("map size: ");
		System.out.println(roads.size() + " roads");
		System.out.println(crosses.size() + " crosses");
		System.out.println(cars.size() + " cars");
	}
	
	public static void checkParameters() {
		if(CarFlowMaxCarCount > RoadMaxLoad) {
			logger.info("CarFlowMaxCarCount > RoadMaxLoad, maybe error");
		}
	}
	
	private static void clear() {
		CarIdMaxLength = 10;
		CarMinSpeed = 10;
		CarMaxSpeed = 0;
		crosses.clear();
		roads.clear();
		carFlows.clear();
		crossSequence.clear();
		MapSimulator.finishCars.clear();
		MapSimulator.term = DelayTerm;
	}
	
	private static void runFile(String arg) {
		Date start_time = new Date();
		String[] args = FileUtil.initFiles(arg);
		String carPath = args[0];
        String roadPath = args[1];
        String crossPath = args[2];
        String answerPath = args[3];
        FileUtil.readInputs(carPath, roadPath, crossPath);
        GlobalSolver.invokeSolver();
        MapSimulator.term = DelayTerm;
        MapSimulator.runMapWithCarFlow();
//    	MapSimulator.runMapWithCarFlowWithView();
    	FileUtil.outputAnswer(answerPath);
    	Date end_time = new Date();
        long timeDiff = end_time.getTime() - start_time.getTime();
        long count = 0;
        for(Car car : cars.values()) {
        	count += car.getRealEndTime()-car.getStartTime();
        }
        System.out.println(arg + ":   " + (MapSimulator.term-MapSimulator.realStartTerm) + ",   " + count + ",   "+ timeDiff);
	}
	
	private static void testAnswer(String arg) {
		String[] args = FileUtil.initFiles(arg);
		String carPath = args[0];
        String roadPath = args[1];
        String crossPath = args[2];
        String answerPath = args[3];
		FileUtil.readInputs(carPath, roadPath, crossPath);
		FileUtil.inputAnswer(answerPath);
        MapSimulator.term = DelayTerm;
//        Date test_start_time = new Date();
        MapSimulator.runMapWithOutView();
//        MapSimulator.runMapWithView();
//        Date test_end_time = new Date();
//        long timeDiff = test_end_time.getTime() - test_start_time.getTime();
//        logger.info("Take time " + timeDiff);
        logger.info("Dispatcher time: " + (MapSimulator.term-MapSimulator.realStartTerm));
        long count = 0;
        for(Car car : cars.values()) {
        	count += car.getRealEndTime()-car.getStartTime();
        }
        logger.info("Total time: " + count);
	}
		
		
	public static void main(String[] args) {
//		for(LoadParameter=0.1;LoadParameter<=2; LoadParameter += 0.1) {
//			System.out.pr
			clear();
			runFile("inputs/1-map-training-1/");
			clear();
			runFile("inputs/1-map-training-2/");
//		}
		clear();
		testAnswer("inputs/1-map-training-1/");
		clear();
		testAnswer("inputs/1-map-training-2/");
    }
}
