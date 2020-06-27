package edu.southwestern.tasks.loderunner;

import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import edu.southwestern.parameters.Parameters;
import edu.southwestern.tasks.loderunner.astar.LodeRunnerState;
import edu.southwestern.tasks.loderunner.astar.LodeRunnerState.LodeRunnerAction;
import edu.southwestern.util.MiscUtil;
import edu.southwestern.util.datastructures.Graph;
import edu.southwestern.util.datastructures.ListUtil;
import edu.southwestern.util.datastructures.Pair;
import edu.southwestern.util.datastructures.Triple;
import edu.southwestern.util.random.RandomNumbers;

public class LodeRunnerTSPUtil {

	public static void main(String[] args) {
		Parameters.initializeParameterCollections(new String[] {});
		RandomNumbers.randomGenerator.setSeed(0L); // Same random String Node labels every time
		//int visitedSize = 0;
		List<List<Integer>> level = LodeRunnerVGLCUtil.convertLodeRunnerLevelFileVGLCtoListOfLevelForLodeRunnerState(LodeRunnerVGLCUtil.LODE_RUNNER_LEVEL_PATH + "Level 41.txt");
		Pair<ArrayList<LodeRunnerAction>, HashSet<LodeRunnerState>> tspInfo = getFullActionSequenceAndVisitedStatesTSPGreedySolution(level);
		ArrayList<LodeRunnerAction> actionSequence = tspInfo.t1;
		HashSet<LodeRunnerState> mostRecentVisited = tspInfo.t2;
		//		System.out.println(level);
		LodeRunnerRenderUtil.visualizeLodeRunnerLevelSolutionPath(level, actionSequence, mostRecentVisited);

		//System.out.println(fullActionSequence);
		//		//calculates number of states visited from the spawn to every other gold
		//		for(Point p : gold) {
		//			List<List<Integer>> levelCopy = ListUtil.deepCopyListOfLists(level);
		//			levelCopy.get(spawn.y).set(spawn.x, LodeRunnerState.LODE_RUNNER_TILE_SPAWN);
		//			levelCopy.get(p.y).set(p.x, LodeRunnerState.LODE_RUNNER_TILE_GOLD);
		//			Triple<HashSet<LodeRunnerState>, ArrayList<LodeRunnerAction>, LodeRunnerState> aStarInfo =LodeRunnerLevelAnalysisUtil.performAStarSearch(levelCopy, Double.NaN);
		//			visitedSize+=aStarInfo.t1.size();//keeps track of how many visited states for every run of A*
		//		}
		//		System.out.println(tsp.root());
		//		System.out.println(tsp.root().adjacencies());
		//		System.out.println(tsp.getNodes());
		//		System.out.println("Number of states visited: "+visitedSize);
	}

	/**
	 * Gets the action sequence based on solving the TSP in the level that is passed in as a parameter
	 * @param level A level
	 * @return ArrayList; actionSeqeunce for the whole level
	 */
	public static Pair<ArrayList<LodeRunnerAction>, HashSet<LodeRunnerState>> getFullActionSequenceAndVisitedStatesTSPGreedySolution(List<List<Integer>> level) {
		Triple<Graph<Point>, HashMap<Pair<Point, Point>, ArrayList<LodeRunnerAction>>, HashSet<LodeRunnerState>> tspInfo = getTSPGraph(ListUtil.deepCopyListOfLists(level));
		Graph<Point> tsp = tspInfo.t1;
		HashMap<Pair<Point, Point>, ArrayList<LodeRunnerAction>> tspActions = tspInfo.t2;
		HashSet<LodeRunnerState> mostRecentVisited = tspInfo.t3;
		//List<Pair<Graph<Point>.Node, Double>> solutionPath = getTSPGreedySolution(tsp);
//		System.out.println(tsp);
//		MiscUtil.waitForReadStringAndEnterKeyPress();
		List<Pair<Graph<Point>.Node, Double>> solutionPath = getTSPGreedyWithBackTrackingSolution(tsp.deepCopy());
		ArrayList<LodeRunnerAction> actionSequence = getFullTSPActionSequence(tspActions, solutionPath);
		return new Pair<ArrayList<LodeRunnerAction>, HashSet<LodeRunnerState>>(actionSequence, mostRecentVisited);
	}

