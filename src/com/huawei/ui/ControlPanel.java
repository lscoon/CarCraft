package com.huawei.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextPane;

import com.huawei.service.MapSimulator;
import com.huawei.util.MapUtil;

public class ControlPanel extends JPanel{

	private static final int CarInfoX = 100;
	private static final int InfoX = 50;
	private static final int InfoY = 200;
	private static final int RoadInfoX = 250;
	private static final int RoadInfoY = 180;
	
	private static final int RoadViewX = 400;
	private static final int RoadViewY = 160;
	
	private static final String CarInfo = "car \ncarId: \norigin: \ndestin: \nmaxSpd: \n"
			+ "stTime: \nrealStT: \nnowRoad: \nnextRoad:";
	private static final String CrossInfo = "cross \ncrossId: \nroad0Id: \n"
			+ "road1Id: \nroad2Id: \nroad3Id: ";
	private static final String RoadInfo = "road \nroadId: \nlimitSpeed: \n"
			+ "origin: \ndestination: \nlanNum: \nlen: \nforwCar: \nbackCar";
	
	public JTextArea info = null;
	
	protected JPanel pTop = new JPanel(new GridLayout(5,2));
	protected JPanel pCen = new JPanel(new BorderLayout());
	protected JPanel pSou = new JPanel(new GridLayout(1,4));
	
	protected JTextArea carInfo = new JTextArea();
	protected JComboBox<Integer> carBox = new JComboBox<>();
	protected JComboBox<Integer> crossBox = new JComboBox<>();
	protected JComboBox<Integer> roadBox = new JComboBox<>();
	protected JComboBox<Integer> roadBoxTwo = new JComboBox<>();
	
	protected JTextArea roadText0 = null;
	protected JTextArea roadText1 = null;
	protected JScrollPane roadView0 = null;
	protected JScrollPane roadView1 = null;
	
