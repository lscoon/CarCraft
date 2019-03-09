package data;

public class Road {

	private int roadId;
	private int length;
	private int limitSpeed;
	private int lanesNum;
	private int origin;
	private int destination;
	private int isBiDirectional;
	
	public Road (String[] strs) {
		if (strs.length != 7)
			return;
		roadId = Integer.valueOf(strs[0].trim()).intValue();
		length = Integer.valueOf(strs[1].trim()).intValue();
		limitSpeed = Integer.valueOf(strs[2].trim()).intValue();
		lanesNum = Integer.valueOf(strs[3].trim()).intValue();
		origin = Integer.valueOf(strs[4].trim()).intValue();
		destination = Integer.valueOf(strs[5].trim()).intValue();
		isBiDirectional = Integer.valueOf(strs[6].trim()).intValue();
		
		
	}
	
	public int getRoadId() {
		return roadId;
	}

	public void setRoadId(int id) {
		this.roadId = id;
	}

	public int getLength() {
		return length;
	}

	public void setLength(int length) {
		this.length = length;
	}

	public int getLimitSpeed() {
		return limitSpeed;
	}

	public void setLimitSpeed(int limitSpeed) {
		this.limitSpeed = limitSpeed;
	}

	public int getLanesNum() {
		return lanesNum;
	}

	public void setLanesNum(int lanesNum) {
		this.lanesNum = lanesNum;
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

	public int getIsBiDirectional() {
		return isBiDirectional;
	}

	public void setIsBiDirectional(int isBiDirectional) {
		this.isBiDirectional = isBiDirectional;
	}
	
	public String info() {
		String info = "\n";
		info = info.concat(roadId + "\n");
		info = info.concat(limitSpeed + "\n");
		info = info.concat(origin + "\n");
		info = info.concat(destination + "\n");
		return info;
	}
	
}
