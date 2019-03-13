package com.huawei.view;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import javax.swing.JTextPane;
import org.apache.log4j.Logger;
import com.huawei.data.RoadMap;

public class MapFrame extends JFrame{

	private static final Logger logger = Logger.getLogger(MapFrame.class);
	
	private static final int ViewX = 800;
	private static final int ViewY = 660;
	private static final int CarInfoX = 100;
	private static final int InfoX = 50;
	private static final int InfoY = 200;
	private static final int RoadInfoX = 250;
	private static final int RoadInfoY = 180;
	private static final int RoadViewX = 400;
	private static final int RoadViewY = 160;
	
	private static final String CarInfo = "car \ncarId: \norigin: \ndestination: \nmaxSpeed: \n"
			+ "startTime: \nrealStTime: \nnowRoad: ";
	private static final String CrossInfo = "cross \ncrossId: \nroad0Id: \n"
			+ "road1Id: \nroad2Id: \nroad3Id: ";
	private static final String RoadInfo = "road \nroadId: \nlimitSpeed \n"
			+ "origin: \ndestination: ";
	
	JPanel pControl = new JPanel(new BorderLayout());
	MapPanel pMap = new MapPanel();
	JPanel pRoad = new JPanel(new GridLayout(1,2));
	
	private JComboBox<Integer> carBox = new JComboBox<>();
	private JComboBox<Integer> crossBox = new JComboBox<>();
	private JComboBox<Integer> roadBox = new JComboBox<>();
	private JComboBox<Integer> roadBoxTwo = new JComboBox<>();
	
	private JTextArea carInfo = new JTextArea();
	private JTextArea crossInfo = new JTextArea();
	private JTextArea roadInfo = new JTextArea();
	private JTextArea roadText0 = new JTextArea();
	private JScrollPane roadView0 = new JScrollPane(roadText0);
	private JTextArea roadText1 = new JTextArea();
	private JScrollPane roadView1 = new JScrollPane(roadText1);
	
	private JButton btMapRefresh = new JButton("refresh");
	
	public MapFrame() {
		
		add(pControl, BorderLayout.CENTER);
		add(pRoad, BorderLayout.SOUTH);
		add(pMap, BorderLayout.EAST);
		
		JPanel pTop = new JPanel();
		pTop.setLayout(new GridLayout(5,2));
		pTop.add(new JLabel("Car ID"));
		pTop.add(carBox);
		pTop.add(new JLabel("Cross ID"));
		pTop.add(crossBox);
		pTop.add(new JLabel("Road ID"));
		pTop.add(roadBox);
		pTop.add(btMapRefresh);
		pTop.add(roadBoxTwo);
		
		for(int i : RoadMap.cars.keySet())
			carBox.addItem(i);
		for(int i : RoadMap.crosses.keySet())
			crossBox.addItem(i);
		for(int i : RoadMap.roads.keySet()) {
			roadBox.addItem(i);
			roadBoxTwo.addItem(i);
		}
		
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
						roadText0.setText(RoadMap.roads.get(roadBox.getSelectedItem()).showStatus());
					}
				});
		roadBoxTwo.addActionListener(
				new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						roadText1.setText(RoadMap.roads.get(roadBoxTwo.getSelectedItem()).showStatus());
					}
				});
		
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
		
		pControl.add(pTop, BorderLayout.NORTH);
		initInfoPanel();
		initRoadPanel();
		
		setTitle("map");
		setSize(ViewX, ViewY);
		setLocationRelativeTo(null);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setVisible(true);
		setResizable(false);
		// important!!! take control of keyboard listener
		requestFocus();
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
		roadText0.setEditable(false);
		roadView1.setPreferredSize(new Dimension(RoadViewX, RoadViewY));
		roadText1.setEditable(false);
	}
}