	public ControlPanel(MapPanel pMap) {
		super(new BorderLayout());

		add(pTop, BorderLayout.NORTH);
		
		add(pCen, BorderLayout.CENTER);
		add(pSou, BorderLayout.SOUTH);
		
		initCarInfo();
		//initCrossInfo();
		initBlankInfo();
		initRoadInfo();
		
		JButton btMapRefresh = new JButton("refresh");
		pTop.add(btMapRefresh);
		
		btMapRefresh.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e) {
				if(MapSimulator.finishCars.size()!=MapUtil.cars.size()) {
					Thread t = new Thread(new Runnable(){  
			            public void run(){
			            	btMapRefresh.setEnabled(false);
			            	MapSimulator.updateMap();
			            	MapSimulator.term++;
			            	
			            	carInfo.setText(MapUtil.cars.get(carBox.getSelectedItem()).info());
			            	roadText0.setText(MapUtil.roads.get(roadBox.getSelectedItem()).showStatus());
			            	roadText1.setText(MapUtil.roads.get(roadBoxTwo.getSelectedItem()).showStatus());
							btMapRefresh.setText(Integer.toString(MapSimulator.term-1));
							pMap.repaint();
							btMapRefresh.setEnabled(true);
			            }
					});
			        t.start();  
				}
				else btMapRefresh.setText("End " + (MapSimulator.term-1));
			}
			
		});
	}
	
	private void initCarInfo() {
		carInfo = new JTextArea();
		carInfo.setPreferredSize(new Dimension(CarInfoX, InfoY));
		carInfo.setEditable(false);
		
		carBox = new JComboBox<>();
		for(int i : MapUtil.cars.keySet())
			carBox.addItem(i);
		carBox.addActionListener(
				new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						carInfo.setText(MapUtil.cars.get(carBox.getSelectedItem()).info());
					}
				});
		
		JTextPane tempCarInfo = new JTextPane();
		tempCarInfo.setPreferredSize(new Dimension(InfoX, InfoY));
		tempCarInfo.setText(CarInfo);
		tempCarInfo.setEditable(false);
		
		pTop.add(new JLabel("Car ID"));
		pTop.add(carBox);
		
		pCen.add(tempCarInfo, BorderLayout.WEST);
		pCen.add(carInfo, BorderLayout.CENTER);
	}
	
	private void initCrossInfo() {
		JTextArea crossInfo = new JTextArea();
		crossInfo.setPreferredSize(new Dimension(CarInfoX, InfoY));
		crossInfo.setEditable(false);
		
		crossBox = new JComboBox<>();
		for(int i : MapUtil.crosses.keySet())
			crossBox.addItem(i);
		crossBox.addActionListener(
				new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						crossInfo.setText(MapUtil.crosses.get(crossBox.getSelectedItem()).info());
					}
				});
		
		JTextPane tempCrossInfo = new JTextPane();
		tempCrossInfo.setPreferredSize(new Dimension(InfoX, InfoY));
		tempCrossInfo.setText(CrossInfo);
		tempCrossInfo.setEditable(false);
		
		pTop.add(new JLabel("Cross ID"));
		pTop.add(crossBox);
		
		pCen.add(tempCrossInfo);
		pCen.add(crossInfo);
	}
	
	private void initBlankInfo() {
		info = new JTextArea();
		info.setEditable(false);
		JScrollPane pane = new JScrollPane(info);
		pane.setPreferredSize(new Dimension((CarInfoX+InfoX), InfoY));
		
		pCen.add(pane, BorderLayout.EAST);
	}
	
	private void initRoadInfo() {
		JTextArea roadInfo = new JTextArea();
		roadInfo.setPreferredSize(new Dimension(CarInfoX, RoadInfoY));
		roadInfo.setEditable(false);
		
		JTextArea roadInfoTwo = new JTextArea();
		roadInfoTwo.setPreferredSize(new Dimension(CarInfoX, RoadInfoY));
		roadInfoTwo.setEditable(false);
		
		roadBox = new JComboBox<>();
		roadBoxTwo = new JComboBox<>();
		
		roadText0 = new JTextArea();
		roadView0 = new JScrollPane(roadText0);
		roadView0.setPreferredSize(new Dimension(RoadViewX, RoadViewY));
		roadText0.setEditable(false);
		
		roadText1 = new JTextArea();
		roadView1 = new JScrollPane(roadText1);
		roadView1.setPreferredSize(new Dimension(RoadViewX, RoadViewY));
		roadText1.setEditable(false);
		
		for(int i : MapUtil.roads.keySet()) {
			roadBox.addItem(i);
			roadBoxTwo.addItem(i);
		}
		
		roadBox.addActionListener(
				new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						roadInfo.setText(MapUtil.roads.get(roadBox.getSelectedItem()).info());
						roadText0.setText(MapUtil.roads.get(roadBox.getSelectedItem()).showStatus());
					}
				});
		roadBoxTwo.addActionListener(
				new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						roadInfoTwo.setText(MapUtil.roads.get(roadBoxTwo.getSelectedItem()).info());
						roadText1.setText(MapUtil.roads.get(roadBoxTwo.getSelectedItem()).showStatus());
					}
				});
		
		JTextPane tempRoadInfo = new JTextPane();
		tempRoadInfo.setPreferredSize(new Dimension(InfoX, RoadInfoY));
		tempRoadInfo.setText(RoadInfo);		
		tempRoadInfo.setEditable(false);
		
		JTextPane tempRoadInfoTwo = new JTextPane();
		tempRoadInfoTwo.setPreferredSize(new Dimension(InfoX, RoadInfoY));
		tempRoadInfoTwo.setText(RoadInfo);		
		tempRoadInfoTwo.setEditable(false);
		
		pTop.add(new JLabel("Road ID"));
		pTop.add(roadBox);
		pTop.add(new JLabel("Road2 ID"));
		pTop.add(roadBoxTwo);
		
		pSou.add(tempRoadInfo);
		pSou.add(roadInfo);
		pSou.add(tempRoadInfoTwo);
		pSou.add(roadInfoTwo);
	}
}
