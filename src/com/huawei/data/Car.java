package com.huawei.data;

import org.apache.log4j.Logger;

import com.huawei.util.Util;
import com.huawei.view.MapPanel;

/**
 * @author sc
 *
 */

public class Car {
	
	private static final Logger logger = Logger.getLogger(Car.class);
	
	private int carId;
	private int origin;
	private int destination;
	private int maxSpeed;
	private int startTime;
	
	private int realStTime = 0;
	private int realEndTime = 0;
	
	// now speed = 0 means in jammed status
	private int nowSpeed = 0;
	private int nowRoad = 0;
	private boolean isUpdated = false;
	
	public Car (String[] strs) {
		if (strs.length != 5) {
			logger.error("car create format error: " + strs);
			return;
		}
		carId = Integer.valueOf(strs[0].trim()).intValue();
		origin = Integer.valueOf(strs[1].trim()).intValue();
		destination = Integer.valueOf(strs[2].trim()).intValue();
		maxSpeed = Integer.valueOf(strs[3].trim()).intValue();
		startTime = Integer.valueOf(strs[4].trim()).intValue();
		
		if(carId/Util.CarIdMaxLength > 0)
			Util.CarIdMaxLength = Util.CarIdMaxLength*10;
	}
	
	public int getCarId() {
		return carId;
	}
	public void setCarId(int id) {
		this.carId = id;
	}
	public int getOrigin() {
		return origin;
	}
	public void setOrigin(int origin) {
		this.origin = origin;
	}
	public int getDestination() {
		return destination;
	}
	public void setDestination(int destination) {
		this.destination = destination;
	}
	public int getMaxSpeed() {
		return maxSpeed;
	}
	public void setMaxSpeed(int speed) {
		this.maxSpeed = speed;
	}
	public int getStartTime() {
		return startTime;
	}
	public void setStartTime(int beginTime) {
		this.startTime = beginTime;
	}
	
	public int getRealStTime() {
		return realStTime;
	}

	public void setRealStTime(int realStTime) {
		this.realStTime = realStTime;
	}
	
	public int getRealEndTime() {
		return realEndTime;
	}

	public void setRealEndTime(int realEndTime) {
		this.realEndTime = realEndTime;
	}

	public int getNowSpeed() {
		return nowSpeed;
	}

	public void setNowSpeed(int nowSpeed) {
		this.nowSpeed = nowSpeed;
	}

	public int getNowRoad() {
		return nowRoad;
	}

	public void setNowRoad(int nowRoad) {
		this.nowRoad = nowRoad;
	}
	
	public boolean isUpdated() {
		return isUpdated;
	}

	public void setUpdated(boolean isUpdated) {
		this.isUpdated = isUpdated;
	}

	public String info() {
		String info = "\n";
		info = info.concat(carId + "\n");
		info = info.concat(origin + "\n");
		info = info.concat(destination + "\n");
		info = info.concat(maxSpeed + "\n");
		info = info.concat(startTime + "\n");
		info = info.concat(realStTime + "\n");
		info = info.concat(nowSpeed + "\n");
		info = info.concat(nowRoad + "\n");
		
		return info;
	}
}
