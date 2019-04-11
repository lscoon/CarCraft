package com.huawei.util;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import com.huawei.entity.Car;
import com.huawei.entity.CarFlow;
import com.huawei.entity.Cross;
import com.huawei.entity.Road;
import com.huawei.service.SolverWithFlow;
import com.huawei.service.Judge;
import com.huawei.service.JudgeWithFlow;
import com.huawei.ui.MapFrame;

public class MapUtil {
	
	private static final Logger logger = Logger.getLogger(MapUtil.class);
	public static final int IntMax = Integer.MAX_VALUE;
	public static final float FloatMax = Float.MAX_VALUE;
	public static final int roadIdMax = Integer.MAX_VALUE;
	
	// id max = 312 -> CarIdMaxLength = 1000
	// will init while handle input data
	public static int CarIdMaxLength = 10;
	
	public static int AllCarNum = 0;
	public static int PriorityCarNum = 0;
	public static int AllCarMinSpeed = IntMax;
	public static int AllCarMaxSpeed = 0;
	public static int PriorityCarMinSpeed = IntMax;
	public static int PriorityCarMaxSpeed = 0;
	public static int AllCarEarliestStartTime = IntMax;
	public static int AllCarLatestStartTime = 0;
	public static int PriorityCarEarliestStartTime = IntMax;
	public static int PriorityCarLatestStartTime = 0;
	public static int AllCarOriginDistribution = 0;
	public static int PriorityCarOriginDistribution = 0;
	public static int AllCarDestinationDistrubution = 0;
	public static int PriorityCarDestinationDistrubution = 0;
	
	public static int CarFlowMaxCarCount = 0;
	
//	public static double PresetParameter = 1;
	
	public static int NowStartOffCarsAdd = 1;
	public static int DelayTerm = 0;
	public static double LoadParameter = 0.8;
	public static int RoadMaxLoad = 250;
	public static int RoadListMaxIncrease = 2;
	// when fail this nums, will not try judge overlay
	public static int MaxFailCount = 100;
	public static int MaxPreSetFailCount = 50;
	// when finish this nums carflows, will try to add carflows
	public static int MaxCarFlowFinishCount = 2;
	
	public static int ExpectedFlowSize = 200;
	public static int SplitBeginOutRoadCarFlowNum = 20;
	public static int SplitFlowThreshhold = 10;
	public static int SelectedFlowNum = 3;
	
	public static MapFrame mapView = null;
	
	public static Map<Integer, Cross> crosses = new HashMap<>();
	public static Map<Integer, Road> roads = new HashMap<>();
	public static Map<Integer, Car> cars = new HashMap<>();

	public static ArrayList<CarFlow> carFlows = new ArrayList<CarFlow>();
	public static List<CarFlow> presetCarflow = new ArrayList<>();
	
	// ordered sequence from min to max
	public static List<Integer> crossSequence = new ArrayList<>();
	
	public static void printMapSize() {
		System.out.println("map size: ");
		System.out.println(roads.size() + " roads");
		System.out.println(crosses.size() + " crosses");
		System.out.println(cars.size() + " cars");
	}
	
