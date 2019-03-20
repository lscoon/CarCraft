package com.huawei;

import java.io.File;

import org.apache.log4j.Logger;

import com.huawei.service.GlobalSolver;
import com.huawei.service.MapSimulator;
import com.huawei.ui.MapFrame;
import com.huawei.util.FileUtil;

public class Main {
    
	private static final Logger logger = Logger.getLogger(Main.class);
    
	public static void main(String[] args) {
        args = FileUtil.initFiles("inputs/1-map-training-2/");
		
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
        FileUtil.readInputs(carPath, roadPath, crossPath);
        
        // TODO: calc
        logger.info("start floyd init");
    	GlobalSolver.initCarRoadList();
    	logger.info("end floyd init");
    	
    	//MapSimulator.runMapWithView();
    	MapSimulator.runMapWithOutView();
    	
        // TODO: write answer.txt
        logger.info("Start write output file");

        logger.info("end in term " + MapSimulator.term);
        logger.info("End...");
    }
    
}