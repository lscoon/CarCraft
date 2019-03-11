package com.huawei;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.huawei.handle.InputHandle;
import com.huawei.view.MapFrame;

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

        // TODO: calc

        // TODO: write answer.txt
        logger.info("Start write output file");

        logger.info("End...");*/
    	
    	
    	//logger info show program process
    	//logger error show program bugs
    	
    	logger.info("Start...");
    	
    	logger.info("start read input files");
    	InputHandle.readInputs();
    	logger.info("end read input files");
    	
		//RoadMap.printMapSize();
		MapFrame view = new MapFrame();
		logger.info("End...");
		
    }
}