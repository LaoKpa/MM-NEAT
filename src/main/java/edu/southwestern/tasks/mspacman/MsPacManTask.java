package edu.utexas.cs.nn.tasks.mspacman;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import edu.utexas.cs.nn.MMNEAT.MMNEAT;
import edu.utexas.cs.nn.evolution.Organism;
import edu.utexas.cs.nn.evolution.genotypes.Genotype;
import edu.utexas.cs.nn.evolution.genotypes.NetworkGenotype;
import edu.utexas.cs.nn.evolution.nsga2.tug.TUGTask;
import edu.utexas.cs.nn.networks.Network;
import edu.utexas.cs.nn.networks.NetworkTask;
import edu.utexas.cs.nn.networks.hyperneat.HyperNEATTask;
import edu.utexas.cs.nn.networks.hyperneat.HyperNEATUtil;
import edu.utexas.cs.nn.networks.hyperneat.Substrate;
import edu.utexas.cs.nn.parameters.CommonConstants;
import edu.utexas.cs.nn.parameters.Parameters;
import edu.utexas.cs.nn.scores.Score;
import edu.utexas.cs.nn.tasks.NoisyLonerTask;
import edu.utexas.cs.nn.tasks.mspacman.agentcontroller.ghosts.SharedNNGhosts;
import edu.utexas.cs.nn.tasks.mspacman.agentcontroller.pacman.MultinetworkMsPacManController;
import edu.utexas.cs.nn.tasks.mspacman.agentcontroller.pacman.NNMsPacMan;
import edu.utexas.cs.nn.tasks.mspacman.data.TrainingCampManager;
import edu.utexas.cs.nn.tasks.mspacman.facades.ExecutorFacade;
import edu.utexas.cs.nn.tasks.mspacman.facades.GameFacade;
import edu.utexas.cs.nn.tasks.mspacman.facades.GhostControllerFacade;
import edu.utexas.cs.nn.tasks.mspacman.facades.PacManControllerFacade;
import edu.utexas.cs.nn.tasks.mspacman.multitask.MsPacManModeSelector;
import edu.utexas.cs.nn.tasks.mspacman.objectives.AvoidDeadSpaceScore;
import edu.utexas.cs.nn.tasks.mspacman.objectives.ClearTimeScore;
import edu.utexas.cs.nn.tasks.mspacman.objectives.EatenGhostScore;
import edu.utexas.cs.nn.tasks.mspacman.objectives.EdibleTimeParameter;
import edu.utexas.cs.nn.tasks.mspacman.objectives.EligibilityTimeFramesGhostScore;
import edu.utexas.cs.nn.tasks.mspacman.objectives.EligibilityTimeFramesPillScore;
import edu.utexas.cs.nn.tasks.mspacman.objectives.FastGhostEatingScore;
import edu.utexas.cs.nn.tasks.mspacman.objectives.FastPillEatingScore;
import edu.utexas.cs.nn.tasks.mspacman.objectives.GameScore;
import edu.utexas.cs.nn.tasks.mspacman.objectives.GhostRegretScore;
import edu.utexas.cs.nn.tasks.mspacman.objectives.GhostRewardScore;
import edu.utexas.cs.nn.tasks.mspacman.objectives.GhostsPerPowerPillScore;
import edu.utexas.cs.nn.tasks.mspacman.objectives.ImproperlyEatenPowerPillScore;
import edu.utexas.cs.nn.tasks.mspacman.objectives.LairTimeParameter;
import edu.utexas.cs.nn.tasks.mspacman.objectives.LevelGameScore;
import edu.utexas.cs.nn.tasks.mspacman.objectives.LevelScore;
import edu.utexas.cs.nn.tasks.mspacman.objectives.LuringScore;
import edu.utexas.cs.nn.tasks.mspacman.objectives.MsPacManObjective;
import edu.utexas.cs.nn.tasks.mspacman.objectives.PillScore;
import edu.utexas.cs.nn.tasks.mspacman.objectives.PowerPillEatenWhenGhostFarScore;
import edu.utexas.cs.nn.tasks.mspacman.objectives.ProperlyEatenPowerPillScore;
import edu.utexas.cs.nn.tasks.mspacman.objectives.RandomScore;
import edu.utexas.cs.nn.tasks.mspacman.objectives.RawTimeScore;
import edu.utexas.cs.nn.tasks.mspacman.objectives.RemainingLivesScore;
import edu.utexas.cs.nn.tasks.mspacman.objectives.SurvivalAndSpeedTimeScore;
import edu.utexas.cs.nn.tasks.mspacman.objectives.TimeFramesGhostScore;
import edu.utexas.cs.nn.tasks.mspacman.objectives.TimeFramesPillScore;
import edu.utexas.cs.nn.tasks.mspacman.objectives.TimeToEatAllGhostsScore;
import edu.utexas.cs.nn.util.ClassCreation;
import edu.utexas.cs.nn.util.datastructures.Pair;
import edu.utexas.cs.nn.util.datastructures.Triple;
import edu.utexas.cs.nn.util.random.RandomNumbers;
import edu.utexas.cs.nn.util.stats.Average;
import edu.utexas.cs.nn.util.stats.Max;
import edu.utexas.cs.nn.util.stats.Mode;
import edu.utexas.cs.nn.util.stats.Statistic;
import pacman.Executor;
import pacman.controllers.NewGhostController;
import pacman.controllers.NewPacManController;
import pacman.game.Constants;
import pacman.game.Game;

