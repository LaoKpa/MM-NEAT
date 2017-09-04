package edu.utexas.cs.nn.tasks.gridTorus;

import static org.junit.Assert.*;

import java.util.ArrayList;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import edu.utexas.cs.nn.MMNEAT.MMNEAT;
import edu.utexas.cs.nn.evolution.genotypes.TWEANNGenotype;
import edu.utexas.cs.nn.gridTorus.controllers.PreyFleeClosestPredatorController;
import edu.utexas.cs.nn.gridTorus.controllers.RandomPredatorController;
import edu.utexas.cs.nn.gridTorus.controllers.TorusPredPreyController;
import edu.utexas.cs.nn.networks.Network;
import edu.utexas.cs.nn.parameters.Parameters;
import edu.utexas.cs.nn.tasks.gridTorus.competitive.CompetitiveHomogeneousPredatorsVsPreyTask;
import edu.utexas.cs.nn.tasks.gridTorus.cooperative.CooperativePredatorsVsStaticPreyTask;
import edu.utexas.cs.nn.tasks.gridTorus.cooperative.CooperativePreyVsStaticPredatorsTask;
import edu.utexas.cs.nn.tasks.gridTorus.cooperativeAndCompetitive.CompetitiveAndCooperativePredatorsVsPreyTask;
import edu.utexas.cs.nn.tasks.gridTorus.objectives.GridTorusObjective;
import edu.utexas.cs.nn.tasks.gridTorus.objectives.PredatorCatchCloseObjective;
import edu.utexas.cs.nn.tasks.gridTorus.objectives.PredatorCatchCloseQuickObjective;
import edu.utexas.cs.nn.tasks.gridTorus.objectives.PredatorCatchObjective;
import edu.utexas.cs.nn.tasks.gridTorus.objectives.PredatorEatEachPreyQuicklyObjective;
import edu.utexas.cs.nn.tasks.gridTorus.objectives.PredatorMinimizeDistanceFromPreyObjective;
import edu.utexas.cs.nn.tasks.gridTorus.objectives.PredatorMinimizeGameTimeObjective;
import edu.utexas.cs.nn.tasks.gridTorus.objectives.PreyLongSurvivalTimeObjective;
import edu.utexas.cs.nn.tasks.gridTorus.objectives.PreyMaximizeDistanceFromPredatorsObjective;
import edu.utexas.cs.nn.tasks.gridTorus.objectives.PreyMaximizeGameTimeObjective;
import edu.utexas.cs.nn.tasks.gridTorus.objectives.PreyMinimizeCaughtObjective;
import edu.utexas.cs.nn.tasks.gridTorus.objectives.PreyRawalRajagopalanMiikkulainenObjective;

public class TorusPredPreyTaskTest <T extends Network> {
	
	public static final double doubleThreshold = .001;
	
	//NOTE: numOthers depends on how many fitnesses are currently added to other scores.
	//NOTE: If this is failing, probably because there are not 22 other scores anymore.
	private static final int numOthers = 22;
	
	//NOTE: numOthersCompetitive depends on how many fitnesses are currently added to other scores.
	//NOTE: If this is failing, probably because there are not 37 other scores anymore.
	private static final int numOthersCompetitive = 37;

	@SuppressWarnings("rawtypes")
	private static TorusEvolvedPredatorsVsStaticPreyTask homoPred;
	@SuppressWarnings("rawtypes")
	private static TorusEvolvedPreyVsStaticPredatorsTask homoPrey;
	@SuppressWarnings("rawtypes")
	private static CooperativePredatorsVsStaticPreyTask coPred;
	@SuppressWarnings("rawtypes")
	private static CooperativePreyVsStaticPredatorsTask coPrey;
	@SuppressWarnings("rawtypes")
	private static CompetitiveHomogeneousPredatorsVsPreyTask homoComp;
	@SuppressWarnings("rawtypes")
	private static CompetitiveAndCooperativePredatorsVsPreyTask coComp;

	@Before
	public void setUp() throws Exception {
		//NOTE: MAKE SURE THAT THE BELOW PARAMETER INITIALIZATION SETS THE DEFAULT FITNESSES TO FALSE
		Parameters.initializeParameterCollections(new String[] { "io:false", "netio:false", "torusTimeLimit:1000",
				"torusPreys:2", "torusPredators:3", "allowDoNothingActionForPredators:false", "torusSenseTeammates:true", 
				"staticPreyController:edu.utexas.cs.nn.gridTorus.controllers.PreyFleeClosestPredatorController",
				"torusSenseByProximity:true", "predatorCatchClose:false", "preyRRM:false" });
		MMNEAT.loadClasses();
	}

	@After
	public void tearDown() throws Exception {
		homoPred = null;
		homoPrey = null;
		coPred = null;
		coPrey = null;
		homoComp = null;
		coComp = null;
		MMNEAT.clearClasses();
	}
	
	@SuppressWarnings({ "rawtypes", "static-access" })
	@Test
	public void testConstructors() {
		MMNEAT.task = new TorusEvolvedPredatorsVsStaticPreyTask();
		homoPred = (TorusEvolvedPredatorsVsStaticPreyTask)MMNEAT.task;
		assertEquals(homoPred.preyEvolve, false);
		assertEquals(homoPred.competitive, false);
		assertEquals(homoPred.objectives.size(), 1);

		MMNEAT.task = new TorusEvolvedPreyVsStaticPredatorsTask();
		homoPrey = (TorusEvolvedPreyVsStaticPredatorsTask)MMNEAT.task;
		assertEquals(homoPrey.preyEvolve, true);
		assertEquals(homoPrey.competitive, false);
		assertEquals(homoPrey.objectives.size(), 1);

		MMNEAT.task = new CooperativePredatorsVsStaticPreyTask();
		coPred = (CooperativePredatorsVsStaticPreyTask)MMNEAT.task;
		assertEquals(coPred.getLonerTaskInstance().preyEvolve, false);
		assertEquals(coPred.getLonerTaskInstance().competitive, false);
		assertEquals(coPred.getLonerTaskInstance().objectives.size(), 3);
		assertEquals(coPred.getLonerTaskInstance().objectives.size(), coPred.numberOfPopulations());

		MMNEAT.task = new CooperativePreyVsStaticPredatorsTask();
		coPrey = (CooperativePreyVsStaticPredatorsTask)MMNEAT.task;
		assertEquals(coPrey.getLonerTaskInstance().preyEvolve, true);
		assertEquals(coPrey.getLonerTaskInstance().competitive, false);
		assertEquals(coPrey.getLonerTaskInstance().objectives.size(), 2);
		assertEquals(coPrey.getLonerTaskInstance().objectives.size(), coPrey.numberOfPopulations());

		MMNEAT.task = new CompetitiveHomogeneousPredatorsVsPreyTask();
		homoComp = (CompetitiveHomogeneousPredatorsVsPreyTask)MMNEAT.task;
		assertEquals(homoComp.getLonerTaskInstance().competitive, true);
		assertEquals(homoComp.getLonerTaskInstance().objectives.size(), 2);
		assertEquals(homoComp.getLonerTaskInstance().objectives.size(), homoComp.numberOfPopulations());

		MMNEAT.task = new CompetitiveAndCooperativePredatorsVsPreyTask();
		coComp = (CompetitiveAndCooperativePredatorsVsPreyTask)MMNEAT.task;
		assertEquals(coComp.getLonerTaskInstance().competitive, true);
		assertEquals(coComp.getLonerTaskInstance().objectives.size(), 5);
		assertEquals(coComp.getLonerTaskInstance().objectives.size(), coComp.numberOfPopulations());
	}

