package com.huawei.view;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import javax.swing.JPanel;

import org.apache.log4j.Logger;

import com.huawei.data.Cross;
import com.huawei.data.Road;
import com.huawei.data.RoadMap;

public class MapPanel extends JPanel{
	private static final Logger logger = Logger.getLogger(MapPanel.class);
	
	private static final Font crossFont = new Font("SansSerif", Font.BOLD, 12);
	private static final Font roadFont = new Font("SansSerif", Font.BOLD, 8);
	
	private static final int MapX = 500;
	private static final int MapY = 500;
	
	private int Distance = 80;
	private int TranslateX = 50;
	private int TranslateY = 450;
	
	// key matrix, don't ask me why
	// x means beginCross roads count, y means endCross roads count
	private static final int[][] rotationMatrix = {
			{2,1,0,3},{3,2,1,0},{0,1,2,3},{3,0,1,2}};
	
	private Map<Integer,int[]> crossLocationMap = new HashMap<>();
	private Set<Integer> paintedRoads = new HashSet<>();
	
	public MapPanel() {
		setPreferredSize(new Dimension(MapX, MapY));
		setBackground(Color.white);
		setForeground(Color.black);
		setVisible(true);
		
		BFSCrosses();
	}
	
	private void BFSCrosses() {
		int startCrossId = RoadMap.crossSequence.get(0);
		crossLocationMap.put(startCrossId, new int[]{0,0});
		
		Queue<Integer> queue = new LinkedList<>();
		queue.add(startCrossId);
		while(!queue.isEmpty()) {
			Integer crossId = queue.poll();
			Cross cross = RoadMap.crosses.get(crossId);
			for(int i=0; i<4; i++) {
				Road road = cross.getRoads().get(i);
				if(road == null)
					continue;
				int nextCrossId = road.getAnOtherCross(crossId);
				if(!crossLocationMap.containsKey(nextCrossId)) {
					Cross nextCross = RoadMap.crosses.get(nextCrossId);
					nextCross.setRotationStatus(cross.getRotationStatus()
							+ rotationMatrix[i][nextCross.getRotation(road.getRoadId())]);
					crossLocationMap.put(nextCrossId, getNextLocation(
							crossLocationMap.get(crossId),
							cross.getRotationStatus()+i));
					queue.add(nextCrossId);
				}
			}
		}
	};
	
	private int[] getNextLocation(int[] location, int rotation) {
		rotation = rotation%4;
		switch(rotation) {
			case 0:return new int[] {location[0],location[1]-1};
			case 1:return new int[] {location[0]+1,location[1]};
			case 2:return new int[] {location[0],location[1]+1};
			case 3:return new int[] {location[0]-1,location[1]};
			default:
				logger.error("rotation parameter error");
				return location;
		}
	}
	
	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		paintedRoads.clear();
		
		Iterator<Map.Entry<Integer, Cross>> iterator = RoadMap.crosses.entrySet().iterator();
		while (iterator.hasNext()) {
		    Map.Entry<Integer, Cross> entry = iterator.next();
		    paintCross(g, entry.getValue());
		}
	}
	
	private void paintCross(Graphics g, Cross cross) {
		int crossId = cross.getCrossId();
		int[] location = crossLocationMap.get(crossId);
		int locationX = Distance*location[0]+TranslateX;
		int locationY = Distance*location[1]+TranslateY;
		g.setColor(Color.green);
		g.setFont(crossFont);
		g.drawString(Integer.toString(crossId), locationX, locationY);
		
		g.setColor(Color.black);
		g.setFont(roadFont);
		
		List<Road> roads = cross.getRoads();
		int roSta = cross.getRotationStatus();
		
		paintRoad(g, roads.get((4-roSta)%4), locationX, locationY, locationX, locationY-Distance);
		paintRoad(g, roads.get((5-roSta)%4), locationX, locationY, locationX+Distance, locationY);
		paintRoad(g, roads.get((6-roSta)%4), locationX, locationY, locationX, locationY+Distance);
		paintRoad(g, roads.get((7-roSta)%4), locationX, locationY, locationX-Distance, locationY);
	}
	
	private void paintRoad(Graphics g, Road road, int x_1, int y_1, int x_2, int y_2) {
		if(road==null)
			return;
		int roadId = road.getRoadId();
		if(paintedRoads.contains(roadId))
			return;
		g.drawString(Integer.toString(roadId), (x_1+x_2-20)/2, (y_1+y_2)/2);
		g.setColor(Color.red);
		g.drawString(Integer.toString(road.getCarNum()), (x_1+x_2+10)/2, (y_1+y_2)/2);
		g.setColor(Color.black);
		g.drawLine(x_1, y_1, x_2, y_2);
		paintedRoads.add(roadId);
	}
	
	protected void enlargeMap() {
		Distance++;
		//Scala += 0.1;
		repaint();
	}
	
	protected void reduceMap() {
		Distance--;
		//Scala -= 0.1;
		repaint();
	}
	
	protected void leftMap() {
		TranslateX -= 10;
		repaint();
	}
	
	protected void rightMap() {
		TranslateX += 10;
		repaint();
	}
	
	protected void upMap() {
		TranslateY -= 10;
		repaint();
	}
	
	protected void downMap() {
		TranslateY += 10;
		repaint();
	}
}