	/**
	 * Concatenates the A* paths between each gold visited by the solved TSP problem 
	 * @param tspActions HashMap; key is a pair of points, value is the action sequence between those points
	 * @param solutionPath Solution to the TSP problem 
	 * @return The full solution path from the TSP problem 
	 */
	public static ArrayList<LodeRunnerAction> getFullTSPActionSequence(
			HashMap<Pair<Point, Point>, ArrayList<LodeRunnerAction>> tspActions,
			List<Pair<Graph<Point>.Node, Double>> solutionPath) {
		ArrayList<LodeRunnerAction> fullActionSequence = new ArrayList<>();
		for(int i = 0; i < solutionPath.size()-1; i++) {
			Pair<Point, Point> key = new Pair<Point, Point>(solutionPath.get(i).t1.getData(), solutionPath.get(i+1).t1.getData());
			// We may get a null result here if the movement model was unable to find a path between any edges.
			// Some few transitions are legitimately one-directional.
			ArrayList<LodeRunnerAction> result = tspActions.get(key);
			if(result != null)
				fullActionSequence.addAll(result);
			//			else
			//				System.out.println(key);
		}
		return fullActionSequence;
	}

	/**
	 * Kick off method for the recursive backtracking method of getting the solution path 
	 * @param tsp Graph
	 * @return Solution Path
	 */
	public static List<Pair<Graph<Point>.Node, Double>> getTSPGreedyWithBackTrackingSolution(Graph<Point> tsp) {
		List<Pair<Graph<Point>.Node, Double>> solution = new ArrayList<>();
		solution.add(new Pair<Graph<Point>.Node, Double>(tsp.root(), 0.0)); //adds the spawn as the first point 
		return greedyTSPStep(tsp, solution);
	}

	/**
	 * Recursive backtracking algorithm to get the right solution path that cna always beat a level
	 * @param tsp Graph 
	 * @param solution Partial solution, always guaranteed to have the root
	 * @return Full final solution path
	 */
	private static List<Pair<Graph<Point>.Node, Double>> greedyTSPStep(Graph<Point> tsp, List<Pair<Graph<Point>.Node, Double>> solution) {
		//System.out.println(solution);
		Graph<Point>.Node sourceNode = solution.get(solution.size()-1).t1;
		List<Pair<Graph<Point>.Node, Double>> sortedList = tsp.getNode(sourceNode.getID()).adjacenciesSortedByEdgeCost();
		
//		for(Pair p : solution) {
//			System.out.print(" ");
//		}
//		System.out.println(sourceNode + ":" + sortedList);
		
		
		for(int i = 0; i < sortedList.size(); i++) {
			Pair<Graph<Point>.Node, Double> candidate = sortedList.get(i);
			
//			for(Pair p : solution) {
//				System.out.print(" ");
//			}
//			System.out.println("Candidate: "+candidate);
			
			
//			for(Pair p : solution) {
//				System.out.print(" ");
//			}
//			System.out.println(candidate.t1);
			solution.add(candidate);
						
//			if(tsp.size() == 5) { 
//				System.out.println(i + " of " + sortedList.size());
//				System.out.println(solution);
//				System.out.println(tsp);
//				MiscUtil.waitForReadStringAndEnterKeyPress();
//			}
			
			if(tsp.size() == 2) { // All but final node successfully removed from completed solution 
				//System.out.println("Success: " + solution);
				return solution;
			} else {
//				for(Pair p : solution) {
//					System.out.print(" ");
//				}
//				System.out.println("Edges Before:"+tsp.totalEdges());
				
				Graph<Point> tspCopy = tsp.deepCopy();
				boolean nodeRemoved = tsp.removeNode(sourceNode);
				assert nodeRemoved : "How could "+candidate.t1+" not be remove from \n"+tsp;
				List<Pair<Graph<Point>.Node, Double>> result = greedyTSPStep(tsp, solution);
				if(result != null) return result;
				tsp = tspCopy; // Undoes the removal of sourceNode
//				for(Pair p : solution) {
//					System.out.print("X");
//				}
//				System.out.println("Edges Restored:"+tsp.totalEdges());

			}
			solution.remove(solution.size()-1);
//			for(Pair p : solution) {
//				System.out.print("X");
//			}
//			System.out.println(candidate.t1);

		}
		
		
//		for(Pair p : solution) {
//			System.out.print("X");
//		}
//		System.out.println(sourceNode + ":" + sortedList);
		
		return null;
	}



	/**
	 * Creates a sequence to collect the gold for in the level 
	 * by moving to the node that has the least weight on its edge and removing 
	 * the current node
	 * It will always give a solution, but not an optimal one
	 * @param tsp A digraph holding points of gold and weights
	 * @return The order to collect gold
	 */
	public static List<Pair<Graph<Point>.Node, Double>> getTSPGreedySolution(Graph<Point> tsp) {
		//solving the TSP problem from the graph 
		List<Pair<Graph<Point>.Node, Double>> solutionPath = new ArrayList<>(); //set of points
		solutionPath.add(new Pair<Graph<Point>.Node, Double>(tsp.root(), 0.0)); //adds the spawn as the first point 
		//loops through all adjacent nodes, adds the node with the lowest weight 
		while(!tsp.root().adjacencies().isEmpty()) {
			Iterator<Pair<Graph<Point>.Node, Double>> itr = tsp.root().adjacencies().iterator();
			Pair<Graph<Point>.Node, Double> min = new Pair<Graph<Point>.Node, Double>(tsp.root(), Double.MAX_VALUE);
			while(itr.hasNext()) {
				Pair<Graph<Point>.Node, Double> node = itr.next();
				if(node.t2 < min.t2) {
					min = node;
				}
			}
			solutionPath.add(min);
			//System.out.println(solutionPath);
			tsp.root().adjacencies().remove(min);
		}
		return solutionPath;
	}

