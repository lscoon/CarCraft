package com.huawei.ui;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

import javax.swing.JFrame;
import javax.swing.JPanel;

import org.apache.log4j.Logger;

public class MapFrame extends JFrame{

	private static final Logger logger = Logger.getLogger(MapFrame.class);
	
	private static final int ViewX = 800;
	private static final int ViewY = 660;
	
	private MapPanel pMap = null;
	public ControlPanel pControl = null;
	private JPanel pRoad = null;
	
	public MapFrame() {
		pMap = new MapPanel();
		add(pMap, BorderLayout.EAST);
		setMapPanelListener();
		
		pControl = new ControlPanel(pMap);
		add(pControl, BorderLayout.CENTER);
		
		pRoad = new JPanel(new GridLayout(1,2));
		add(pRoad, BorderLayout.SOUTH);		
		pRoad.add(pControl.roadView0);
		pRoad.add(pControl.roadView1);
		
		setTitle("map");
		setSize(ViewX, ViewY);
		setLocationRelativeTo(null);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setVisible(true);
		setResizable(false);
		// important!!! take control of keyboard listener
		requestFocus();
	}
	
	private void setMapPanelListener() {
		addMouseWheelListener(new MouseWheelListener() {
			@Override
			public void mouseWheelMoved(MouseWheelEvent e) {
				if(e.getWheelRotation() < 0)
					pMap.enlargeMap();
				else pMap.reduceMap();
			}
		});
		
		addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				switch(e.getKeyCode()) {
					case KeyEvent.VK_UP: pMap.upMap();break;
					case KeyEvent.VK_DOWN: pMap.downMap();break;
					case KeyEvent.VK_LEFT: pMap.leftMap();break;
					case KeyEvent.VK_RIGHT: pMap.rightMap();break;
					default: break;
				}
			}
		});
	}
}