/**
 * Ms. Pac-Man vs. four ghosts across four mazes.
 * 
 * @author Jacob Schrum
 * @param <T> phenotype of evolved agent
 */
public class MsPacManTask<T extends Network> extends NoisyLonerTask<T>implements TUGTask, NetworkTask, HyperNEATTask {

	// Approximate size of Ms. Pac-Man screen in terms of nodes
	public static final int MS_PAC_MAN_NODE_WIDTH = 112;
	public static final int MS_PAC_MAN_NODE_HEIGHT = 120;
	// Number of nodes between pills (effective cell size)
	public static final int MS_PAC_MAN_NODE_DIM = 4;
	// Size of substrate in terms of cells
	public static final int MS_PAC_MAN_SUBSTRATE_WIDTH = MS_PAC_MAN_NODE_WIDTH/MS_PAC_MAN_NODE_DIM;
	public static final int MS_PAC_MAN_SUBSTRATE_HEIGHT = MS_PAC_MAN_NODE_HEIGHT/MS_PAC_MAN_NODE_DIM;
	public static final int MS_PAC_MAN_SUBSTRATE_SIZE = MS_PAC_MAN_SUBSTRATE_WIDTH * MS_PAC_MAN_SUBSTRATE_HEIGHT;
	// Substrate index that deals with power pills
	public static final int POWER_PILL_SUBSTRATE_INDEX = 1; 
	public List<Substrate> subs = null; // filled below
	public List<Triple<String, String, Boolean>> connections = null; // filled below
	public HashMap<Integer, List<Substrate>> substratesForMaze = new HashMap<Integer, List<Substrate>>();
	public static String saveFilePrefix = "";
	//boolean variables
	protected boolean deterministic;
	protected boolean ignorePillScore;
	protected boolean noPills;
	protected boolean noPowerPills;
	protected boolean endAfterGhostEatingChances;
	protected boolean luringTask;
	protected boolean removePillsNearPowerPills;
	protected boolean exitLairEdible;
	protected boolean endOnlyOnTimeLimit;
	protected boolean randomLairExit;
	protected boolean lairExitDatabase;
	protected boolean simultaneousLairExit;
	protected boolean ghostsStartOutsideLair;
	protected boolean onlyOneLairExitAllowed;
	protected boolean evolveGhosts;
	//objectives and scores used for multitask
	protected ArrayList<MsPacManObjective<T>> objectives;
	protected ArrayList<MsPacManObjective<T>> otherScores;
	protected GhostControllerFacade ghosts;
	protected PacManControllerFacade mspacman;
	protected ExecutorFacade exec;
	protected GameFacade game;
	//indices used for calculation of fitness TODO
	private final int scoreIndexInOtherScores;
	private final int pillScoreIndexInOtherScores;
	private final int ghostRewardIndexInOtherScores;
	@SuppressWarnings("unused")
	private final int maxScoreIndexInOtherScores;
	@SuppressWarnings("unused")
	private final int maxPillScoreIndexInOtherScores;
	@SuppressWarnings("unused")
	private final int maxGhostRewardIndexInOtherScores;
	private final int properPowerPillIndexInOtherScores;
	private final int improperPowerPillIndexInOtherScores;
	private final int luringScoreIndexInOtherScores;
	private final int specificLevelScoreFirstIndexInOtherScores;
	private final int powerPillEatenWhenGhostFarIndexInOtherScores;
	private final int avgLevelIndexInOtherScores;
	@SuppressWarnings("unused")
	private final int maxLevelIndexInOtherScores;
	@SuppressWarnings("unused")
	private final int ghostRegretScoreInOtherScores;
	private final int ghostsEatenIndexInOtherScores;
	protected int rawTimeScoreIndex = -1;
	protected int usedGhostScoreIndex = -1;
	public int[] pillTimeFrameIndices;
	public int[] ghostTimeFrameIndices;
	private final boolean eachComponentTracksScoreToo;
	private final boolean plainGhostScore;
	private final TrainingCampManager tcManager;