	/**
	 * Creates a graph for the TSP problem from the points where the gold are located
	 * @param level A level
	 * @return A graph with gold as nodes and the spawn point as the root
	 */
	public static Triple<Graph<Point>, HashMap<Pair<Point, Point>, ArrayList<LodeRunnerAction>>, 
	HashSet<LodeRunnerState>> getTSPGraph(List<List<Integer>> level) {
		//clears level of gold and spawn but maintains a reference in this set 
		HashSet<Point> gold = LodeRunnerState.fillGold(level);
		Point spawn = findSpawnAndRemove(level);
		HashMap<Pair<Point, Point>, ArrayList<LodeRunnerAction>> actionSequences = new HashMap<>();
		HashSet<LodeRunnerState> mostRecentVisited = new HashSet<>();
		Graph<Point> tsp = new Graph<Point>();
		tsp.addNode(spawn);
		for(Point p : gold) {
			tsp.addNode(p);
		}
		for(Graph<Point>.Node p : tsp.getNodes()) {
			for(Graph<Point>.Node i : tsp.getNodes()) {
				List<List<Integer>> levelCopy = ListUtil.deepCopyListOfLists(level);
				//if the nodes aren't equal and makes the spawn point not be a destination, only a source
				if(!p.equals(i) && !i.getData().equals(spawn)) {
					levelCopy.get(p.getData().y).set(p.getData().x, LodeRunnerState.LODE_RUNNER_TILE_SPAWN);//sets spawn as one of the gold to get distance between the gold
					levelCopy.get(i.getData().y).set(i.getData().x, LodeRunnerState.LODE_RUNNER_TILE_GOLD); //destination gold 
					Triple<HashSet<LodeRunnerState>, ArrayList<LodeRunnerAction>, LodeRunnerState> aStarInfo = LodeRunnerLevelAnalysisUtil.performAStarSearch(levelCopy, Double.NaN);

					//System.out.println(p + " to " + i +":" + aStarInfo.t2);
					if(aStarInfo.t2 == null) {
						// TODO: Here Kirby: if path is null, then turn on cheat movement through diggable and try again
						// 		 However, if the result is STILL null after that, then we assume the path is one-directional,
						//		 and simply do not add an edge to the TSP

						continue; // Cannot reach i from p, so do not add edge to TSP graph
						//LodeRunnerRenderUtil.visualizeLodeRunnerLevelSolutionPath(level, aStarInfo.t2, aStarInfo.t1);
						//MiscUtil.waitForReadStringAndEnterKeyPress();
					}
					//visitedSize+=aStarInfo.t1.size(); //keeps track of how many visited states for every run of A*
					double simpleAStarDistance = LodeRunnerLevelAnalysisUtil.calculateSimpleAStarLength(aStarInfo.t2);
					tsp.addDirectedEdge(p, i, simpleAStarDistance); //adds the directed edge to the graph
					Pair<Point, Point> key = new Pair<Point, Point>(p.getData(), i.getData());
					actionSequences.put(key, aStarInfo.t2); //adds the key and value to the hash map 
					mostRecentVisited.addAll(aStarInfo.t1); //adds the visited states from going between those two points
				}
			}
		}
		return new Triple<Graph<Point>,  HashMap<Pair<Point, Point>, ArrayList<LodeRunnerAction>>, HashSet<LodeRunnerState>>(tsp,actionSequences,mostRecentVisited);
	}

	/**
	 * This method finds the spawn point in the level, marks it, and removes it for artificial placement
	 * @param level A level
	 * @return Location of the spawn point
	 */
	public static Point findSpawnAndRemove(List<List<Integer>> level) {
		Point spawn = null;
		for(int i = 0; i < level.size(); i++) {
			for(int j = 0; j < level.get(i).size(); j++){
				if(level.get(i).get(j) == LodeRunnerState.LODE_RUNNER_TILE_SPAWN) {
					spawn = new Point(j, i);
					level.get(i).set(j, LodeRunnerState.LODE_RUNNER_TILE_EMPTY);
					break;
				}
			}
		}
		return spawn;
	}





}
