package data;

public class Car {
	
	private int carId;
	private int origin;
	private int destination;
	private int speed;
	private int startTime;
	
	public Car (String[] strs) {
		if (strs.length != 5)
			return;
		carId = Integer.valueOf(strs[0].trim()).intValue();
		origin = Integer.valueOf(strs[1].trim()).intValue();
		destination = Integer.valueOf(strs[2].trim()).intValue();
		speed = Integer.valueOf(strs[3].trim()).intValue();
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
	public int getSpeed() {
		return speed;
	}
	public void setSpeed(int speed) {
		this.speed = speed;
	}
	public int getStartTime() {
		return startTime;
	}
	public void setStartTime(int beginTime) {
		this.startTime = beginTime;
	}

}