	/**
	 * Default constructor
	 */
	public MsPacManTask() {
		this(Parameters.parameters.booleanParameter("deterministic"));
	}

	/**
	 * Constructor
	 * @param det boolean parameter "deterministic"
	 */
	public MsPacManTask(boolean det) {
		super();//constructor for noisy loner task
		exec = new ExecutorFacade(new Executor());
		this.deterministic = det;//if game is deterministic
		tcManager = new TrainingCampManager();

		//variables from command line parameters
		onlyOneLairExitAllowed = false;
		evolveGhosts = Parameters.parameters.booleanParameter("evolveGhosts");
		boolean rawScorePacMan = Parameters.parameters.booleanParameter("rawScorePacMan");
		boolean clearTimeScore = Parameters.parameters.booleanParameter("clearTimeScore");
		boolean rewardFasterGhostEating = Parameters.parameters.booleanParameter("rewardFasterGhostEating");
		boolean rewardFasterPillEating = Parameters.parameters.booleanParameter("rewardFasterPillEating");
		luringTask = Parameters.parameters.booleanParameter("luringTask");
		removePillsNearPowerPills = Parameters.parameters.booleanParameter("removePillsNearPowerPills");
		simultaneousLairExit = Parameters.parameters.booleanParameter("simultaneousLairExit");
		exitLairEdible = Parameters.parameters.booleanParameter("exitLairEdible");
		endOnlyOnTimeLimit = Parameters.parameters.booleanParameter("endOnlyOnTimeLimit");
		endAfterGhostEatingChances = Parameters.parameters.booleanParameter("endAfterGhostEatingChances");
		noPills = Parameters.parameters.booleanParameter("noPills");
		noPowerPills = Parameters.parameters.booleanParameter("noPowerPills");
		ignorePillScore = Parameters.parameters.booleanParameter("ignorePillScore");
		eachComponentTracksScoreToo = Parameters.parameters.booleanParameter("eachComponentTracksScoreToo");
		plainGhostScore = Parameters.parameters.booleanParameter("plainGhostScore");
		//used in constructor, not global
		boolean avgGhostsPerPowerPill = Parameters.parameters.booleanParameter("avgGhostsPerPowerPill");
		boolean punishDeadSpace = Parameters.parameters.booleanParameter("punishDeadSpace");
		boolean randomSelection = Parameters.parameters.booleanParameter("randomSelection");

		objectives = new ArrayList<MsPacManObjective<T>>(17);//why 17? TODO
		otherScores = new ArrayList<MsPacManObjective<T>>(17);//why 17? TODO
		if (randomSelection) {//command line parameter, "Only objective is a random objective"
			addObjective(new RandomScore<T>(), objectives, true);//random score since only objective
		} else if (rawScorePacMan) {// "Pac-Man uses Game Score as only fitness"
			addObjective(new GameScore<T>(), objectives, true);
		} else if (luringTask) {//"Pac-Man rewarded for luring ghosts to power pills before eating pill"
			addObjective(new LuringScore<T>(), objectives, true);//
		} else if (Parameters.parameters.booleanParameter("individualLevelFitnesses")) {
			for (int i = 0; i < Constants.NUM_MAZES; i++) {//"One fitness function for each level"
				addObjective(new LevelGameScore<T>(i), objectives, new Average(), true);
			}
		} else {//more than one objective is a random objective
			if (!noPowerPills && CommonConstants.numActiveGhosts > 0) {//power pills still present and ghosts still active
				if (!Parameters.parameters.booleanParameter("ignoreGhostScores")) {//"No fitness from edible ghosts in Ms Pac-Man, even though there are present"
					usedGhostScoreIndex = objectives.size();//keeps track of ghost scores since not used by agent TODO
					if (avgGhostsPerPowerPill) {//command line parameter, "Ghost score used is the average eaten per power pill eaten"
						addObjective(new GhostsPerPowerPillScore<T>(true), objectives, true);
					} else if (rewardFasterGhostEating) {//command line parameter, "Ghost reward fitness gives higher fitness to eating ghosts quickly after power pills"
						addObjective(new FastGhostEatingScore<T>(), objectives, true);
					} else if (plainGhostScore) {//command line parameter, "For ghost fitness, just use eaten ghosts instead of ghost score"
						addObjective(new EatenGhostScore<T>(), objectives, true);
					} else {//otherwise, ghost fitness determined by proportional ms. pac man and ghost score 
						addObjective(new GhostRewardScore<T>(), objectives, true);
					}
					//command line parameter, "Include negative fitness for ghosts that pacman fails to eat"
					if (Parameters.parameters.booleanParameter("ghostRegretFitness")) {
						addObjective(new GhostRegretScore<T>(), objectives, true);
					}//command line parameter, "Fitness based on time to eat all ghosts after power pill"
					if (Parameters.parameters.booleanParameter("timeToEatAllFitness")) {
						addObjective(new TimeToEatAllGhostsScore<T>(), objectives, true);
					}
				}//command line parameter,  "Fitness for eating power pills when all ghosts are threats"
				if (Parameters.parameters.booleanParameter("awardProperPowerPillEating")) {
					addObjective(new ProperlyEatenPowerPillScore<T>(), objectives, true);
				}
				if (Parameters.parameters.booleanParameter("punishImproperPowerPillEating")) {
					addObjective(new ImproperlyEatenPowerPillScore<T>(), objectives, true);
				}
			}
			if (!ignorePillScore && !noPills) {
				if (rewardFasterPillEating) {
					addObjective(new FastPillEatingScore<T>(), objectives, true);
				} else {
					addObjective(new PillScore<T>(), objectives, true);
				}
			}
		}
		if (Parameters.parameters.booleanParameter("rawTimeScore")) {
			rawTimeScoreIndex = objectives.size();
			addObjective(new RawTimeScore<T>(), objectives, true);
		}
		if (punishDeadSpace) {
			addObjective(new AvoidDeadSpaceScore<T>(), objectives, true);
		}
		if (clearTimeScore && CommonConstants.justMaze != -1) {
			addObjective(new ClearTimeScore<T>(), objectives, true);
		}
		if (Parameters.parameters.booleanParameter("livesObjective")) {
			addObjective(new RemainingLivesScore<T>(), objectives, true);
		}
		if (Parameters.parameters.booleanParameter("levelObjective")) {
			addObjective(new LevelScore<T>(), objectives, true);
		}
		if (Parameters.parameters.booleanParameter("consistentLevelObjective")) {
			addObjective(new LevelScore<T>(), objectives, new Mode(), true);
		}
		if (Parameters.parameters.booleanParameter("pacManTimeFitness")) {
			addObjective(new SurvivalAndSpeedTimeScore<T>(), objectives, true);
		}
		if (Parameters.parameters.booleanParameter("pacManLureFitness")) {
			addObjective(new LuringScore<T>(), objectives, true);
		}
		// Game Score
		scoreIndexInOtherScores = otherScores.size();
		addObjective(new GameScore<T>(), otherScores, new Average(), false);
		maxScoreIndexInOtherScores = otherScores.size();
		addObjective(new GameScore<T>(), otherScores, new Max(), false);
		// Pill Score
		pillScoreIndexInOtherScores = otherScores.size();
		addObjective(new PillScore<T>(), otherScores, new Average(), false);
		maxPillScoreIndexInOtherScores = otherScores.size();
		addObjective(new PillScore<T>(), otherScores, new Max(), false);
		// Ghost Reward
		addObjective(new GhostsPerPowerPillScore<T>(true), otherScores, new Average(), false);
		addObjective(new GhostsPerPowerPillScore<T>(false), otherScores, new Average(), false);
		ghostRewardIndexInOtherScores = otherScores.size();
		addObjective(new GhostRewardScore<T>(), otherScores, new Average(), false);
		maxGhostRewardIndexInOtherScores = otherScores.size();
		addObjective(new GhostRewardScore<T>(), otherScores, new Max(), false);
		// Level Scores
		avgLevelIndexInOtherScores = otherScores.size();
		addObjective(new LevelScore<T>(), otherScores, new Average(), false);
		maxLevelIndexInOtherScores = otherScores.size();
		addObjective(new LevelScore<T>(), otherScores, new Max(), false);
		addObjective(new LevelScore<T>(), otherScores, new Mode(), false);
		// Ghosts Eaten
		ghostsEatenIndexInOtherScores = otherScores.size();
		addObjective(new EatenGhostScore<T>(), otherScores, new Average(), false);
		addObjective(new EatenGhostScore<T>(), otherScores, new Max(), false);
		// Missed Ghosts
		ghostRegretScoreInOtherScores = otherScores.size();
		addObjective(new GhostRegretScore<T>(), otherScores, new Average(), false);
		// Luring
		luringScoreIndexInOtherScores = otherScores.size();
		addObjective(new LuringScore<T>(), otherScores, false);
		// How/When Power Pills Are Eaten
		properPowerPillIndexInOtherScores = otherScores.size();
		addObjective(new ProperlyEatenPowerPillScore<T>(), otherScores, false);
		improperPowerPillIndexInOtherScores = otherScores.size();
		addObjective(new ImproperlyEatenPowerPillScore<T>(), otherScores, false);
		powerPillEatenWhenGhostFarIndexInOtherScores = otherScores.size();
		addObjective(new PowerPillEatenWhenGhostFarScore<T>(), otherScores, false);

		addObjective(new SurvivalAndSpeedTimeScore<T>(), otherScores, new Average(), false);
		addObjective(new SurvivalAndSpeedTimeScore<T>(), otherScores, new Max(), false);

		if (MMNEAT.taskHasSubnetworks()) {
			boolean eligibilityOnEarnedFitness = Parameters.parameters.booleanParameter("eligibilityOnEarnedFitness");
			pillTimeFrameIndices = new int[MMNEAT.modesToTrack];
			ghostTimeFrameIndices = new int[MMNEAT.modesToTrack];
			for (int i = 0; i < MMNEAT.modesToTrack; i++) {
				pillTimeFrameIndices[i] = otherScores.size();
				addObjective(eligibilityOnEarnedFitness ? new EligibilityTimeFramesPillScore<T>(i)
						: new TimeFramesPillScore<T>(i), otherScores, false);
				ghostTimeFrameIndices[i] = otherScores.size();
				addObjective(eligibilityOnEarnedFitness ? new EligibilityTimeFramesGhostScore<T>(i)
						: new TimeFramesGhostScore<T>(i), otherScores, false);
			}
		}

		specificLevelScoreFirstIndexInOtherScores = otherScores.size();
		for (int i = 0; i < Constants.NUM_MAZES; i++) {
			addObjective(new LevelGameScore<T>(i), otherScores, new Average(), false);
		}
		// Kind of a silly way to track this, but easy
		addObjective(new EdibleTimeParameter<T>(), otherScores, new Average(), false);
		addObjective(new LairTimeParameter<T>(), otherScores, new Average(), false);
	}

