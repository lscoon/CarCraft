package com.huawei.handle;

import org.apache.log4j.Logger;

public class GlobalSolver {
	private static final Logger logger = Logger.getLogger(GlobalSolver.class);

	public GlobalSolver() {
		// To Do
	}

	public static void initSolver() {
		RoadMap.initMap();
		initCarRoadList();
	}

	public static void invokeSolver() {
		initSolver();

		while (!RoadMap.isDispatchFinished()) {
			RoadMap.updateMap();
			RoadMap.term++;
			updateCarRoadList();
		}
	}

	// shortest path road sequence
	public static void initCarRoadList() {
		// To Do
	}

	public static void updateCarRoadList() {
		// To Do
	}
}
