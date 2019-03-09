package com.huawei.view;

import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JTextPane;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.huawei.data.Car;
import com.huawei.data.Cross;
import com.huawei.data.Road;
import com.huawei.data.RoadMap;
import com.huawei.handle.InputHandle;

public class MapFrame extends JFrame{
	private static final Logger logger = Logger.getLogger(MapFrame.class);
	
	private static final int ViewX = 800;
	private static final int ViewY = 600;
	private static final int CarInfoX = 100;
	private static final int InfoX = 50;
	private static final int InfoY = 200;
	private static final int RoadInfoX = 250;
	private static final int RoadInfoY = 150;
	private static final int RoadViewX = 400;
	private static final int RoadViewY = 150;
	
	private static final String CarInfo = "car \ncarId: \norigin: \ndestination: \nmaxSpeed: \n"
			+ "startTime: \nrealStTime: \nnowSpeed: \nnowRoad: ";
	private static final String CrossInfo = "cross \ncrossId: \nroad0Id: \n"
			+ "road1Id: \nroad2Id: \nroad3Id: ";
	private static final String RoadInfo = "road \nroadId: \nlimitSpeed \n"
			+ "origin: \ndestination: ";
	
	JPanel pControl = new JPanel(new BorderLayout());
	MapPanel pMap = new MapPanel();
	JPanel pRoad = new JPanel(new GridLayout(1,2));
	
	private JComboBox<Integer> carBox = new JComboBox<Integer>();
	private JComboBox<Integer> crossBox = new JComboBox<Integer>();
	private JComboBox<Integer> roadBox = new JComboBox<Integer>();
	
	private JTextPane carInfo = new JTextPane();
	private JTextPane crossInfo = new JTextPane();
	private JTextPane roadInfo = new JTextPane();
	private JTextPane roadView0 = new JTextPane();
	private JTextPane roadView1 = new JTextPane();
	
	private JButton btMapRefresh = new JButton("refresh");
	
	public MapFrame() {
		
		add(pMap, BorderLayout.EAST);
		add(pControl, BorderLayout.CENTER);
		add(pRoad, BorderLayout.SOUTH);
		
		JPanel pTop = new JPanel();
		pTop.setLayout(new GridLayout(4,2));
		pTop.add(new JLabel("Car ID"));
		pTop.add(carBox);
		pTop.add(new JLabel("Cross ID"));
		pTop.add(crossBox);
		pTop.add(new JLabel("Road ID"));
		pTop.add(roadBox);
		pTop.add(btMapRefresh);
		
		for(int i : RoadMap.cars.keySet())
			carBox.addItem(i);
		for(int i : RoadMap.crosses.keySet())
			crossBox.addItem(i);
		for(int i : RoadMap.roads.keySet())
			roadBox.addItem(i);
		
		carBox.addActionListener(
				new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						carInfo.setText(RoadMap.cars.get(carBox.getSelectedItem()).info());
					}
				});
		crossBox.addActionListener(
				new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						crossInfo.setText(RoadMap.crosses.get(crossBox.getSelectedItem()).info());
					}
				});
		roadBox.addActionListener(
				new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						roadInfo.setText(RoadMap.roads.get(roadBox.getSelectedItem()).info());
					}
				});
		
		pControl.add(pTop, BorderLayout.NORTH);
		initInfoPanel();
		initRoadPanel();
		
		setTitle("map");
		setSize(ViewX, ViewY);
		setLocationRelativeTo(null);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setVisible(true);
		setResizable(false);
	}
	
	private void initInfoPanel() {
		carInfo.setPreferredSize(new Dimension(CarInfoX, InfoY));
		carInfo.setEditable(false);
		crossInfo.setPreferredSize(new Dimension(CarInfoX, InfoY));
		crossInfo.setEditable(false);
		roadInfo.setPreferredSize(new Dimension(RoadInfoX, RoadInfoY));
		roadInfo.setEditable(false);
		
		JTextPane tempCarInfo = new JTextPane();
		tempCarInfo.setPreferredSize(new Dimension(InfoX, InfoY));
		tempCarInfo.setText(CarInfo);
		tempCarInfo.setEditable(false);
		JTextPane tempCrossInfo = new JTextPane();
		tempCrossInfo.setPreferredSize(new Dimension(InfoX, InfoY));
		tempCrossInfo.setText(CrossInfo);
		tempCrossInfo.setEditable(false);
		JTextPane tempRoadInfo = new JTextPane();
		tempRoadInfo.setPreferredSize(new Dimension(InfoX, RoadInfoY));
		tempRoadInfo.setText(RoadInfo);		
		tempRoadInfo.setEditable(false);
		
		JPanel pTemp1 = new JPanel(new GridLayout(1,4));
		pTemp1.add(tempCarInfo);
		pTemp1.add(carInfo);
		pTemp1.add(tempCrossInfo);
		pTemp1.add(crossInfo);
		JPanel pTemp2 = new JPanel(new GridLayout(1,2));
		pTemp2.add(tempRoadInfo);
		pTemp2.add(roadInfo);
		pControl.add(pTemp1, BorderLayout.CENTER);
		pControl.add(pTemp2, BorderLayout.SOUTH);
	}
	
	private void initRoadPanel() {
		pRoad.add(roadView0);
		pRoad.add(roadView1);
		roadView0.setPreferredSize(new Dimension(RoadViewX, RoadViewY));
		roadView0.setEditable(false);
		roadView1.setPreferredSize(new Dimension(RoadViewX, RoadViewY));
		roadView1.setEditable(false);
	}
}
