package com.huawei.data;

import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;

import com.huawei.util.Util;

public class OneWayRoad {
	
	private int lanesNum;
	private int len;
	
	private int[][] status;
	
	public OneWayRoad(int lanesNumber, int length) {
		lanesNum = lanesNumber;
		len = length;
		status = new int[lanesNumber][length];
	}
	
	protected String showForwardStatus() {
		int carIdMaxLength = countNum(Util.CarIdMaxLength);
		String temp = "";
		for(int i=0; i<lanesNum; i++) {
			for(int j=0; j<len; j++) {
				int blankCount = carIdMaxLength-countNum(status[i][j]);
				for(int k=0; k<blankCount; k++)
					temp = temp.concat(" ");
				temp = temp.concat(status[i][j]+"");
			}
			temp = temp.concat("\n");
		}
		return temp;
	}
	
	protected String showBackwardStatus() {
		int carIdMaxLength = countNum(Util.CarIdMaxLength);
		String temp = "";
		for(int i=lanesNum-1; i>=0; i--) {
			for(int j=len-1; j>=0; j--) {
				int blankCount = carIdMaxLength-countNum(status[i][j]);
				for(int k=0; k<blankCount; k++)
					temp = temp.concat(" ");
				temp = temp.concat(status[i][j]+"");
			}
			temp = temp.concat("\n");
		}
		return temp;
	}
	
	private int countNum(int input) {
		if(input/10==0)
			return 1;
		else 
			return countNum(input/10) + 1;
	}
}
