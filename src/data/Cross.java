package data;

public class Cross {
	
	private int crossId;
	private int road0Id;
	private int road1Id;
	private int road2Id;
	private int road3Id;
	
	public Cross (String[] strs) {
		if (strs.length != 5)
			return;
		crossId = Integer.valueOf(strs[0].trim()).intValue();
		road0Id = Integer.valueOf(strs[1].trim()).intValue();
		road1Id = Integer.valueOf(strs[2].trim()).intValue();
		road2Id = Integer.valueOf(strs[3].trim()).intValue();
		road3Id = Integer.valueOf(strs[4].trim()).intValue();
	}
	
	public int getCrossId() {
		return crossId;
	}
	public void setCrossId(int id) {
		this.crossId = id;
	}
	public int getRoad0Id() {
		return road0Id;
	}
	public void setRoad0Id(int road0Id) {
		this.road0Id = road0Id;
	}
	public int getRoad1Id() {
		return road1Id;
	}
	public void setRoad1Id(int road1Id) {
		this.road1Id = road1Id;
	}
	public int getRoad2Id() {
		return road2Id;
	}
	public void setRoad2Id(int road2Id) {
		this.road2Id = road2Id;
	}
	public int getRoad3Id() {
		return road3Id;
	}
	public void setRoad3Id(int road3Id) {
		this.road3Id = road3Id;
	}
	
	public String info() {
		String info = "\n";
		info = info.concat(crossId + "\n");
		info = info.concat(road0Id + "\n");
		info = info.concat(road1Id + "\n");
		info = info.concat(road2Id + "\n");
		info = info.concat(road3Id + "\n");
		return info;
	}
	
}