	@SuppressWarnings({ "hiding", "rawtypes", "unchecked" })
	@Test
	public <T extends Network> void testAddObjective(){
		//Test for homogeneous pred vs static prey
		ArrayList<ArrayList<GridTorusObjective<T>>> objectives = new ArrayList<ArrayList<GridTorusObjective<T>>>();
		ArrayList<ArrayList<GridTorusObjective<T>>> otherScores = new ArrayList<ArrayList<GridTorusObjective<T>>>();
		objectives.add(new ArrayList<GridTorusObjective<T>>());
		otherScores.add(new ArrayList<GridTorusObjective<T>>());
		MMNEAT.task = new TorusEvolvedPredatorsVsStaticPreyTask();
		homoPred = (TorusEvolvedPredatorsVsStaticPreyTask)MMNEAT.task;
		assertEquals(objectives.get(0).size(), 0);
		assertEquals(objectives.size(), 1);
		assertEquals(otherScores.get(0).size(), 0);
		assertEquals(otherScores.size(), 1);
		
		homoPred.addObjective(new PredatorCatchCloseObjective(), objectives, 0);
		assertEquals(objectives.get(0).size(), 1);
		assertTrue(objectives.get(0).get(0) instanceof PredatorCatchCloseObjective);
		assertFalse(objectives.get(0).get(0) instanceof PredatorCatchObjective);
		assertEquals(objectives.size(), 1);
		assertTrue(otherScores.get(0).isEmpty());
		
		homoPred.addObjective(new PredatorCatchObjective(), objectives, 0);
		assertEquals(objectives.get(0).size(), 2);
		assertTrue(objectives.get(0).get(0) instanceof PredatorCatchCloseObjective);
		assertFalse(objectives.get(0).get(0) instanceof PredatorCatchObjective);
		assertFalse(objectives.get(0).get(1) instanceof PredatorCatchCloseObjective);
		assertTrue(objectives.get(0).get(1) instanceof PredatorCatchObjective);
		assertEquals(objectives.size(), 1);
		assertTrue(otherScores.get(0).isEmpty());
		
		homoPred.addObjective(new PredatorCatchCloseObjective(), otherScores, false, 0);
		assertEquals(otherScores.get(0).size(), 1);
		assertTrue(otherScores.get(0).get(0) instanceof PredatorCatchCloseObjective);
		assertFalse(otherScores.get(0).get(0) instanceof PredatorCatchObjective);
		assertEquals(otherScores.size(), 1);
		assertFalse(otherScores.get(0).isEmpty());
		assertEquals(objectives.get(0).size(), 2);
		assertTrue(objectives.get(0).get(0) instanceof PredatorCatchCloseObjective);
		assertFalse(objectives.get(0).get(0) instanceof PredatorCatchObjective);
		assertFalse(objectives.get(0).get(1) instanceof PredatorCatchCloseObjective);
		assertTrue(objectives.get(0).get(1) instanceof PredatorCatchObjective);
		assertEquals(objectives.size(), 1);
		
		homoPred.addObjective(new PredatorCatchObjective(), otherScores, false, 0);
		assertEquals(otherScores.get(0).size(), 2);
		assertTrue(otherScores.get(0).get(0) instanceof PredatorCatchCloseObjective);
		assertFalse(otherScores.get(0).get(0) instanceof PredatorCatchObjective);
		assertFalse(otherScores.get(0).get(1) instanceof PredatorCatchCloseObjective);
		assertTrue(otherScores.get(0).get(1) instanceof PredatorCatchObjective);
		assertEquals(otherScores.size(), 1);
		assertEquals(objectives.get(0).size(), 2);
		assertTrue(objectives.get(0).get(0) instanceof PredatorCatchCloseObjective);
		assertFalse(objectives.get(0).get(0) instanceof PredatorCatchObjective);
		assertFalse(objectives.get(0).get(1) instanceof PredatorCatchCloseObjective);
		assertTrue(objectives.get(0).get(1) instanceof PredatorCatchObjective);
		assertEquals(objectives.size(), 1);
		
		
		//Test for homogeneous prey vs static pred
		objectives = new ArrayList<ArrayList<GridTorusObjective<T>>>();
		otherScores = new ArrayList<ArrayList<GridTorusObjective<T>>>();
		objectives.add(new ArrayList<GridTorusObjective<T>>());
		otherScores.add(new ArrayList<GridTorusObjective<T>>());
		MMNEAT.task = new TorusEvolvedPreyVsStaticPredatorsTask();
		homoPrey = (TorusEvolvedPreyVsStaticPredatorsTask)MMNEAT.task;
		assertEquals(objectives.get(0).size(), 0);
		assertEquals(objectives.size(), 1);
		assertEquals(otherScores.get(0).size(), 0);
		assertEquals(otherScores.size(), 1);
		
		homoPrey.addObjective(new PreyLongSurvivalTimeObjective(), objectives, 0);
		assertEquals(objectives.get(0).size(), 1);
		assertTrue(objectives.get(0).get(0) instanceof PreyLongSurvivalTimeObjective);
		assertFalse(objectives.get(0).get(0) instanceof PreyMinimizeCaughtObjective);
		assertEquals(objectives.size(), 1);
		assertTrue(otherScores.get(0).isEmpty());
		
		homoPrey.addObjective(new PreyMinimizeCaughtObjective(), objectives, 0);
		assertEquals(objectives.get(0).size(), 2);
		assertTrue(objectives.get(0).get(0) instanceof PreyLongSurvivalTimeObjective);
		assertFalse(objectives.get(0).get(0) instanceof PreyMinimizeCaughtObjective);
		assertFalse(objectives.get(0).get(1) instanceof PreyLongSurvivalTimeObjective);
		assertTrue(objectives.get(0).get(1) instanceof PreyMinimizeCaughtObjective);
		assertEquals(objectives.size(), 1);
		assertTrue(otherScores.get(0).isEmpty());
		
		homoPrey.addObjective(new PreyLongSurvivalTimeObjective(), otherScores, false, 0);
		assertEquals(otherScores.get(0).size(), 1);
		assertTrue(otherScores.get(0).get(0) instanceof PreyLongSurvivalTimeObjective);
		assertFalse(otherScores.get(0).get(0) instanceof PreyMinimizeCaughtObjective);
		assertEquals(otherScores.size(), 1);
		assertFalse(otherScores.get(0).isEmpty());
		assertEquals(objectives.get(0).size(), 2);
		assertTrue(objectives.get(0).get(0) instanceof PreyLongSurvivalTimeObjective);
		assertFalse(objectives.get(0).get(0) instanceof PreyMinimizeCaughtObjective);
		assertFalse(objectives.get(0).get(1) instanceof PreyLongSurvivalTimeObjective);
		assertTrue(objectives.get(0).get(1) instanceof PreyMinimizeCaughtObjective);
		assertEquals(objectives.size(), 1);
		
		homoPrey.addObjective(new PreyMinimizeCaughtObjective(), otherScores, false, 0);
		assertEquals(otherScores.get(0).size(), 2);
		assertTrue(otherScores.get(0).get(0) instanceof PreyLongSurvivalTimeObjective);
		assertFalse(otherScores.get(0).get(0) instanceof PreyMinimizeCaughtObjective);
		assertFalse(otherScores.get(0).get(1) instanceof PreyLongSurvivalTimeObjective);
		assertTrue(otherScores.get(0).get(1) instanceof PreyMinimizeCaughtObjective);
		assertEquals(otherScores.size(), 1);
		assertEquals(objectives.get(0).size(), 2);
		assertTrue(objectives.get(0).get(0) instanceof PreyLongSurvivalTimeObjective);
		assertFalse(objectives.get(0).get(0) instanceof PreyMinimizeCaughtObjective);
		assertFalse(objectives.get(0).get(1) instanceof PreyLongSurvivalTimeObjective);
		assertTrue(objectives.get(0).get(1) instanceof PreyMinimizeCaughtObjective);
		assertEquals(objectives.size(), 1);
		
		
		//Test for cooperative pred vs static prey
		objectives = new ArrayList<ArrayList<GridTorusObjective<T>>>();
		otherScores = new ArrayList<ArrayList<GridTorusObjective<T>>>();
		objectives.add(new ArrayList<GridTorusObjective<T>>());
		objectives.add(new ArrayList<GridTorusObjective<T>>());
		objectives.add(new ArrayList<GridTorusObjective<T>>());
		otherScores.add(new ArrayList<GridTorusObjective<T>>());
		otherScores.add(new ArrayList<GridTorusObjective<T>>());
		otherScores.add(new ArrayList<GridTorusObjective<T>>());
		MMNEAT.task = new CooperativePredatorsVsStaticPreyTask();
		coPred = (CooperativePredatorsVsStaticPreyTask)MMNEAT.task;
		assertEquals(objectives.get(0).size(), 0);
		assertEquals(objectives.get(1).size(), 0);
		assertEquals(objectives.get(2).size(), 0);
		assertEquals(objectives.size(), 3);
		assertEquals(otherScores.get(0).size(), 0);
		assertEquals(otherScores.get(1).size(), 0);
		assertEquals(otherScores.get(2).size(), 0);
		assertEquals(otherScores.size(), 3);
		
		coPred.getLonerTaskInstance().addObjective(new PredatorEatEachPreyQuicklyObjective(), objectives, 1);
		assertEquals(objectives.get(0).size(), 0);
		assertEquals(objectives.get(1).size(), 1);
		assertEquals(objectives.get(2).size(), 0);
		assertTrue(objectives.get(1).get(0) instanceof PredatorEatEachPreyQuicklyObjective);
		assertEquals(objectives.size(), 3);
		assertEquals(otherScores.size(), 3);
		assertTrue(otherScores.get(0).isEmpty());
		assertTrue(otherScores.get(1).isEmpty());
		assertTrue(otherScores.get(2).isEmpty());
		
		
		coPred.getLonerTaskInstance().addObjective(new PredatorCatchCloseObjective(), objectives, 0);
		assertEquals(objectives.get(0).size(), 1);
		assertEquals(objectives.get(1).size(), 1);
		assertTrue(objectives.get(0).get(0) instanceof PredatorCatchCloseObjective);
		assertFalse(objectives.get(0).get(0) instanceof PredatorCatchObjective);
		assertEquals(objectives.size(), 3);
		assertTrue(otherScores.get(0).isEmpty());
		
		coPred.getLonerTaskInstance().addObjective(new PredatorMinimizeDistanceFromPreyObjective(), otherScores, 2);
		assertEquals(objectives.get(0).size(), 1);
		assertEquals(objectives.get(1).size(), 1);
		assertTrue(objectives.get(1).get(0) instanceof PredatorEatEachPreyQuicklyObjective);
		assertTrue(objectives.get(0).get(0) instanceof PredatorCatchCloseObjective);
		assertFalse(objectives.get(0).get(0) instanceof PredatorCatchObjective);
		assertEquals(objectives.size(), 3);
		assertEquals(otherScores.size(), 3);
		assertTrue(otherScores.get(0).isEmpty());
		assertTrue(otherScores.get(1).isEmpty());
		assertEquals(otherScores.get(2).size(), 1);
		assertTrue(otherScores.get(2).get(0) instanceof PredatorMinimizeDistanceFromPreyObjective);
		
		coPred.getLonerTaskInstance().addObjective(new PredatorCatchObjective(), objectives, 0);
		assertEquals(objectives.get(0).size(), 2);
		assertTrue(objectives.get(0).get(0) instanceof PredatorCatchCloseObjective);
		assertFalse(objectives.get(0).get(0) instanceof PredatorCatchObjective);
		assertFalse(objectives.get(0).get(1) instanceof PredatorCatchCloseObjective);
		assertTrue(objectives.get(0).get(1) instanceof PredatorCatchObjective);
		assertEquals(objectives.size(), 3);
		assertTrue(otherScores.get(0).isEmpty());
		
		coPred.getLonerTaskInstance().addObjective(new PredatorCatchCloseObjective(), otherScores, false, 0);
		assertEquals(otherScores.get(0).size(), 1);
		assertTrue(otherScores.get(0).get(0) instanceof PredatorCatchCloseObjective);
		assertFalse(otherScores.get(0).get(0) instanceof PredatorCatchObjective);
		assertEquals(otherScores.size(), 3);
		assertFalse(otherScores.get(0).isEmpty());
		assertEquals(objectives.get(0).size(), 2);
		assertTrue(objectives.get(0).get(0) instanceof PredatorCatchCloseObjective);
		assertFalse(objectives.get(0).get(0) instanceof PredatorCatchObjective);
		assertFalse(objectives.get(0).get(1) instanceof PredatorCatchCloseObjective);
		assertTrue(objectives.get(0).get(1) instanceof PredatorCatchObjective);
		assertEquals(objectives.size(), 3);
		
		coPred.getLonerTaskInstance().addObjective(new PredatorCatchObjective(), otherScores, false, 0);
		assertEquals(otherScores.get(0).size(), 2);
		assertTrue(otherScores.get(0).get(0) instanceof PredatorCatchCloseObjective);
		assertFalse(otherScores.get(0).get(0) instanceof PredatorCatchObjective);
		assertFalse(otherScores.get(0).get(1) instanceof PredatorCatchCloseObjective);
		assertTrue(otherScores.get(0).get(1) instanceof PredatorCatchObjective);
		assertEquals(otherScores.size(), 3);
		assertEquals(objectives.get(0).size(), 2);
		assertTrue(objectives.get(0).get(0) instanceof PredatorCatchCloseObjective);
		assertFalse(objectives.get(0).get(0) instanceof PredatorCatchObjective);
		assertFalse(objectives.get(0).get(1) instanceof PredatorCatchCloseObjective);
		assertTrue(objectives.get(0).get(1) instanceof PredatorCatchObjective);
		assertEquals(objectives.size(), 3);
		
		//Test for cooperative prey vs static pred
		objectives = new ArrayList<ArrayList<GridTorusObjective<T>>>();
		otherScores = new ArrayList<ArrayList<GridTorusObjective<T>>>();
		objectives.add(new ArrayList<GridTorusObjective<T>>());
		objectives.add(new ArrayList<GridTorusObjective<T>>());
		objectives.add(new ArrayList<GridTorusObjective<T>>());
		otherScores.add(new ArrayList<GridTorusObjective<T>>());
		otherScores.add(new ArrayList<GridTorusObjective<T>>());
		otherScores.add(new ArrayList<GridTorusObjective<T>>());
		MMNEAT.task = new CooperativePreyVsStaticPredatorsTask();
		coPrey = (CooperativePreyVsStaticPredatorsTask)MMNEAT.task;
		assertEquals(objectives.get(0).size(), 0);
		assertEquals(objectives.get(1).size(), 0);
		assertEquals(objectives.get(2).size(), 0);
		assertEquals(objectives.size(), 3);
		assertEquals(otherScores.get(0).size(), 0);
		assertEquals(otherScores.get(1).size(), 0);
		assertEquals(otherScores.get(2).size(), 0);
		assertEquals(otherScores.size(), 3);
		
		coPrey.getLonerTaskInstance().addObjective(new PreyLongSurvivalTimeObjective(), objectives, 1);
		assertEquals(objectives.get(0).size(), 0);
		assertEquals(objectives.get(1).size(), 1);
		assertEquals(objectives.get(2).size(), 0);
		assertTrue(objectives.get(1).get(0) instanceof PreyLongSurvivalTimeObjective);
		assertEquals(objectives.size(), 3);
		assertEquals(otherScores.size(), 3);
		assertTrue(otherScores.get(0).isEmpty());
		assertTrue(otherScores.get(1).isEmpty());
		assertTrue(otherScores.get(2).isEmpty());
		
		
		coPrey.getLonerTaskInstance().addObjective(new PreyMaximizeGameTimeObjective(), objectives, 0);
		assertEquals(objectives.get(0).size(), 1);
		assertEquals(objectives.get(1).size(), 1);
		assertTrue(objectives.get(0).get(0) instanceof PreyMaximizeGameTimeObjective);
		assertFalse(objectives.get(0).get(0) instanceof PreyMinimizeCaughtObjective);
		assertEquals(objectives.size(), 3);
		assertTrue(otherScores.get(0).isEmpty());
		
		coPrey.getLonerTaskInstance().addObjective(new PreyMaximizeDistanceFromPredatorsObjective(), otherScores, 2);
		assertEquals(objectives.get(0).size(), 1);
		assertEquals(objectives.get(1).size(), 1);
		assertTrue(objectives.get(1).get(0) instanceof PreyLongSurvivalTimeObjective);
		assertTrue(objectives.get(0).get(0) instanceof PreyMaximizeGameTimeObjective);
		assertFalse(objectives.get(0).get(0) instanceof PreyMinimizeCaughtObjective);
		assertEquals(objectives.size(), 3);
		assertEquals(otherScores.size(), 3);
		assertTrue(otherScores.get(0).isEmpty());
		assertTrue(otherScores.get(1).isEmpty());
		assertEquals(otherScores.get(2).size(), 1);
		assertTrue(otherScores.get(2).get(0) instanceof PreyMaximizeDistanceFromPredatorsObjective);
		
		coPrey.getLonerTaskInstance().addObjective(new PreyMinimizeCaughtObjective(), objectives, 0);
		assertEquals(objectives.get(0).size(), 2);
		assertTrue(objectives.get(0).get(0) instanceof PreyMaximizeGameTimeObjective);
		assertFalse(objectives.get(0).get(0) instanceof PreyMinimizeCaughtObjective);
		assertFalse(objectives.get(0).get(1) instanceof PreyMaximizeGameTimeObjective);
		assertTrue(objectives.get(0).get(1) instanceof PreyMinimizeCaughtObjective);
		assertEquals(objectives.size(), 3);
		assertTrue(otherScores.get(0).isEmpty());
		
		coPrey.getLonerTaskInstance().addObjective(new PreyMaximizeGameTimeObjective(), otherScores, false, 0);
		assertEquals(otherScores.get(0).size(), 1);
		assertTrue(otherScores.get(0).get(0) instanceof PreyMaximizeGameTimeObjective);
		assertFalse(otherScores.get(0).get(0) instanceof PreyMinimizeCaughtObjective);
		assertEquals(otherScores.size(), 3);
		assertFalse(otherScores.get(0).isEmpty());
		assertEquals(objectives.get(0).size(), 2);
		assertTrue(objectives.get(0).get(0) instanceof PreyMaximizeGameTimeObjective);
		assertFalse(objectives.get(0).get(0) instanceof PreyMinimizeCaughtObjective);
		assertFalse(objectives.get(0).get(1) instanceof PreyMaximizeGameTimeObjective);
		assertTrue(objectives.get(0).get(1) instanceof PreyMinimizeCaughtObjective);
		assertEquals(objectives.size(), 3);
		
		coPrey.getLonerTaskInstance().addObjective(new PreyMinimizeCaughtObjective(), otherScores, false, 0);
		assertEquals(otherScores.get(0).size(), 2);
		assertTrue(otherScores.get(0).get(0) instanceof PreyMaximizeGameTimeObjective);
		assertFalse(otherScores.get(0).get(0) instanceof PreyMinimizeCaughtObjective);
		assertFalse(otherScores.get(0).get(1) instanceof PreyMaximizeGameTimeObjective);
		assertTrue(otherScores.get(0).get(1) instanceof PreyMinimizeCaughtObjective);
		assertEquals(otherScores.size(), 3);
		assertEquals(objectives.get(0).size(), 2);
		assertTrue(objectives.get(0).get(0) instanceof PreyMaximizeGameTimeObjective);
		assertFalse(objectives.get(0).get(0) instanceof PreyMinimizeCaughtObjective);
		assertFalse(objectives.get(0).get(1) instanceof PreyMaximizeGameTimeObjective);
		assertTrue(objectives.get(0).get(1) instanceof PreyMinimizeCaughtObjective);
		assertEquals(objectives.size(), 3);
		
		//Test for homogeneous competitive pred vs prey
		objectives = new ArrayList<ArrayList<GridTorusObjective<T>>>();
		otherScores = new ArrayList<ArrayList<GridTorusObjective<T>>>();
		objectives.add(new ArrayList<GridTorusObjective<T>>());
		objectives.add(new ArrayList<GridTorusObjective<T>>());
		otherScores.add(new ArrayList<GridTorusObjective<T>>());
		otherScores.add(new ArrayList<GridTorusObjective<T>>());
		MMNEAT.task = new CompetitiveHomogeneousPredatorsVsPreyTask();
		homoComp = (CompetitiveHomogeneousPredatorsVsPreyTask)MMNEAT.task;
		assertEquals(objectives.get(0).size(), 0);
		assertEquals(objectives.get(1).size(), 0);
		assertEquals(objectives.size(), 2);
		assertEquals(otherScores.get(0).size(), 0);
		assertEquals(otherScores.get(1).size(), 0);
		assertEquals(otherScores.size(), 2);
		
		homoComp.getLonerTaskInstance().addObjective(new PreyLongSurvivalTimeObjective(), objectives, 1);
		assertEquals(objectives.get(0).size(), 0);
		assertEquals(objectives.get(1).size(), 1);
		assertTrue(objectives.get(1).get(0) instanceof PreyLongSurvivalTimeObjective);
		assertEquals(objectives.size(), 2);
		assertEquals(otherScores.size(), 2);
		assertTrue(otherScores.get(0).isEmpty());
		assertTrue(otherScores.get(1).isEmpty());
		
		
		homoComp.getLonerTaskInstance().addObjective(new PredatorMinimizeGameTimeObjective(), objectives, 0);
		assertEquals(objectives.get(0).size(), 1);
		assertEquals(objectives.get(1).size(), 1);
		assertTrue(objectives.get(0).get(0) instanceof PredatorMinimizeGameTimeObjective);
		assertEquals(objectives.size(), 2);
		assertTrue(otherScores.get(0).isEmpty());
		
		homoComp.getLonerTaskInstance().addObjective(new PreyMaximizeDistanceFromPredatorsObjective(), otherScores, 1);
		assertEquals(objectives.get(0).size(), 1);
		assertEquals(objectives.get(1).size(), 1);
		assertTrue(objectives.get(1).get(0) instanceof PreyLongSurvivalTimeObjective);
		assertTrue(objectives.get(0).get(0) instanceof PredatorMinimizeGameTimeObjective);
		assertEquals(objectives.size(), 2);
		assertEquals(otherScores.size(), 2);
		assertTrue(otherScores.get(0).isEmpty());
		assertEquals(otherScores.get(1).size(), 1);
		assertTrue(otherScores.get(1).get(0) instanceof PreyMaximizeDistanceFromPredatorsObjective);
		
		homoComp.getLonerTaskInstance().addObjective(new PredatorCatchCloseQuickObjective(), objectives, 0);
		assertEquals(objectives.get(0).size(), 2);
		assertEquals(objectives.get(1).size(), 1);
		assertTrue(objectives.get(0).get(0) instanceof PredatorMinimizeGameTimeObjective);
		assertTrue(objectives.get(0).get(1) instanceof PredatorCatchCloseQuickObjective);
		assertEquals(objectives.size(), 2);
		assertTrue(otherScores.get(0).isEmpty());
		
		homoComp.getLonerTaskInstance().addObjective(new PredatorCatchObjective(), otherScores, false, 0);
		assertEquals(otherScores.get(0).size(), 1);
		assertTrue(otherScores.get(0).get(0) instanceof PredatorCatchObjective);
		assertEquals(otherScores.size(), 2);
		assertFalse(otherScores.get(0).isEmpty());
		assertEquals(objectives.get(0).size(), 2);
		assertTrue(objectives.get(0).get(0) instanceof PredatorMinimizeGameTimeObjective);
		assertEquals(objectives.size(), 2);
		
		homoComp.getLonerTaskInstance().addObjective(new PredatorEatEachPreyQuicklyObjective(), otherScores, false, 0);
		assertEquals(otherScores.get(0).size(), 2);
		assertTrue(otherScores.get(0).get(0) instanceof PredatorCatchObjective);
		assertTrue(otherScores.get(0).get(1) instanceof PredatorEatEachPreyQuicklyObjective);
		assertEquals(otherScores.size(), 2);
		assertEquals(objectives.get(0).size(), 2);
		assertTrue(objectives.get(0).get(0) instanceof PredatorMinimizeGameTimeObjective);
		assertTrue(objectives.get(0).get(1) instanceof PredatorCatchCloseQuickObjective);
		assertEquals(objectives.size(), 2);

	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Test
	public void testNumObjectives() {
		MMNEAT.task = new TorusEvolvedPredatorsVsStaticPreyTask();
		homoPred = (TorusEvolvedPredatorsVsStaticPreyTask)MMNEAT.task;
		ArrayList<ArrayList<GridTorusObjective<T>>> objectives = homoPred.objectives;
		ArrayList<ArrayList<GridTorusObjective<T>>> otherScores = homoPred.otherScores;
		
		assertEquals(objectives.get(0).size(), 0);
		assertEquals(objectives.size(), 1);
		assertEquals(homoPred.numObjectives(), 0);
		homoPred.addObjective(new PredatorCatchCloseObjective(), objectives, 0);
		assertEquals(homoPred.numObjectives(), 1);
		homoPred.addObjective(new PredatorCatchObjective(), objectives, 0);
		assertEquals(homoPred.numObjectives(), 2);
		homoPred.addObjective(new PreyMinimizeCaughtObjective(), objectives, 0);
		assertEquals(homoPred.numObjectives(), 3);
		homoPred.addObjective(new PredatorEatEachPreyQuicklyObjective(), otherScores, 0);
		assertEquals(homoPred.numObjectives(), 3);
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Test
	public void testNumOtherScores() {
		//NOTE: numOthers depends on how many fitnesses are currently added to other scores.
		//NOTE: If this is failing, probably because there are not 22 other scores anymore.
		MMNEAT.task = new TorusEvolvedPredatorsVsStaticPreyTask();
		homoPred = (TorusEvolvedPredatorsVsStaticPreyTask)MMNEAT.task;
		ArrayList<ArrayList<GridTorusObjective<T>>> objectives = homoPred.objectives;
		ArrayList<ArrayList<GridTorusObjective<T>>> otherScores = homoPred.otherScores;
		assertEquals(otherScores.size(), 1);
		//NOTE: numOthers depends on how many fitnesses are currently added to other scores.
		//NOTE: If this is failing, probably because there are not 22 other scores anymore.
		assertEquals(homoPred.numOtherScores(), numOthers);
		homoPred.addObjective(new PredatorCatchCloseObjective(), otherScores, 0);
		//NOTE: numOthers depends on how many fitnesses are currently added to other scores.
		//NOTE: If this is failing, probably because there are not 22 other scores anymore.
		assertEquals(homoPred.numOtherScores(), numOthers + 1);
		homoPred.addObjective(new PredatorCatchObjective(), otherScores, 0);
		//NOTE: numOthers depends on how many fitnesses are currently added to other scores.
		//NOTE: If this is failing, probably because there are not 22 other scores anymore.
		assertEquals(homoPred.numOtherScores(), numOthers + 2);
		homoPred.addObjective(new PreyMinimizeCaughtObjective(), otherScores, 0);
		//NOTE: numOthers depends on how many fitnesses are currently added to other scores.
		//NOTE: If this is failing, probably because there are not 22 other scores anymore.
		assertEquals(homoPred.numOtherScores(), numOthers + 3);
		homoPred.addObjective(new PredatorEatEachPreyQuicklyObjective(), objectives, 0);
		//NOTE: numOthers depends on how many fitnesses are currently added to other scores.
		//NOTE: If this is failing, probably because there are not 22 other scores anymore.
		assertEquals(homoPred.numOtherScores(), numOthers + 3);
		
	}
	
	@SuppressWarnings("rawtypes")
	@Test
	public void testStartingGoals() {
		Parameters.initializeParameterCollections(new String[] { "io:false", "netio:false", "torusTimeLimit:1000",
				"torusPreys:2", "torusPredators:3",
				"staticPreyController:edu.utexas.cs.nn.gridTorus.controllers.PreyFleeClosestPredatorController",
				"predatorCatchClose:true", "preyRRM:true" });
		MMNEAT.loadClasses();
		MMNEAT.task = new TorusEvolvedPredatorsVsStaticPreyTask();
		homoPred = (TorusEvolvedPredatorsVsStaticPreyTask)MMNEAT.task;
		//min score of predatorCatchClose is 0
		assertEquals(homoPred.startingGoals()[0], 0, doubleThreshold);
		assertEquals(homoPred.startingGoals().length, 1);
		
		Parameters.initializeParameterCollections(new String[] { "io:false", "netio:false", "torusTimeLimit:1000",
				"torusPreys:2", "torusPredators:3",
				"staticPreyController:edu.utexas.cs.nn.gridTorus.controllers.PreyFleeClosestPredatorController",
				"predatorCatchClose:true", "predatorMinimizeDistance:true", "predatorsEatQuick:true"});
		MMNEAT.loadClasses();
		MMNEAT.task = new TorusEvolvedPredatorsVsStaticPreyTask();
		homoPred = (TorusEvolvedPredatorsVsStaticPreyTask)MMNEAT.task;
		//NOTE: This relies on the ordering of the adding of the fitness functions in addPredatorObjectives
		//NOTE: If this test is failing, the ordering of the fitness objective checks/adds probably changed in addPredatorObjectives
		//min score of predatorCatchClose is 0
		assertEquals(homoPred.startingGoals()[0], 0, doubleThreshold);
		//min score of PredatorMinimizeDistanceFromPreyObjective is -2000 with two prey
		assertEquals(homoPred.startingGoals()[1], -2000, doubleThreshold);
		//min score of PredatorEatEachPreyQuicklyObjective is -600 with two prey
		assertEquals(homoPred.startingGoals()[2], -600, doubleThreshold);

	}
	
	@SuppressWarnings("rawtypes")
	@Test
	public void testMinScores() {
		Parameters.initializeParameterCollections(new String[] { "io:false", "netio:false", "torusTimeLimit:1000",
				"torusPreys:2", "torusPredators:3",
				"staticPreyController:edu.utexas.cs.nn.gridTorus.controllers.PreyFleeClosestPredatorController",
				"predatorCatchClose:true", "preyRRM:true" });
		MMNEAT.loadClasses();
		MMNEAT.task = new TorusEvolvedPredatorsVsStaticPreyTask();
		homoPred = (TorusEvolvedPredatorsVsStaticPreyTask)MMNEAT.task;
		//min score of predatorCatchClose is 0
		assertEquals(homoPred.minScores()[0], 0, doubleThreshold);
		assertEquals(homoPred.minScores().length, 1);
		
		Parameters.initializeParameterCollections(new String[] { "io:false", "netio:false", "torusTimeLimit:1000",
				"torusPreys:2", "torusPredators:3",
				"staticPreyController:edu.utexas.cs.nn.gridTorus.controllers.PreyFleeClosestPredatorController",
				"predatorCatchClose:true", "predatorMinimizeDistance:true", "predatorsEatQuick:true"});
		MMNEAT.loadClasses();
		MMNEAT.task = new TorusEvolvedPredatorsVsStaticPreyTask();
		homoPred = (TorusEvolvedPredatorsVsStaticPreyTask)MMNEAT.task;
		//NOTE: This relies on the ordering of the adding of the fitness functions in addPredatorObjectives
		//NOTE: If this test is failing, the ordering of the fitness objective checks/adds probably changed in addPredatorObjectives
		//min score of predatorCatchClose is 0
		assertEquals(homoPred.minScores()[0], 0, doubleThreshold);
		//min score of PredatorMinimizeDistanceFromPreyObjective is -2000 with two prey
		assertEquals(homoPred.minScores()[1], -2000, doubleThreshold);
		//min score of PredatorEatEachPreyQuicklyObjective is -600 with two prey
		assertEquals(homoPred.minScores()[2], -600, doubleThreshold);
		
	}
	
	@SuppressWarnings("rawtypes")
	@Test
	public void testSensorLabels() {
		//Test with proximity sensors and with sense teammates
		MMNEAT.task = new TorusEvolvedPredatorsVsStaticPreyTask();
		homoPred = (TorusEvolvedPredatorsVsStaticPreyTask)MMNEAT.task;
		//with proximity and sense teammates
		int numPreds = 3;
		int numPrey = 2;
		//NOTE: this relies on the fact that prey are listed first followed by predators
		String type1 = "Closest Prey";
		String type2 = "Closest Pred";
		String[] result = new String[(numPreds-1+numPrey) * 2];
		for (int i = 0; i < numPrey; i++) {
			result[(2 * i)] = "X Offset to " + type1 + " " + i;
			result[(2 * i) + 1] = "Y Offset to " + type1 + " " + i;
		}
		for (int i = numPrey; i < numPreds-1+numPrey; i++) {
			result[(2 * i)] = "X Offset to " + type2 + " " + (i-numPrey);
			result[(2 * i) + 1] = "Y Offset to " + type2 + " " + (i-numPrey);
		}
		String[] resultWithBias = new String[(numPreds-1+numPrey) * 2 + 1];
		resultWithBias[0] = "Bias";
		System.arraycopy(result, 0, resultWithBias, 1, result.length);
		//NOTE: this relies on the fact that prey are listed first followed by predators
		//NOTE: if this is failing, the sensor ordering probably changed (or the labels themselves) 
		assertArrayEquals(homoPred.sensorLabels(), resultWithBias);
		
		//Test without proximity sensors and with sense teammates
		Parameters.initializeParameterCollections(new String[] { "io:false", "netio:false", "torusTimeLimit:1000",
				"torusPreys:2", "torusPredators:3", "allowDoNothingActionForPredators:false", "torusSenseTeammates:true", 
				"staticPreyController:edu.utexas.cs.nn.gridTorus.controllers.PreyFleeClosestPredatorController",
				"torusSenseByProximity:false", "predatorCatchClose:false", "preyRRM:false" });
		MMNEAT.loadClasses();
		MMNEAT.task = new TorusEvolvedPredatorsVsStaticPreyTask();
		homoPred = (TorusEvolvedPredatorsVsStaticPreyTask)MMNEAT.task;
		//with proximity and sense teammates
		numPreds = 3;
		numPrey = 2;
		//NOTE: this relies on the fact that prey are listed first followed by predators
		type1 = "Prey";
		type2 = "Pred";
		result = new String[(numPreds-1+numPrey) * 2];
		for (int i = 0; i < numPrey; i++) {
			result[(2 * i)] = "X Offset to " + type1 + " " + i;
			result[(2 * i) + 1] = "Y Offset to " + type1 + " " + i;
		}
		for (int i = numPrey; i < numPreds-1+numPrey; i++) {
			result[(2 * i)] = "X Offset to " + type2 + " " + (i-numPrey);
			result[(2 * i) + 1] = "Y Offset to " + type2 + " " + (i-numPrey);
		}
		resultWithBias = new String[(numPreds-1+numPrey) * 2 + 1];
		resultWithBias[0] = "Bias";
		System.arraycopy(result, 0, resultWithBias, 1, result.length);
		//NOTE: this relies on the fact that prey are listed first followed by predators
		//NOTE: if this is failing, the sensor ordering probably changed (or the labels themselves) 
		assertArrayEquals(homoPred.sensorLabels(), resultWithBias);
		
		//Test with proximity sensors and without sense teammates
		Parameters.initializeParameterCollections(new String[] { "io:false", "netio:false", "torusTimeLimit:1000",
				"torusPreys:2", "torusPredators:3", "allowDoNothingActionForPredators:false", "torusSenseTeammates:false", 
				"staticPreyController:edu.utexas.cs.nn.gridTorus.controllers.PreyFleeClosestPredatorController",
				"torusSenseByProximity:true", "predatorCatchClose:false", "preyRRM:false" });
		MMNEAT.loadClasses();
		MMNEAT.task = new TorusEvolvedPredatorsVsStaticPreyTask();
		homoPred = (TorusEvolvedPredatorsVsStaticPreyTask)MMNEAT.task;
		//with proximity and sense teammates
		numPrey = 2;
		//NOTE: this relies on the fact that prey are listed first followed by predators
		String type = "Closest Prey";
		result = new String[(numPrey) * 2];
		for (int i = 0; i < numPrey; i++) {
			result[(2 * i)] = "X Offset to " + type + " " + i;
			result[(2 * i) + 1] = "Y Offset to " + type + " " + i;
		}
		resultWithBias = new String[(numPrey) * 2 + 1];
		resultWithBias[0] = "Bias";
		System.arraycopy(result, 0, resultWithBias, 1, result.length);
		//NOTE: this relies on the fact that prey are listed first followed by predators
		//NOTE: if this is failing, the sensor ordering probably changed (or the labels themselves) 
		assertArrayEquals(homoPred.sensorLabels(), resultWithBias);
		
		//Test without proximity sensors and without sense teammates
		Parameters.initializeParameterCollections(new String[] { "io:false", "netio:false", "torusTimeLimit:1000",
				"torusPreys:2", "torusPredators:3", "allowDoNothingActionForPredators:false", "torusSenseTeammates:false", 
				"staticPreyController:edu.utexas.cs.nn.gridTorus.controllers.PreyFleeClosestPredatorController",
				"torusSenseByProximity:false", "predatorCatchClose:false", "preyRRM:false" });
		MMNEAT.loadClasses();
		MMNEAT.task = new TorusEvolvedPredatorsVsStaticPreyTask();
		homoPred = (TorusEvolvedPredatorsVsStaticPreyTask)MMNEAT.task;
		//with proximity and sense teammates
		numPrey = 2;
		//NOTE: this relies on the fact that prey are listed first followed by predators
		type = "Prey";
		result = new String[(numPrey) * 2];
		for (int i = 0; i < numPrey; i++) {
			result[(2 * i)] = "X Offset to " + type + " " + i;
			result[(2 * i) + 1] = "Y Offset to " + type + " " + i;
		}
		resultWithBias = new String[(numPrey) * 2 + 1];
		resultWithBias[0] = "Bias";
		System.arraycopy(result, 0, resultWithBias, 1, result.length);
		//NOTE: this relies on the fact that prey are listed first followed by predators
		//NOTE: if this is failing, the sensor ordering probably changed (or the labels themselves) 
		assertArrayEquals(homoPred.sensorLabels(), resultWithBias);
	}
	
	@SuppressWarnings({ "rawtypes", "static-access" })
	@Test
	public void testOutputLabels() {
		final String[] ALL_ACTIONS = new String[] { "UP", "RIGHT", "DOWN", "LEFT", "NOTHING" };
		final String[] MOVEMENT_ACTIONS = new String[] { "UP", "RIGHT", "DOWN", "LEFT" };
		
		
		MMNEAT.task = new TorusEvolvedPredatorsVsStaticPreyTask();
		homoPred = (TorusEvolvedPredatorsVsStaticPreyTask)MMNEAT.task;
		//Predator is evolving without the possibility of do nothing action
		assertArrayEquals(homoPred.outputLabels(), MOVEMENT_ACTIONS);
		MMNEAT.task = new TorusEvolvedPreyVsStaticPredatorsTask();
		homoPrey = (TorusEvolvedPreyVsStaticPredatorsTask)MMNEAT.task;
		//Prey is evolving without the possibility of do nothing action
		assertArrayEquals(homoPrey.outputLabels(), MOVEMENT_ACTIONS);
		
		Parameters.initializeParameterCollections(new String[] { "io:false", "netio:false", "torusTimeLimit:1000",
				"torusPreys:2", "torusPredators:3", "allowDoNothingActionForPredators:true", "allowDoNothingActionForPreys:true", 
				"staticPreyController:edu.utexas.cs.nn.gridTorus.controllers.PreyFleeClosestPredatorController",
				"predatorCatchClose:false", "preyRRM:false" });
		MMNEAT.loadClasses();
		MMNEAT.task = new TorusEvolvedPredatorsVsStaticPreyTask();
		homoPred = (TorusEvolvedPredatorsVsStaticPreyTask)MMNEAT.task;
		//Predator is evolving with the possibility of do nothing action
		assertArrayEquals(homoPred.outputLabels(), ALL_ACTIONS);
		MMNEAT.task = new TorusEvolvedPreyVsStaticPredatorsTask();
		homoPrey = (TorusEvolvedPreyVsStaticPredatorsTask)MMNEAT.task;
		//Prey is evolving with the possibility of do nothing action
		assertArrayEquals(homoPrey.outputLabels(), ALL_ACTIONS);
		
		Parameters.initializeParameterCollections(new String[] { "io:false", "netio:false", "torusTimeLimit:1000",
				"torusPreys:2", "torusPredators:3", "allowDoNothingActionForPredators:false", "allowDoNothingActionForPreys:true", 
				"staticPreyController:edu.utexas.cs.nn.gridTorus.controllers.PreyFleeClosestPredatorController",
				"predatorCatchClose:false", "preyRRM:false" });
		MMNEAT.loadClasses();
		MMNEAT.task = new TorusEvolvedPredatorsVsStaticPreyTask();
		homoPred = (TorusEvolvedPredatorsVsStaticPreyTask)MMNEAT.task;
		//Predator is evolving without the possibility of do nothing action
		assertArrayEquals(homoPred.outputLabels(), MOVEMENT_ACTIONS);
		MMNEAT.task = new TorusEvolvedPreyVsStaticPredatorsTask();
		homoPrey = (TorusEvolvedPreyVsStaticPredatorsTask)MMNEAT.task;
		//Prey is evolving with the possibility of do nothing action
		assertArrayEquals(homoPrey.outputLabels(), ALL_ACTIONS);
		
		Parameters.initializeParameterCollections(new String[] { "io:false", "netio:false", "torusTimeLimit:1000",
				"torusPreys:2", "torusPredators:3", "allowDoNothingActionForPredators:true", "allowDoNothingActionForPreys:false", 
				"staticPreyController:edu.utexas.cs.nn.gridTorus.controllers.PreyFleeClosestPredatorController",
				"predatorCatchClose:false", "preyRRM:false" });
		MMNEAT.loadClasses();
		MMNEAT.task = new TorusEvolvedPredatorsVsStaticPreyTask();
		homoPred = (TorusEvolvedPredatorsVsStaticPreyTask)MMNEAT.task;
		//Predator is evolving with the possibility of do nothing action
		assertArrayEquals(homoPred.outputLabels(), ALL_ACTIONS);
		MMNEAT.task = new TorusEvolvedPreyVsStaticPredatorsTask();
		homoPrey = (TorusEvolvedPreyVsStaticPredatorsTask)MMNEAT.task;
		//Prey is evolving without the possibility of do nothing action
		assertArrayEquals(homoPrey.outputLabels(), MOVEMENT_ACTIONS);
		
		Parameters.initializeParameterCollections(new String[] { "io:false", "netio:false", "torusTimeLimit:1000",
				"torusPreys:2", "torusPredators:3", "allowDoNothingActionForPredators:false", "allowDoNothingActionForPreys:false", 
				"staticPreyController:edu.utexas.cs.nn.gridTorus.controllers.PreyFleeClosestPredatorController",
				"predatorCatchClose:false", "preyRRM:false" });
		MMNEAT.loadClasses();
		MMNEAT.task = new CompetitiveHomogeneousPredatorsVsPreyTask();
		homoComp = (CompetitiveHomogeneousPredatorsVsPreyTask)MMNEAT.task;
		homoComp.getLonerTaskInstance().preyEvolve = true;
		//Prey is evolving without the possibility of do nothing action
		assertArrayEquals(homoComp.outputLabels(), MOVEMENT_ACTIONS);
		homoComp.getLonerTaskInstance().preyEvolve = false;
		//Predator is evolving without the possibility of do nothing action
		assertArrayEquals(homoComp.outputLabels(), MOVEMENT_ACTIONS);
		
		Parameters.initializeParameterCollections(new String[] { "io:false", "netio:false", "torusTimeLimit:1000",
				"torusPreys:2", "torusPredators:3", "allowDoNothingActionForPredators:true", "allowDoNothingActionForPreys:false", 
				"staticPreyController:edu.utexas.cs.nn.gridTorus.controllers.PreyFleeClosestPredatorController",
				"predatorCatchClose:false", "preyRRM:false" });
		MMNEAT.loadClasses();
		MMNEAT.task = new CompetitiveHomogeneousPredatorsVsPreyTask();
		homoComp = (CompetitiveHomogeneousPredatorsVsPreyTask)MMNEAT.task;
		homoComp.getLonerTaskInstance().preyEvolve = true;
		//Prey is evolving without the possibility of do nothing action
		assertArrayEquals(homoComp.outputLabels(), MOVEMENT_ACTIONS);
		homoComp.getLonerTaskInstance().preyEvolve = false;
		//Predator is evolving with the possibility of do nothing action
		assertArrayEquals(homoComp.outputLabels(), ALL_ACTIONS);
		
		Parameters.initializeParameterCollections(new String[] { "io:false", "netio:false", "torusTimeLimit:1000",
				"torusPreys:2", "torusPredators:3", "allowDoNothingActionForPredators:false", "allowDoNothingActionForPreys:true", 
				"staticPreyController:edu.utexas.cs.nn.gridTorus.controllers.PreyFleeClosestPredatorController",
				"predatorCatchClose:false", "preyRRM:false" });
		MMNEAT.loadClasses();
		MMNEAT.task = new CompetitiveHomogeneousPredatorsVsPreyTask();
		homoComp = (CompetitiveHomogeneousPredatorsVsPreyTask)MMNEAT.task;
		homoComp.getLonerTaskInstance().preyEvolve = true;
		//Prey is evolving with the possibility of do nothing action
		assertArrayEquals(homoComp.outputLabels(), ALL_ACTIONS);
		homoComp.getLonerTaskInstance().preyEvolve = false;
		//Predator is evolving without the possibility of do nothing action
		assertArrayEquals(homoComp.outputLabels(), MOVEMENT_ACTIONS);
		
		Parameters.initializeParameterCollections(new String[] { "io:false", "netio:false", "torusTimeLimit:1000",
				"torusPreys:2", "torusPredators:3", "allowDoNothingActionForPredators:true", "allowDoNothingActionForPreys:true", 
				"staticPreyController:edu.utexas.cs.nn.gridTorus.controllers.PreyFleeClosestPredatorController",
				"predatorCatchClose:false", "preyRRM:false" });
		MMNEAT.loadClasses();
		MMNEAT.task = new CompetitiveHomogeneousPredatorsVsPreyTask();
		homoComp = (CompetitiveHomogeneousPredatorsVsPreyTask)MMNEAT.task;
		homoComp.getLonerTaskInstance().preyEvolve = true;
		//Prey is evolving with the possibility of do nothing action
		assertArrayEquals(homoComp.outputLabels(), ALL_ACTIONS);
		homoComp.getLonerTaskInstance().preyEvolve = false;
		//Predator is evolving with the possibility of do nothing action
		assertArrayEquals(homoComp.outputLabels(), ALL_ACTIONS);
	}
	
	@SuppressWarnings({ "rawtypes", "static-access" })
	@Test
	public void testGetStaticControllers() {
		Parameters.initializeParameterCollections(new String[] { "io:false", "netio:false", "torusTimeLimit:1000",
				"torusPreys:2", "torusPredators:3", "allowDoNothingActionForPredators:false", "torusSenseTeammates:true", 
				"staticPreyController:edu.utexas.cs.nn.gridTorus.controllers.PreyFleeClosestPredatorController", 
				"staticPredatorController:edu.utexas.cs.nn.gridTorus.controllers.RandomPredatorController", 
				"torusSenseByProximity:true", "predatorCatchClose:false", "preyRRM:false" });
		MMNEAT.loadClasses();
		
		MMNEAT.task = new TorusEvolvedPredatorsVsStaticPreyTask();
		homoPred = (TorusEvolvedPredatorsVsStaticPreyTask)MMNEAT.task;
		TorusPredPreyController[] staticControllers;
		staticControllers = homoPred.getStaticControllers(false, 2);
		assertEquals(staticControllers.length, 2);
		assertTrue(staticControllers[0] instanceof PreyFleeClosestPredatorController);
		assertTrue(staticControllers[1] instanceof PreyFleeClosestPredatorController);
		
		staticControllers = homoPred.getStaticControllers(true, 3);
		assertEquals(staticControllers.length, 3);
		assertTrue(staticControllers[0] instanceof RandomPredatorController);
		assertTrue(staticControllers[1] instanceof RandomPredatorController);
		assertTrue(staticControllers[2] instanceof RandomPredatorController);
	}
	
	@Test
	public void testGetEvolvedControllers() {
		Parameters.initializeParameterCollections(new String[] { "io:false", "netio:false", "torusTimeLimit:1000",
				"torusPreys:2", "torusPredators:3", "allowDoNothingActionForPredators:true", "torusSenseTeammates:true", 
				"staticPreyController:edu.utexas.cs.nn.gridTorus.controllers.PreyFleeClosestPredatorController", 
				"task:edu.utexas.cs.nn.tasks.gridTorus.TorusEvolvedPredatorsVsStaticPreyTask", 
				"torusSenseByProximity:true", "predatorCatchClose:false", "preyRRM:false" });
		MMNEAT.loadClasses();
		TWEANNGenotype g = new TWEANNGenotype();
		
		TorusPredPreyController[] evolvedControllers = new TorusPredPreyController[3];
		TorusPredPreyTask.getEvolvedControllers(evolvedControllers, g, true);
		
		assertEquals(evolvedControllers.length, 3);
		assertTrue(evolvedControllers[0] instanceof NNTorusPredPreyController);
		assertTrue(evolvedControllers[1] instanceof NNTorusPredPreyController);
		assertTrue(evolvedControllers[2] instanceof NNTorusPredPreyController);
		assertTrue(((NNTorusPredPreyController) evolvedControllers[0]).isPredator == true);
		//9 because not sensing self, would have been 11 with sensing self
		assertEquals(((NNTorusPredPreyController) evolvedControllers[0]).nn.numInputs(), 9);
		assertEquals(((NNTorusPredPreyController) evolvedControllers[0]).nn.numOutputs(), 5);
		
		Parameters.initializeParameterCollections(new String[] { "io:false", "netio:false", "torusTimeLimit:1000",
				"torusPreys:2", "torusPredators:3", "allowDoNothingActionForPredators:false", "torusSenseTeammates:false", 
				"staticPreyController:edu.utexas.cs.nn.gridTorus.controllers.PreyFleeClosestPredatorController", 
				"task:edu.utexas.cs.nn.tasks.gridTorus.TorusEvolvedPredatorsVsStaticPreyTask", 
				"torusSenseByProximity:true", "predatorCatchClose:false", "preyRRM:false" });
		MMNEAT.loadClasses();
		
		g = new TWEANNGenotype();
		evolvedControllers = new TorusPredPreyController[3];
		TorusPredPreyTask.getEvolvedControllers(evolvedControllers, g, true);
		assertEquals(evolvedControllers.length, 3);
		assertTrue(evolvedControllers[0] instanceof NNTorusPredPreyController);
		assertTrue(evolvedControllers[1] instanceof NNTorusPredPreyController);
		assertTrue(evolvedControllers[2] instanceof NNTorusPredPreyController);
		assertTrue(((NNTorusPredPreyController) evolvedControllers[1]).isPredator == true);
		assertEquals(((NNTorusPredPreyController) evolvedControllers[2]).nn.numInputs(), 5);
		assertEquals(((NNTorusPredPreyController) evolvedControllers[0]).nn.numOutputs(), 4);
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Test
	public void testAddAllObjectives() {
		//NOTE: numOthers depends on how many fitnesses are currently added to other scores.
		//NOTE: If this is failing, probably because there are not 22 other scores anymore.
		MMNEAT.task = new TorusEvolvedPredatorsVsStaticPreyTask();
		homoPred = (TorusEvolvedPredatorsVsStaticPreyTask)MMNEAT.task;
		ArrayList<ArrayList<GridTorusObjective<T>>> objectives = homoPred.objectives;
		ArrayList<ArrayList<GridTorusObjective<T>>> otherScores = homoPred.otherScores;
		//NOTE: addAllObjectives already called once in constructor
		assertEquals(objectives.size(), 1);
		assertTrue(objectives.get(0).isEmpty());
		assertEquals(otherScores.size(), 1);
		//NOTE: numOthers depends on how many fitnesses are currently added to other scores.
		//NOTE: If this is failing, probably because there are not 22 other scores anymore.
		assertEquals(otherScores.get(0).size(), numOthers);
		
		Parameters.initializeParameterCollections(new String[] { "io:false", "netio:false", "torusTimeLimit:1000",
				"torusPreys:2", "torusPredators:3",
				"staticPreyController:edu.utexas.cs.nn.gridTorus.controllers.PreyFleeClosestPredatorController",
				"predatorCatchClose:true", "preyRRM:true" });
		MMNEAT.loadClasses();
		MMNEAT.task = new TorusEvolvedPredatorsVsStaticPreyTask();
		homoPred = (TorusEvolvedPredatorsVsStaticPreyTask)MMNEAT.task;
		objectives = homoPred.objectives;
		otherScores = homoPred.otherScores;
		//NOTE: addAllObjectives already called once in constructor
		assertEquals(objectives.size(), 1);
		assertFalse(objectives.get(0).isEmpty());
		assertEquals(otherScores.size(), 1);
		//NOTE: numOthers depends on how many fitnesses are currently added to other scores.
		//NOTE: If this is failing, probably because there are not 22 other scores anymore.
		assertEquals(otherScores.get(0).size(), numOthers);
		assertTrue(objectives.get(0).get(0) instanceof PredatorCatchCloseObjective);
		
		Parameters.initializeParameterCollections(new String[] { "io:false", "netio:false", "torusTimeLimit:1000",
				"torusPreys:2", "torusPredators:3",
				"staticPreyController:edu.utexas.cs.nn.gridTorus.controllers.PreyFleeClosestPredatorController",
				"predatorCatchClose:true", "preyRRM:true" });
		MMNEAT.loadClasses();
		MMNEAT.task = new CooperativePredatorsVsStaticPreyTask();
		coPred = (CooperativePredatorsVsStaticPreyTask)MMNEAT.task;
		objectives = coPred.getLonerTaskInstance().objectives;
		otherScores = coPred.getLonerTaskInstance().otherScores;
		//NOTE:constructor for cooperative task calls addAllObjectives once per population
		assertEquals(objectives.size(), 3);
		assertFalse(objectives.get(0).isEmpty());
		assertEquals(otherScores.size(), 3);
		//NOTE: numOthers depends on how many fitnesses are currently added to other scores.
		//NOTE: If this is failing, probably because there are not 22 other scores anymore.
		assertEquals(otherScores.get(0).size(), numOthers);
		assertTrue(objectives.get(0).get(0) instanceof PredatorCatchCloseObjective);
		assertTrue(otherScores.get(1).isEmpty());
		assertTrue(otherScores.get(2).isEmpty());
		assertTrue(objectives.get(1).get(0) instanceof PredatorCatchCloseObjective);
		assertTrue(objectives.get(2).get(0) instanceof PredatorCatchCloseObjective);
		assertEquals(objectives.get(0).size(), 1);
		assertEquals(objectives.get(1).size(), 1);
		assertEquals(objectives.get(2).size(), 1);
		
		Parameters.initializeParameterCollections(new String[] { "io:false", "netio:false", "torusTimeLimit:1000",
				"torusPreys:2", "torusPredators:3",
				"staticPreyController:edu.utexas.cs.nn.gridTorus.controllers.PreyFleeClosestPredatorController",
				"predatorCatchClose:true", "preyRRM:true" });
		MMNEAT.loadClasses();
		MMNEAT.task = new CompetitiveHomogeneousPredatorsVsPreyTask();
		homoComp = (CompetitiveHomogeneousPredatorsVsPreyTask)MMNEAT.task;
		objectives = homoComp.getLonerTaskInstance().objectives;
		otherScores = homoComp.getLonerTaskInstance().otherScores;
		//NOTE:constructor for cooperative task calls addAllObjectives once per population
		assertEquals(objectives.size(), 2);
		assertFalse(objectives.get(0).isEmpty());
		assertEquals(otherScores.size(), 2);
		//NOTE: numOthersCompetitive depends on how many fitnesses are currently added to other scores.
		//NOTE: If this is failing, probably because there are not 37 other scores anymore.
		assertEquals(otherScores.get(0).size(), numOthersCompetitive);
		assertTrue(objectives.get(0).get(0) instanceof PredatorCatchCloseObjective);
		assertTrue(objectives.get(1).get(0) instanceof PreyRawalRajagopalanMiikkulainenObjective);
		assertTrue(otherScores.get(1).isEmpty());
		assertEquals(objectives.get(0).size(), 1);
		assertEquals(objectives.get(1).size(), 1);
		
	}
	
	//NOTE: HyperNEAT methods are untested

}
