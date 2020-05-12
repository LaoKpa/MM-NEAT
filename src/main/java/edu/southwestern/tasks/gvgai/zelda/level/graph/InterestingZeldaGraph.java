package edu.southwestern.tasks.gvgai.zelda.level.graph;

import java.util.LinkedList;
import java.util.List;

import edu.southwestern.tasks.gvgai.zelda.level.ZeldaGrammar;
import edu.southwestern.util.datastructures.Graph;

public class InterestingZeldaGraph implements ZeldaDungeonGraph{

	@Override
	public Graph<ZeldaGrammar> getGraph() {
//		List<ZeldaGrammar> initialList = new LinkedList<>();
//		initialList.add(ZeldaGrammar.START_S);
//		initialList.add(ZeldaGrammar.KEY_S);
//		initialList.add(ZeldaGrammar.KEY_S);
//
//		initialList.add(ZeldaGrammar.ENEMY_S);
//		initialList.add(ZeldaGrammar.ENEMY_S);
//
//		initialList.add(ZeldaGrammar.KEY_S);
//		initialList.add(ZeldaGrammar.ENEMY_S);
//
//		initialList.add(ZeldaGrammar.LOCK_S);
//		
//		initialList.add(ZeldaGrammar.KEY_S);
//		
//		initialList.add(ZeldaGrammar.LOCK_S);
//		
//		initialList.add(ZeldaGrammar.ENEMY_S);
//		
//		initialList.add(ZeldaGrammar.KEY_S);
//		
//		initialList.add(ZeldaGrammar.PUZZLE_S);
//		
//		initialList.add(ZeldaGrammar.LOCK_S);
//		
//		initialList.add(ZeldaGrammar.ENEMY_S);
//		
//		initialList.add(ZeldaGrammar.ENEMY_S);
//		initialList.add(ZeldaGrammar.TREASURE);
//		Graph<ZeldaGrammar> graph = new Graph<>(initialList);
//		return graph;
		
		/**
		 * THIS COMBINATION BREEDS THREE TRIFORCES
		 */
		List<ZeldaGrammar> initialList = new LinkedList<>();
		initialList.add(ZeldaGrammar.START_S);
		initialList.add(ZeldaGrammar.ENEMY_S);
		initialList.add(ZeldaGrammar.KEY_S);
		initialList.add(ZeldaGrammar.ENEMY_S);		
		initialList.add(ZeldaGrammar.ENEMY_S);		
		initialList.add(ZeldaGrammar.LOCK_S);
		initialList.add(ZeldaGrammar.KEY_S);
		initialList.add(ZeldaGrammar.LOCK_S);
		initialList.add(ZeldaGrammar.ENEMY_S);
		initialList.add(ZeldaGrammar.ENEMY_S);
		initialList.add(ZeldaGrammar.KEY_S);
		initialList.add(ZeldaGrammar.PUZZLE_S);
		initialList.add(ZeldaGrammar.LOCK_S);
		initialList.add(ZeldaGrammar.ENEMY_S);
		initialList.add(ZeldaGrammar.ENEMY_S);

		initialList.add(ZeldaGrammar.TREASURE);
		Graph<ZeldaGrammar> graph = new Graph<>(initialList);
		return graph;
	}

}