	/**
	 * Based on a designation from the fitness mode map, return a collection of
	 * fitness values.
	 *
	 * @param id
	 *            an id from FitnessToModeMap
	 * @param taskScores
	 * @return array of designated fitness values
	 */
	public double[] fitnessArray(int id, Score<T> taskScores) {
		int scoreIndex;
		switch (id) {
		case MsPacManModeSelector.ACTIVE_GHOST_SCORE:
			// Assumes the first subnet is always the ghost network
			scoreIndex = ghostTimeFrameIndices[0];
			break;
		case MsPacManModeSelector.ACTIVE_PILL_SCORE:
			// Assumes the second subnet is always the pill network
			scoreIndex = pillTimeFrameIndices[1];
			break;
		case MsPacManModeSelector.LEVEL_SCORE:
			scoreIndex = avgLevelIndexInOtherScores;
			break;
		case MsPacManModeSelector.GAME_SCORE:
			scoreIndex = scoreIndexInOtherScores;
			break;
		case MsPacManModeSelector.GHOST_SCORE:
			scoreIndex = plainGhostScore ? ghostsEatenIndexInOtherScores : ghostRewardIndexInOtherScores;
			break;
		case MsPacManModeSelector.PILL_SCORE:
			scoreIndex = pillScoreIndexInOtherScores;
			break;
		case MsPacManModeSelector.PROPER_POWER_PILL_SCORE:
			scoreIndex = properPowerPillIndexInOtherScores;
			break;
		case MsPacManModeSelector.LURING_FITNESS:
			scoreIndex = luringScoreIndexInOtherScores;
			break;
		case MsPacManModeSelector.IMPROPER_POWER_PILL_SCORE:
			scoreIndex = improperPowerPillIndexInOtherScores;
			break;
		case MsPacManModeSelector.GHOST_AND_LEVEL_COMBO:
			return new double[] { taskScores.otherStats[plainGhostScore ? ghostsEatenIndexInOtherScores
					: ghostRewardIndexInOtherScores], taskScores.otherStats[avgLevelIndexInOtherScores] };
		case MsPacManModeSelector.PROPER_POWER_PILL_GHOST_COMBO:
			return new double[] {
					taskScores.otherStats[plainGhostScore ? ghostsEatenIndexInOtherScores
							: ghostRewardIndexInOtherScores],
					taskScores.otherStats[properPowerPillIndexInOtherScores] };
		case MsPacManModeSelector.IMPROPER_POWER_PILL_GHOST_COMBO:
			return new double[] {
					taskScores.otherStats[plainGhostScore ? ghostsEatenIndexInOtherScores
							: ghostRewardIndexInOtherScores],
					taskScores.otherStats[improperPowerPillIndexInOtherScores] };
		case MsPacManModeSelector.PILL_AND_NO_POWER_PILL_COMBO:
			return new double[] { taskScores.otherStats[pillScoreIndexInOtherScores],
					taskScores.otherStats[powerPillEatenWhenGhostFarIndexInOtherScores] };
		case MsPacManModeSelector.NO_PREFERENCE:
			// Just use all standard scores, whatever they are
			return Arrays.copyOf(taskScores.scores, taskScores.scores.length);
		default:
			scoreIndex = -100; // error value
			// Check specific ghosts
			for (int j = 0; j < MsPacManModeSelector.SPECIFIC_GHOSTS.length; j++) {
				if (id == MsPacManModeSelector.SPECIFIC_GHOSTS[j]) {
					scoreIndex = taskScores.otherStats.length - CommonConstants.numActiveGhosts + j;
					break;
				}
			}
			// Check specific levels
			for (int j = 0; j < MsPacManModeSelector.SPECIFIC_LEVELS.length; j++) {
				if (id == MsPacManModeSelector.SPECIFIC_LEVELS[j]) {
					scoreIndex = specificLevelScoreFirstIndexInOtherScores + j;
					break;
				}
			}
			// score not found
			if (scoreIndex == -100) {
				System.out.println("Error! Fitness index does not exist: " + id);
				System.exit(1);
			}
			break;
		}
		// Game score is fitness in addition to preferred
		return (eachComponentTracksScoreToo && scoreIndex != scoreIndexInOtherScores 
				? new double[] { taskScores.otherStats[scoreIndex], taskScores.otherStats[scoreIndexInOtherScores] }
		: new double[] { taskScores.otherStats[scoreIndex] });
	}

