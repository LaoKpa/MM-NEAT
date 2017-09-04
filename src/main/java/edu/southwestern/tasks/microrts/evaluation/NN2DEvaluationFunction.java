package edu.southwestern.tasks.microrts.evaluation;

import edu.southwestern.MMNEAT.MMNEAT;
import edu.southwestern.evolution.genotypes.Genotype;
import edu.southwestern.networks.Network;
import edu.southwestern.parameters.Parameters;
import edu.southwestern.util.PopulationUtil;
import micro.rts.GameState;
import micro.rts.units.Unit;

/**
 * Evaluation function compatible with NEAT and HyperNEAT that
 * uses 1 substrate containing information about every tile of
 * the game state.
 * 
 * @author alicequint
 *
 * @param <T> NN
 */
public class NN2DEvaluationFunction<T extends Network> extends NNEvaluationFunction<T> {
 
	private static final double BASE_WEIGHT = 4; //hard to quantify because different amount of importance at different stages of the game
	private static final double BASE_RESOURCE_WEIGHT = .25;
	private static final double BARRACKS_WEIGHT = 2.5;
	private static final double WORKER_WEIGHT = 1;
	private static final double WORKER_RESOURCE_WEIGHT = .15;
	//these subject to change because in experiments so far there have rarely been multiple non-worker units
	private static final double LIGHT_WEIGHT = 3; 
	private static final double HEAVY_WEIGHT = 3.25;
	private static final double RANGED_WEIGHT = 3.75;
	private static final double RAW_RESOURCE_WEIGHT = .01;
	
	/**
	 * constructor for FEStatePane and similar
	 * @param NNfile
	 * 				neural network .xml file 
	 */
	public NN2DEvaluationFunction(String NNfile){
		// Parameter init can/should be removed when moving to stand-alone competition entry
		Parameters.initializeParameterCollections(new String[]{"task:edu.southwestern.tasks.microrts.MicroRTSTask","hyperNEAT:true"
				,"microRTSEnemySequence:edu.southwestern.tasks.microrts.iterativeevolution.CompetitiveEnemySequence",
				"microRTSMapSequence:edu.southwestern.tasks.microrts.iterativeevolution.GrowingMapSequence","log:microRTS-temp","saveTo:temp"});
		MMNEAT.loadClasses();
		Genotype<T> g = PopulationUtil.extractGenotype(NNfile);
		nn = g.getPhenotype();
	}

	/**
	 * Default constructor used by MMNEAT's class creation methods.
	 * Must pass in the network via the setNetwork method of parent class.
	 */
	public NN2DEvaluationFunction(){
		super();
	}
	
	/**
	 * represents all squares of the gameState in an array
	 */
	protected double[] gameStateToArray(GameState gs, int playerToEvaluate) {
		pgs = gs.getPhysicalGameState();
		double[] board = new double[pgs.getHeight()*pgs.getWidth()];
		int boardIndex;
		Unit currentUnit;
		for(int j = 0; j < pgs.getHeight(); j++){
			for(int i = 0; i < pgs.getWidth(); i++){
				boardIndex = i + j * pgs.getHeight();
				currentUnit = pgs.getUnitAt(i, j);
				if(currentUnit != null){
					board[boardIndex] = getWeightedValue(currentUnit, playerToEvaluate);
				}
			}//end inner loop
		}//end outer loop
		return board;
	}

	/**
	 * decides what weight the current unit should be represented as
	 * @param currentUnit
	 * @return weighted value
	 */
	public double getWeightedValue(Unit currentUnit, int playerToEvaluate) {
		double value = 0;
		switch(currentUnit.getType().name){
		case "Worker": value = WORKER_WEIGHT + (WORKER_RESOURCE_WEIGHT * currentUnit.getResources()); break; 
		case "Light": value = LIGHT_WEIGHT; break;
		case "Heavy": value = HEAVY_WEIGHT; break;
		case "Ranged": value = RANGED_WEIGHT; break;
		case "Base": value = BASE_WEIGHT + (BASE_RESOURCE_WEIGHT * currentUnit.getResources()); break;
		case "Barracks": value = BARRACKS_WEIGHT; break;
		case "Resource": value = RAW_RESOURCE_WEIGHT; break;
		default: break;
		}
		if(currentUnit.getPlayer() != playerToEvaluate) value *= -1;
		return value;
	}

	/**
	 * returns labels describing what gameStateToArray will
	 * give for the inputs to a NN  
	 */
	@Override
	public String[] sensorLabels() {
		assert pgs != null : "There must be a physical game state in order to extract height and width";
		String[]labels = new String[pgs.getHeight()*pgs.getWidth()];
		for(int i = 0; i < pgs.getWidth(); i++){
			for(int j = 0; j < pgs.getHeight(); j++){
				String label = "unit at (" + i + ", " + j + ")";
				labels[i*pgs.getWidth() + j] = label;
			} 
		}
		return labels; 
	}
	
	public int getNumInputSubstrates(){
		return 1;
	}
}
