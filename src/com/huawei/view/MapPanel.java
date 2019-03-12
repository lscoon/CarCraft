package com.huawei.view;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import javax.swing.JFrame;
import javax.swing.JPanel;

import org.apache.log4j.Logger;

import com.huawei.data.Cross;
import com.huawei.data.Road;
import com.huawei.data.RoadMap;

public class MapPanel extends JPanel{
	private static final Logger logger = Logger.getLogger(MapPanel.class);
	
	private static final int MapX = 500;
	private static final int MapY = 500;
	
	private Map<Integer,Integer[]> crossLocationMap = new LinkedHashMap<Integer,Integer[]>();
	private Map<Integer,Cross.Direction> paintedRoads = new LinkedHashMap<Integer,Cross.Direction>();
	
	private int Distance = 100;
	//private float Scala = 1;
	private int TranslateX = 0;
	private int TranslateY = 0;
	
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
		
		while(crossStack.size()!=0) {
			int crossId = crossStack.pop();
			Integer[] location = crossLocationMap.get(crossId);
			if(location == null)
				logger.error("can't find cross location");
			int[] roads = RoadMap.crosses.get(crossId).getAvailableRoadIds();
			for(int i=0; i<4; i++) 
				if(roads[i]!=-1 && paintedRoads.containsKey(roads[i])) {
					RoadMap.crosses.get(crossId).rotate(i, paintedRoads.get(roads[i]));
					roads = RoadMap.crosses.get(crossId).getAvailableRoadIds();
					break;
				}
			for(int i=0; i<4; i++) {
				int roadId = roads[i];
				if(roadId==-1)
					continue;
				int nextCrossId = RoadMap.roads.get(roadId).getLinkedCross(crossId);
				if(!crossFinish.contains(nextCrossId))
					crossStack.push(nextCrossId);
				switch (i) {
					case 0:
						paintedRoads.put(roadId, Cross.Direction.n);
						crossLocationMap.put(nextCrossId, new Integer[]{location[0],location[1]+1});
						break;
					case 1:
						paintedRoads.put(roadId, Cross.Direction.e);
						crossLocationMap.put(nextCrossId, new Integer[]{location[0]+1,location[1]});
						break;
					case 2:
						paintedRoads.put(roadId, Cross.Direction.s);
						crossLocationMap.put(nextCrossId, new Integer[]{location[0],location[1]-1});
						break;
					case 3:
						paintedRoads.put(roadId, Cross.Direction.w);
						crossLocationMap.put(nextCrossId, new Integer[]{location[0]-1,location[1]});
						break;
					default:break;
				}
			}
			crossFinish.add(crossId);
		}
	}
	
	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		for(int crossId: crossLocationMap.keySet()) {
			Integer[] location = crossLocationMap.get(crossId);
			g.drawString(Integer.toString(crossId), Distance*location[0]+TranslateX, 
					Distance*location[1]+TranslateY);
		}
		
	}
	
	private void paintCross(int crossId) {
		
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