	/**
	 * Define the Ghost team to evolve against
	 */
	public void loadGhosts() {
		if (ghosts == null) {
			try {
				this.ghosts = new GhostControllerFacade((NewGhostController) ClassCreation.createObject("ghostTeam"));
			} catch (NoSuchMethodException ex) {
				ex.printStackTrace();
				System.exit(1);
			}
		} else {
			ghosts.reset();
		}
	}

	/**
	 * If a static pacman is being used against evolving ghosts, then this
	 * method loads it.
	 * 
	 * Is this actually used anywhere?
	 */
	public void loadPacMan() {
		if (mspacman == null) {
			try {
				this.mspacman = new PacManControllerFacade((NewPacManController) ClassCreation.createObject("staticPacMan"));
			} catch (NoSuchMethodException ex) {
				ex.printStackTrace();
				System.exit(1);
			}
		} else {
			mspacman.reset();
		}
	}

	@SuppressWarnings("rawtypes")
	public final void addObjective(MsPacManObjective o, ArrayList<MsPacManObjective<T>> list, boolean affectsSelection) {
		addObjective(o, list, null, affectsSelection);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public final void addObjective(MsPacManObjective o, ArrayList<MsPacManObjective<T>> list, Statistic override, boolean affectsSelection) {
		list.add(o);
		MMNEAT.registerFitnessFunction(o.getClass().getSimpleName(), override, affectsSelection);
	}

	@Override
	public Score<T> evaluate(Genotype<T> individual) {
		exec.log("Genotype ID: " + individual.getId());
		return super.evaluate(individual);
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Pair<double[], double[]> oneEval(Genotype<T> individual, int num) {
		Organism<T> organism = evolveGhosts ? new SharedNNGhosts<T>(individual) : new NNMsPacMan<T>(individual);
		if (evolveGhosts) {
			loadPacMan();
			ghosts = new GhostControllerFacade((NewGhostController) ((SharedNNGhosts<T>) organism).controller);
		} else {
			mspacman = new PacManControllerFacade((NewPacManController) ((NNMsPacMan<T>) organism).controller);
		}

		// Side-effects to "game"
		agentEval(mspacman, num);
		if (mspacman.newP instanceof MultinetworkMsPacManController && individual instanceof NetworkGenotype) {
			// Track subnet selections as if they were modes
			((NetworkGenotype<T>) individual).setModuleUsage(((MultinetworkMsPacManController) mspacman.newP).fullUsage);
		}

		double[] fitnesses = new double[this.numObjectives()];
		double[] scores = new double[this.numOtherScores()];
		// When evolving ghosts, all fitness scores are flipped to negative,
		// because the ghosts are in direct opposition to pacman
		for (int j = 0; j < objectives.size(); j++) {
			fitnesses[j] = (evolveGhosts ? -1 : 1) * objectives.get(j).score(game, organism);
		}
		for (int j = 0; j < otherScores.size(); j++) {
			scores[j] = otherScores.get(j).score(game, organism);
		}
		return new Pair<double[], double[]>(fitnesses, scores);
	}

	public GameFacade agentEval(PacManControllerFacade mspacman, int num) {
		// System.out.println("Agent Eval");
		if (!evolveGhosts) {
			loadGhosts();
		}
		tcManager.preEval();
		game = new GameFacade(new Game(deterministic ? num : RandomNumbers.randomGenerator.nextLong()));
		game.setExitLairEdible(exitLairEdible);
		game.setEndOnlyOnTimeLimit(endOnlyOnTimeLimit);
		game.setRandomLairExit(randomLairExit);
		game.setLairExitDatabase(lairExitDatabase);
		game.setSimultaneousLairExit(simultaneousLairExit);
		game.setGhostsStartOutsideLair(ghostsStartOutsideLair);
		game.setOnlyOneLairExitAllowed(onlyOneLairExitAllowed);
		game.setEndAfterGhostEatingChances(endAfterGhostEatingChances);
		game.setRemovePillsNearPowerPills(removePillsNearPowerPills);
		game.playWithoutPills(noPills);
		game.playWithoutPowerPills(noPowerPills);
		game.setEndAfterPowerPillsEaten(luringTask);
		int campNum = tcManager.campSetup(game, num);
		int startingLevel = game.getCurrentLevel();
		mspacman.reset();
		if (CommonConstants.recordPacman) {
			exec.runGameTimedRecorded(game, mspacman, ghosts, CommonConstants.watch,
					saveFilePrefix + Parameters.parameters.stringParameter("pacmanSaveFile"));
			// } else if(CommonConstants.replayPacman){
			// exec.replayGame(Parameters.parameters.stringParameter("pacmanSaveFile"),
			// CommonConstants.watch);
		} else if (CommonConstants.watch) {
			// System.out.println("Watch game: " + mspacman);
			exec.runGameTimed(mspacman, ghosts, game);
			// exec.runGameTimed(new HumanController(new KeyBoardInput()),ghosts, true, game);
		} else if (CommonConstants.timedPacman) {
			exec.runGameTimedNonVisual(game, mspacman, ghosts);
		} else {
			exec.runExperiment(mspacman, ghosts, game);
		}
		tcManager.postEval(game, campNum, startingLevel);
		if (MMNEAT.evalReport != null) {
			mspacman.logEvaluationDetails();
		}

		return game;
	}

	@Override
	public int numOtherScores() {
		return otherScores.size();
	}

	@Override
	public int numObjectives() {
		return objectives.size();
	}

	/**
	 * All zeroes, since objectives are positive
	 *
	 * @return
	 */
	@Override
	public double[] minScores() {
		double[] result = new double[numObjectives()];
		for (int i = 0; i < result.length; i++) {
			result[i] = objectives.get(i).minScore();
		}
		return result;
	}

	@Override
	public double[] startingGoals() {
		return minScores();
	}

	@Override
	public String[] sensorLabels() {
		return MMNEAT.pacmanInputOutputMediator.sensorLabels();
	}

	@Override
	public String[] outputLabels() {
		return MMNEAT.pacmanInputOutputMediator.outputLabels();
	}

	@Override
	public double getTimeStamp() {
		return game.getTotalTime();
	}



	@Override
	public List<Substrate> getSubstrateInformation() {
		if(subs == null) {
			subs = getSubstrateInformationFromScratch();
		}
		return subs;
	}

	/**
	 * Full substrate without dead neurons for specific mazes
	 * @return
	 */
	public List<Substrate> getSubstrateInformationFromScratch() {
		
		int numInputSubstrates = getNumInputSubstrates();
		
		List<Triple<String,Integer,Integer>> output = new LinkedList<>();
		if(Parameters.parameters.booleanParameter("pacManFullScreenOutput")) {
			output.add(new Triple<>("DesiredLocation",MS_PAC_MAN_SUBSTRATE_WIDTH, MS_PAC_MAN_SUBSTRATE_HEIGHT));
		}else {
			output.add(new Triple<>("Direction",3,3));
		}
		List<Substrate> substrateInformation = HyperNEATUtil.getSubstrateInformation(MS_PAC_MAN_SUBSTRATE_WIDTH, MS_PAC_MAN_SUBSTRATE_HEIGHT, numInputSubstrates, output);
		// Will always be the second substrate: compact representation may replace full screen version
		if(!Parameters.parameters.booleanParameter("pacmanFullScreenPowerInput")) {
			Substrate originalPowerPillSubstrate = substrateInformation.get(POWER_PILL_SUBSTRATE_INDEX); // Always the second substrate
			Substrate powerPillSubstrate = new Substrate(new Pair<Integer, Integer>(2, 2), Substrate.INPUT_SUBSTRATE, originalPowerPillSubstrate.getSubLocation(), originalPowerPillSubstrate.getName());		
			substrateInformation.set(POWER_PILL_SUBSTRATE_INDEX, powerPillSubstrate);
		}

		// Provide a way to resize the processing substrates		
		if(!Parameters.parameters.booleanParameter("pacmanFullScreenProcess")) { // Will NOT work with convolution
			int current = numInputSubstrates;
			while(substrateInformation.get(current).getName().startsWith("process")) {
				Substrate originalProcessingSubstrate = substrateInformation.get(current);
				// 10 by 10 are magic numbers ... configure? Move into HyperNEATUtil.getSubstrateInformation?
				Substrate newProcessingSubstrate = new Substrate(new Pair<Integer, Integer>(10, 10), Substrate.PROCCESS_SUBSTRATE, originalProcessingSubstrate.getSubLocation(), originalProcessingSubstrate.getName());		
				substrateInformation.set(current, newProcessingSubstrate);
				current++;
			}			
		} 		

		// Kill some neurons in the D-pad
		if(!Parameters.parameters.booleanParameter("pacManFullScreenOutput")) {
			//kills neurons in corner and center of output substrate
			substrateInformation.get(substrateInformation.size() - 1).addDeadNeuron(0, 0);
			substrateInformation.get(substrateInformation.size() - 1).addDeadNeuron(2, 0);
			substrateInformation.get(substrateInformation.size() - 1).addDeadNeuron(1, 1);
			substrateInformation.get(substrateInformation.size() - 1).addDeadNeuron(0, 2);
			substrateInformation.get(substrateInformation.size() - 1).addDeadNeuron(2, 2);
		}

		return substrateInformation;
	}

	private int getNumInputSubstrates() {
		int numInputSubstrates = 0;
		numInputSubstrates += 2; // One for regular pills, and one for power pills
		numInputSubstrates += Parameters.parameters.booleanParameter("pacmanBothThreatAndEdibleSubstrate") ? 2 : 1; // 2 or 1 substrate for ghosts
		numInputSubstrates += 1; // A substrate for Ms. Pac-Man's location
		return numInputSubstrates;
	}

	@Override
	public List<Triple<String, String, Boolean>> getSubstrateConnectivity() {
		if(connections == null) {
			List<String> outputNames = new LinkedList<>();
			if(Parameters.parameters.booleanParameter("pacManFullScreenOutput")) {
				outputNames.add("DesiredLocation");
			}else {
				outputNames.add("Direction");
			}
			connections = HyperNEATUtil.getSubstrateConnectivity(getNumInputSubstrates(), outputNames);			
			// The four input power pill substrate does not contain visual information
			if(!Parameters.parameters.booleanParameter("pacmanFullScreenPowerInput")) {
				for(Triple<String, String, Boolean> triple : connections) { // So do not allow convolution
					if(triple.t1.equals("Input(" + POWER_PILL_SUBSTRATE_INDEX + ")")) { // Power pill substrate
						triple.t3 = Boolean.FALSE; // Convolution not allowed
					}
					
				}
			}
		}
		return connections;
	}

	/**
	 * Figures out which neurons can be killed in the
	 * current maze.
	 * 
	 * @param gf game facade, which accesses current maze
	 */
	public void customizeSubstratesForMaze(GameFacade gf) {
		int mazeIndex = gf.getMazeIndex();
		if(!substratesForMaze.containsKey(mazeIndex)) {
			List<Substrate> localSubs = getSubstrateInformationFromScratch(); // Just to make sure subs is not null
			for(Substrate s : localSubs){
				// Only reset full screen substrates that are not processing layers
				if(s.getSize().t1 == MS_PAC_MAN_SUBSTRATE_WIDTH && s.getSize().t2 == MS_PAC_MAN_SUBSTRATE_HEIGHT && s.getStype() != Substrate.PROCCESS_SUBSTRATE) {
					s.killAllNeurons();
					for(int i = 0; i < gf.lengthMaze(); i++) {
						int x = gf.getNodeXCoord(i);
						int y = gf.getNodeYCoord(i);
						int scaledX = x / MsPacManTask.MS_PAC_MAN_NODE_DIM;
						int scaledY = y / MsPacManTask.MS_PAC_MAN_NODE_DIM;
						s.resurrectNeuron(scaledX, scaledY);
					}
				}
			}
			substratesForMaze.put(mazeIndex, localSubs);
		}
		subs = substratesForMaze.get(mazeIndex);
	}
	
	/**
	 * Default behavior
	 */
	@Override
	public int numCPPNInputs() {
		return HyperNEATTask.DEFAULT_NUM_CPPN_INPUTS;
	}

	/**
	 * Default behavior
	 */
	@Override
	public double[] filterCPPNInputs(double[] fullInputs) {
		return fullInputs;
	}	
}
