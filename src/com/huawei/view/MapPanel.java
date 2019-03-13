package com.huawei.view;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import javax.swing.JPanel;

import org.apache.log4j.Logger;

import com.huawei.data.Cross;
import com.huawei.data.RoadMap;

public class MapPanel extends JPanel{
	private static final Logger logger = Logger.getLogger(MapPanel.class);
	
	private static final Font crossFont = new Font("SansSerif", Font.BOLD, 12);
	private static final Font roadFont = new Font("SansSerif", Font.BOLD, 8);
	
	private static final int MapX = 500;
	private static final int MapY = 500;
	
	private Map<Integer,Integer[]> crossLocationMap = new LinkedHashMap<Integer,Integer[]>();
	private Map<Integer,Cross.Direction> roadDirectionMap = new LinkedHashMap<Integer,Cross.Direction>();
	private Set<Integer> paintedRoads = new LinkedHashSet<Integer>();
	
	private int Distance = 80;
	//private float Scala = 1;
	private int TranslateX = 50;
	private int TranslateY = 450;
	
	public MapPanel() {
		setPreferredSize(new Dimension(MapX, MapY));
		setBackground(Color.white);
		setForeground(Color.black);
		setVisible(true);
		
		BFSCrosses();	
	}
	
	private void BFSCrosses() {
		Stack<Integer> crossStack = new Stack<Integer>();
		Set<Integer> crossFinish = new HashSet<Integer>();
		
		// Init
		crossStack.push(RoadMap.crossSequence.get(0));
		crossLocationMap.put(RoadMap.crossSequence.get(0), new Integer[]{0,0});
		
		// crossLocationMap means have been discovered
		// crossFinish means have BFS cross
		while(crossStack.size()!=0) {
			int crossId = crossStack.pop();
			Integer[] location = crossLocationMap.get(crossId);
			if(location == null)
				logger.error("can't find cross location");
			int[] roads = RoadMap.crosses.get(crossId).getRoadIds();
			for(int i=0; i<4; i++) {
				int roadId = roads[i];
				if(roadId==-1)
					continue;
				int nextCrossId = RoadMap.roads.get(roadId).getLinkedCross(crossId);
				if(!crossFinish.contains(nextCrossId))
					crossStack.push(nextCrossId);
				switch (i) {
					case 0:
						if(!roadDirectionMap.containsKey(roadId))
							roadDirectionMap.put(roadId, Cross.Direction.n);
						if(!crossLocationMap.containsKey(nextCrossId)) {
							rotateCross(nextCrossId);
							crossLocationMap.put(nextCrossId, new Integer[]{location[0],location[1]-1});
						}
						break;
					case 1:
						if(!roadDirectionMap.containsKey(roadId))
							roadDirectionMap.put(roadId, Cross.Direction.e);
						if(!crossLocationMap.containsKey(nextCrossId)) {
							rotateCross(nextCrossId);
							crossLocationMap.put(nextCrossId, new Integer[]{location[0]+1,location[1]});
						}
						break;
					case 2:
						if(!roadDirectionMap.containsKey(roadId))
							roadDirectionMap.put(roadId, Cross.Direction.s);
						if(!crossLocationMap.containsKey(nextCrossId)) {
							rotateCross(nextCrossId);
							crossLocationMap.put(nextCrossId, new Integer[]{location[0],location[1]+1});
						}
						break;
					case 3:
						if(!roadDirectionMap.containsKey(roadId))
							roadDirectionMap.put(roadId, Cross.Direction.w);
						if(!crossLocationMap.containsKey(nextCrossId)) {
							rotateCross(nextCrossId);
							crossLocationMap.put(nextCrossId, new Integer[]{location[0]-1,location[1]});
						}	
						break;
					default:break;
				}
			}
			crossFinish.add(crossId);
		}
	}
	
	private void rotateCross(int crossId) {
		int[] roads = RoadMap.crosses.get(crossId).getRoadIds();
		for(int i=0; i<4; i++) 
			if(roads[i]!=-1 && roadDirectionMap.containsKey(roads[i])) {
				RoadMap.crosses.get(crossId).rotate(i, roadDirectionMap.get(roads[i]));
				break;
			}
	}
	
	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		
		paintedRoads.clear();
		for(int crossId: crossLocationMap.keySet()) {
			paintCross(g, crossId);
		}
	}
	
	private void paintCross(Graphics g, int crossId) {
		Integer[] location = crossLocationMap.get(crossId);
		int locationX = Distance*location[0]+TranslateX;
		int locationY = Distance*location[1]+TranslateY;
		g.setColor(Color.red);
		g.setFont(crossFont);
		g.drawString(Integer.toString(crossId), locationX, locationY);
		
		g.setColor(Color.black);
		g.setFont(roadFont);
		
		int[] roads = RoadMap.crosses.get(crossId).getRoadIds();
		if(roads[0]!=-1)
			paintRoad(g, roads[0], locationX, locationY, locationX, locationY-Distance);
		if(roads[1]!=-1)
			paintRoad(g, roads[1], locationX, locationY, locationX+Distance, locationY);
		if(roads[2]!=-1)
			paintRoad(g, roads[2], locationX, locationY, locationX, locationY+Distance);
		if(roads[3]!=-1)
			paintRoad(g, roads[3], locationX, locationY, locationX-Distance, locationY);
	}
	
	private void paintRoad(Graphics g, int roadId, int x_1, int y_1, int x_2, int y_2) {
		if(paintedRoads.contains(roadId))
			return;
		g.drawString(Integer.toString(roadId), (x_1+x_2)/2, (y_1+y_2)/2);
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