	public static void computeResult() {
		Set<Integer> allCarOriginDistributionSet = new HashSet<>();
		Set<Integer> priorityCarOriginDistributionSet = new HashSet<>();
		Set<Integer> allCarDestinationDistributionSet = new HashSet<>();
		Set<Integer> priorityCarDestinationDistributionSet = new HashSet<>();
		
		int t = 0;
		int t_earliest = IntMax;
		int t_latest = 0;
		int t_sum = 0;
		int t_priority = 0;
		int t_priority_earliest = IntMax;
		int t_priority_latest = 0;
		int t_priority_sum = 0;
		double a = 0;
		double b = 0;
		
		for(Car car: cars.values()) {
			if(!allCarOriginDistributionSet.contains(car.getOrigin()))
				allCarOriginDistributionSet.add(car.getOrigin());
			if(!allCarDestinationDistributionSet.contains(car.getDestination()))
				allCarDestinationDistributionSet.add(car.getDestination());
			if(car.getStartTime() < t_earliest)
				t_earliest = car.getStartTime();
			if(car.getRealEndTime() > t_latest)
				t_latest = car.getRealEndTime();
			t_sum += (car.getRealEndTime()-car.getStartTime());
			
			if(car.isPriority()) {
				if(!priorityCarOriginDistributionSet.contains(car.getOrigin()))
					priorityCarOriginDistributionSet.add(car.getOrigin());
				if(!priorityCarDestinationDistributionSet.contains(car.getDestination()))
					priorityCarDestinationDistributionSet.add(car.getDestination());
				if(car.getStartTime() < t_priority_earliest)
					t_priority_earliest = car.getStartTime();
				if(car.getRealEndTime() > t_priority_latest)
					t_priority_latest = car.getRealEndTime();
				t_priority_sum += (car.getRealEndTime() - car.getStartTime());
			}
		}
		t = t_latest - t_earliest + 1;
		t_priority = t_priority_latest - t_priority_earliest;
		AllCarOriginDistribution = allCarDestinationDistributionSet.size();
		AllCarDestinationDistrubution = allCarDestinationDistributionSet.size();
		PriorityCarOriginDistribution = priorityCarOriginDistributionSet.size();
		PriorityCarDestinationDistrubution = priorityCarDestinationDistributionSet.size();
		
		a = 0.05*AllCarNum/PriorityCarNum + 
			0.2375*AllCarMaxSpeed/AllCarMinSpeed/(PriorityCarMaxSpeed/PriorityCarMinSpeed) +
			0.2375*AllCarLatestStartTime/AllCarEarliestStartTime/(PriorityCarLatestStartTime/PriorityCarEarliestStartTime) +
			0.2375*AllCarOriginDistribution/PriorityCarOriginDistribution +
			0.2375*AllCarDestinationDistrubution/PriorityCarDestinationDistrubution;
		
		b = 0.8*AllCarNum/PriorityCarNum + 
			0.05*AllCarMaxSpeed/AllCarMinSpeed/(PriorityCarMaxSpeed/PriorityCarMinSpeed) +
			0.05*AllCarLatestStartTime/AllCarEarliestStartTime/(PriorityCarLatestStartTime/PriorityCarEarliestStartTime) +
			0.05*AllCarOriginDistribution/PriorityCarOriginDistribution +
			0.05*AllCarDestinationDistrubution/PriorityCarDestinationDistrubution;
		
		int t_e = (int)(a*t_priority) + t;
		int t_e_sum = (int)(b*t_priority_sum) + t_sum;
//		logger.info("t_pri: " + t_priority + ", t: " + t);
//		logger.info("t_pri_sum: " + t_priority_sum + ", t_sum: " + t_sum);
		logger.info("t_e: " + t_e + ", t_e_sum: " + t_e_sum);
	}
	
	private static void clear() {
		CarIdMaxLength = 10;
		AllCarMinSpeed = 10;
		AllCarMaxSpeed = 0;
		crosses.clear();
		roads.clear();
		cars.clear();
		carFlows.clear();
		crossSequence.clear();
		FloydUtil.pathMap = null;
		FloydUtil.distMap = null;
		DijkstraUtil.dist = null;
	}
	
	private static void runFile(String arg) {
		Date start_time = new Date();
		String[] args = FileUtil.initFiles(arg);
		String carPath = args[0];
        String roadPath = args[1];
        String crossPath = args[2];
        String presetAnswerPath = args[3];
        String answerPath = args[4];
        FileUtil.readInputs(carPath, roadPath, crossPath, presetAnswerPath);
        
        FloydUtil.initPathAndDistMatrixMap();
        SolverWithFlow.initCarClusters();
        JudgeWithFlow judgeWithFlow = new JudgeWithFlow(DelayTerm);
//        for(Car car : cars.values())
//        	if(car.getCarId()==43693 || car.getCarId()==46284 ||car.getCarId()==92227) {
//        		String string = "";
//        		for(Road road : car.getCarFlow().getRoadList())
//        			string = string.concat(road.getRoadId() + ",");
//        		logger.info(car.getCarId() + ":" + string);
//        	}
        judgeWithFlow.runWithoutView();
//        judgeWithFlow.runWithView();
        
    	FileUtil.outputAnswer(answerPath);
    	Date end_time = new Date();
        long timeDiff = end_time.getTime() - start_time.getTime();
        logger.info("timeDiff: "+ timeDiff);
        computeResult();
	}
	
	private static void testAnswer(String arg) {
		String[] args = FileUtil.initFiles(arg);
		String carPath = args[0];
        String roadPath = args[1];
        String crossPath = args[2];
        String presetAnswerPath = args[3];
        String answerPath = args[4];
		FileUtil.readInputs(carPath, roadPath, crossPath, presetAnswerPath);
		FileUtil.inputAnswer(answerPath);
		Judge judge = new Judge(DelayTerm);
//        Date test_start_time = new Date();
		judge.runWithoutView();
//        judge.runWithView();
//        Date test_end_time = new Date();
//        long timeDiff = test_end_time.getTime() - test_start_time.getTime();
//        logger.info("Take time " + timeDiff);
		computeResult();
	}
		
		
	public static void main(String[] args) {
//		logger.info(NowStartOffCarsAdd+","+LoadParameter+","+RoadMaxLoad+","+RoadListMaxIncrease+","+MaxFailCount+","+MaxCarFlowFinishCount);
//		clear();
//		runFile("maps/2-map-training-1/");
		clear();
		runFile("maps/2-map-training-2/");
//		clear();
//		testAnswer("maps/2-map-training-1/");
//		clear();
//		testAnswer("maps/2-map-training-2/");
    }
}
