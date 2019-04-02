package com.huawei;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.log4j.Logger;

import com.huawei.service.SolverWithFlow;
import com.huawei.service.JudgeWithFlow;
import com.huawei.util.FileUtil;
import com.huawei.util.FloydUtil;
import com.huawei.util.MapUtil;

public class Main {
    
	private static final Logger logger = Logger.getLogger(Main.class);
    
	public static void main(String[] args) {
//		args = FileUtil.initFiles("inputs/1-map-training-1/");
		
    	if (args.length != 4) {
            logger.error("please input args: inputFilePath, resultFilePath");
            return;
        }
    	
        logger.info("Start...");
        Date start_time = new Date();
    	SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//    	String s_start_time = df.format(start_time);

        String carPath = args[0];
        String roadPath = args[1];
        String crossPath = args[2];
        String answerPath = args[3];
        logger.info("carPath = " + carPath + " roadPath = " + roadPath + " crossPath = " + crossPath + " and answerPath = " + answerPath);

        // TODO:read input files
        logger.info("start read input files");
        FileUtil.readInputs(carPath, roadPath, crossPath);
        
        FloydUtil.initPathAndDistMatrixMap();
        SolverWithFlow.initCarClusters();
        JudgeWithFlow judgeWithFlow = new JudgeWithFlow(MapUtil.DelayTerm);
        judgeWithFlow.runWithoutView();
    	
    	logger.info("End in term " + judgeWithFlow.getTerm());
        // TODO: write answer.txt
        logger.info("Start write output file");
        FileUtil.outputAnswer(answerPath);
        
        Date end_time = new Date();
        long timeDiff = end_time.getTime() - start_time.getTime();
        logger.info("Take time " + timeDiff);
        
        logger.info("End...");
    }
    
}