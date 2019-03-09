package com.huawei.view;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;

import javax.swing.JFrame;
import javax.swing.JPanel;

import org.apache.log4j.Logger;

public class MapPanel extends JPanel{
	private static final Logger logger = Logger.getLogger(MapPanel.class);
	
	private static final int MapX = 500;
	private static final int MapY = 450;
	
	public MapPanel() {
		setPreferredSize(new Dimension(MapX, MapY));
		setForeground(Color.white);
		logger.info("22");
	}
	
	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
	}
}
