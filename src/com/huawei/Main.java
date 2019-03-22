package com.huawei;

import org.apache.log4j.Logger;

import com.huawei.service.GlobalSolver;
import com.huawei.service.MapSimulator;
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
        
        
    	GlobalSolver.invokeSolver();
    	logger.info("end in term " + MapSimulator.term + "\n");
    	//MapSimulator.runMapWithView();
    	//MapSimulator.runMapWithOutView();
    	
        // TODO: write answer.txt
        logger.info("Start write output file");
        FileUtil.outputAnswer(answerPath);
        logger.info("End...");
        
        testAnswer();
    }
	
	// Simulator test 
	private static void testAnswer() {
		logger.info("start test answer");
        FileUtil.inputAnswer();
        MapSimulator.term = 0;
        MapSimulator.runMapWithOutView();
        logger.info("end in term " + MapSimulator.term);
	}
    
}