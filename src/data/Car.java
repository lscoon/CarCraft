package data;

/**
 * @author sc
 *
 */
public class Car {
	
	private int carId;
	private int origin;
	private int destination;
	private int maxSpeed;
	private int startTime;
	
	private int realStTime = 0;
	private int nowSpeed = 0;
	private int nowRoad = 0;
	
	public Car (String[] strs) {
		if (strs.length != 5)
			return;
		carId = Integer.valueOf(strs[0].trim()).intValue();
		origin = Integer.valueOf(strs[1].trim()).intValue();
		destination = Integer.valueOf(strs[2].trim()).intValue();
		maxSpeed = Integer.valueOf(strs[3].trim()).intValue();
		startTime = Integer.valueOf(strs[4].trim()).intValue();
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
