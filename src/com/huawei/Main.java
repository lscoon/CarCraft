package com.huawei;

import org.apache.log4j.Logger;

import com.huawei.service.GlobalSolver;
import com.huawei.service.MapSimulator;
import com.huawei.ui.MapFrame;
import com.huawei.util.FileUtil;

public class Main {
    private static final Logger logger = Logger.getLogger(Main.class);
    public static void main(String[] args)
    {
        /*
    	if (args.length != 4) {
            logger.error("please input args: inputFilePath, resultFilePath");
            return;
        }

        logger.info("Start...");

        String carPath = args[0];
        String roadPath = args[1];
        String crossPath = args[2];
        String answerPath = args[3];
        logger.info("carPath = " + carPath + " roadPath = " + roadPath + " crossPath = " + crossPath + " and answerPath = " + answerPath);

        // TODO:read input files
        logger.info("start read input files");
        
        InputHandle.readInputs(carPath, roadPath, crossPath);
        
        // TODO: calc
        MapFrame view = new MapFrame();
        // TODO: write answer.txt
        logger.info("Start write output file");

        logger.info("End...");
    	
    	//logger info show program process
    	//logger error show program bugs
    	*/
    	logger.info("Start...");
    	
    	logger.info("start read input files");
    	FileUtil.readInputs("inputs/config");
    	logger.info("end read input files");
    	
    	logger.info("start floyd init");
    	GlobalSolver.initCarRoadList();
    	logger.info("end floyd init");
    	
		MapSimulator.runMapWithView();
		logger.info("End...");
		
    }
}