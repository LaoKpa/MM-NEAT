/**
 Copyright 2007 Brian Tanner
 brian@tannerpages.com
 http://brian.tannerpages.com

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */
package edu.utexas.cs.nn.tasks.rlglue;

import java.util.ArrayList;

import org.rlcommunity.rlglue.codec.LocalGlue;
import org.rlcommunity.rlglue.codec.RLGlue;

import edu.utexas.cs.nn.MMNEAT.MMNEAT;
import edu.utexas.cs.nn.evolution.genotypes.Genotype;
import edu.utexas.cs.nn.networks.Network;
import edu.utexas.cs.nn.networks.NetworkTask;
import edu.utexas.cs.nn.parameters.CommonConstants;
import edu.utexas.cs.nn.parameters.Parameters;
import edu.utexas.cs.nn.tasks.NoisyLonerTask;
import edu.utexas.cs.nn.util.ClassCreation;
import edu.utexas.cs.nn.util.datastructures.Pair;

public class RLGlueTask<T extends Network> extends NoisyLonerTask<T>implements NetworkTask {

	/**
	 * Initialize the elements to be used here
	 */
	protected static RLGlueEnvironment environment;
	@SuppressWarnings("rawtypes") // Needs static access, and type T isn't known yet
	public static RLGlueAgent agent;
	protected int[] rlNumSteps;
	protected double[] rlReturn;
	// cutoff
	protected int maxStepsPerEpisode;
	private ArrayList<Double> behaviorVector;

	/**
	 * Initializer for the RLGlueTask, it called the
	 * MMNEAT.rlGlueEnvironmentthat it needs as a parameter
	 */
	public RLGlueTask() {
		this(MMNEAT.rlGlueEnvironment);
	}

	/**
	 * Initializes the RLGlueTask with MMNEAT.rlGlueEnvironment environment.
	 * Puddleworld and Tetris have special-case options for using multiple
	 * objectives that are set up here.
	 *
	 * @param environment
	 */
	@SuppressWarnings("unchecked")
	public RLGlueTask(RLGlueEnvironment environment) {
		super();
		rlNumSteps = new int[CommonConstants.trials];
		rlReturn = new double[CommonConstants.trials];
		maxStepsPerEpisode = Parameters.parameters.integerParameter("steps");
		RLGlueTask.environment = environment;

		try {
			agent = (RLGlueAgent<T>) ClassCreation.createObject("rlGlueAgent");
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
			System.out.println("Could not launch RLGlue agent");
			System.exit(1);
		}
		// The local glue codec does not need any network connectivity
		RLGlue.setGlue(new LocalGlue(environment, agent));
	}

	/**
	 * Starts the other initializing methods
	 */
	@Override
	public void prep() {
		behaviorVector = new ArrayList<Double>();
		RLGlue.RL_init();
	}

	/**
	 * Cleans the task, doesn't delete anything
	 */
	@Override
	public void cleanup() {
		RLGlue.RL_cleanup();
	}

	/**
	 * Getter for behavior vector (array list)
	 * 
	 * @return Behavior characterization
	 */
	@Override
	public ArrayList<Double> getBehaviorVector() {
		return behaviorVector;
	}

	/**
	 * Other scores: overridden by specific environments
	 * 
	 * @return default of 0
	 */
	@Override
	public int numOtherScores() {
		return 0;
	}

	/**
	 * Used for testing a genotype and is added to an agent and runs in order to
	 * test it.
	 *
	 * @return Pair of doubles arrays: fitness scores followed by "other" scores
	 */
	@SuppressWarnings("unchecked")
	@Override
	public Pair<double[], double[]> oneEval(Genotype<T> individual, int num) {
		agent.replaceGenotype(individual);
		System.out.print("Episode: " + num);
		RLGlue.RL_episode(maxStepsPerEpisode);
		System.out.println("\t steps: " + RLGlue.RL_num_steps());
		rlNumSteps[num] = RLGlue.RL_num_steps();
		rlReturn[num] = RLGlue.RL_return();
		behaviorVector.addAll(environment.getBehaviorVector());

		return episodeResult(num);
	}

	/**
	 * Return fitness results for single episode
	 * @param num episode/eval number
	 * @return fitness and other scores for episode
	 */
	public Pair<double[], double[]> episodeResult(int num){
		return new Pair<double[], double[]>(new double[] { rlReturn[num] }, new double[0]);
	}
	
	/**
	 * Returns the number of objectives
	 * 
	 * @return number of objectives
	 */
	@Override
	public int numObjectives(){
			return 1; // default: just the RL Return
	}

	/**
	 * Supposedly a getter for the time stamp, but returns number of steps Used
	 * by TWEANN.java
	 * 
	 * @return Supposed to be how much time has passed in episode
	 */
	@Override
	public double getTimeStamp() {
		// Need to fix this a bit
		return rlNumSteps[0];
	}

	/**
	 * Brings in the labels for features, which are the NN inputs.
	 * 
	 * @return array of sensor input labels
	 */
	@Override
	public String[] sensorLabels() {
		return MMNEAT.rlGlueExtractor.featureLabels();
	}

	/**
	 * Creates string array of labels for network output neurons
	 * 
	 * @return String of labels
	 */
	@Override
	public String[] outputLabels() {
		int numDiscreteActions = MMNEAT.networkOutputs;
		String[] labels = new String[numDiscreteActions];
		for (int i = 0; i < numDiscreteActions; i++) {
			labels[i] = "Action " + i; // There is a distinct label for each action
		}
		return labels;
	}
}
