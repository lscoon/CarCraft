package view;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;

import javax.swing.JFrame;
import javax.swing.JPanel;

public class MapPanel extends JPanel{
	
	private static final int MapX = 500;
	private static final int MapY = 500;
	
	public MapPanel() {
		setPreferredSize(new Dimension(MapX, MapY));
		setForeground(Color.white);
	}
	
	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
	}
}
