package com.huawei.util;

import java.awt.print.Book;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Stack;

import com.huawei.Main;
import com.huawei.entity.Cross;
import com.huawei.entity.Road;
import org.apache.log4j.Logger;

import com.huawei.service.GlobalSolver;
import com.huawei.service.MapSimulator;
import com.huawei.util.FileUtil;
import java.util.Stack;

public class DijkstraUtil {
	// public static Map<Integer, Road[][]> pathMap = null;
	// public static Map<Integer, float[][]> distMap = null;
/*
	public static void initPathAndDistMatrixMap() {
		// pathMap = new LinkedHashMap<>();
		// distMap = new LinkedHashMap<>();
		// for (int i = MapUtil.CarMinSpeed; i <= MapUtil.CarMaxSpeed; i++)
		// Dijkstra(i);
		Dijkstra(3,32, 4);
	}
*/
	private static void Dijkstra( Cross crossOne,Cross crossTwo, /*int start, int end,*/ int speed) {
		// Road[][] path = new Road[MapUtil.crosses.size()][MapUtil.crosses.size()];
		float[][] dist = new float[MapUtil.crosses.size()][MapUtil.crosses.size()];
		for (int i = 0; i < MapUtil.crosses.size(); i++)
			for (int j = 0; j < MapUtil.crosses.size(); j++) {
				Road road = computeRoad(MapUtil.crosses.get(MapUtil.crossSequence.get(i)),
						MapUtil.crosses.get(MapUtil.crossSequence.get(j)));
				if (road == null) {
					if (i == j)
						dist[i][j] = 0;
					else
						dist[i][j] = MapUtil.FloatMax;
					// path[i][j] = null;
				} else {
					int nowSpeed = Math.min(speed, road.getLimitSpeed());
					dist[i][j] = ((float) road.getLength()) / nowSpeed;
					// path[i][j] = road;
				}
			}
		int start=crossOne.getCrossId();
		int end=crossTwo.getCrossId();
		int x=start;
		int[] s = new int[MapUtil.crosses.size()];// 自动初始化为0，都属于未得到最短路径的顶点
		int[] path = new int[MapUtil.crosses.size()];// 存储x到u最短路径时u的前一个顶点
		for (int i = 0; i < MapUtil.crosses.size(); i++) {
			if (dist[x][i]>0&&dist[x][i]<100) {//i可以到start
				path[i] = x;
			} 
			else {
				path[i] = -1;//i不可以到start
			}
		}
		s[x]=1;//x=start已找到最短路径
		for (int i = 0; i < MapUtil.crosses.size(); i++) {// 首先需要寻找start顶点到各顶点最短的路径
			float min = MapUtil.FloatMax;
			int v = 0; // 记录x到各顶点最短的
			for (int j = 0; j < MapUtil.crosses.size(); j++) {
				if (s[j] != 1/*未找到最短路*/&& x != j && dist[x][j] != 0 && dist[x][j] < min) {
					min = dist[x][j];
					v = j;
				}
			}
			// v 是目前x到各顶点最短的
			s[v] = 1;//v点为选出的点
			// 修正最短路径distance及最短距离path
			for (int j = 0; j < MapUtil.crosses.size(); j++) {
				if (s[j] != 1 && dist[v][j] != 0
						&& (min + dist[v][j] < dist[x][j] || dist[x][j] == 0)) {
					// 说明加入了中间顶点之后找到了更短的路径
					dist[x][j] = min + dist[v][j];
					path[j] = v;
				}
			}
		}
		Stack<Integer> stack = new Stack<Integer>();
		for (int i = 0; i < MapUtil.crosses.size(); i++) {
			if (dist[x][i] != 0 && i == end) {
				System.out.println(x + "-->" + i + "  " + dist[x][i]);
				int index = i;
				while (index != -1) {
					stack.push(index);
					index = path[index];
				}
				while (!stack.isEmpty()) {
					System.out.print(stack.pop() + " ");//输出语句
				}
				System.out.println();
			}
		}
	}

	private static Road computeRoad(Cross crossOne, Cross crossTwo) {
		Road road = crossOne.findLinkedRoad(crossTwo);
		if (road == null)
			return null;
		if (road.getDestination() != crossTwo && !road.isBiDirect())
			return null;
		return road;
	}
	/*
	private static final Logger logger = Logger.getLogger(DijkstraUtil.class);

	public static void main(String[] args) {
		args = FileUtil.initFiles("inputs/config/");

		if (args.length != 4) {
			logger.error("please input args: inputFilePath, resultFilePath");
			return;
		}

		logger.info("Start...");

		String carPath = args[0];
		String roadPath = args[1];
		String crossPath = args[2];
		String answerPath = args[3];
		logger.info("carPath = " + carPath + " roadPath = " + roadPath + " crossPath = " + crossPath
				+ " and answerPath = " + answerPath);

		// TODO:read input files
		logger.info("start read input files");
		FileUtil.readInputs(carPath, roadPath, crossPath);
		initPathAndDistMatrixMap();
	}
	*/

}
