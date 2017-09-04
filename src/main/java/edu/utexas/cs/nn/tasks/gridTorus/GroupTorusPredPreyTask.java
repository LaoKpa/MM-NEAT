package edu.utexas.cs.nn.tasks.gridTorus;

import java.util.ArrayList;

import edu.utexas.cs.nn.evolution.Organism;
import edu.utexas.cs.nn.evolution.genotypes.Genotype;
import edu.utexas.cs.nn.gridTorus.TorusPredPreyGame;
import edu.utexas.cs.nn.gridTorus.controllers.TorusPredPreyController;
import edu.utexas.cs.nn.networks.Network;
import edu.utexas.cs.nn.networks.NetworkTask;
import edu.utexas.cs.nn.scores.Score;
import edu.utexas.cs.nn.tasks.GroupTask;

/**
 * Defines a TorusPredPreyTask for groups of evolved agents. 
 * Each agent of the team is evolved individually, with
 * its own genotype (a team of evolving populations).
 * @author Alex Rollins
 * @param <T> phenotype of all evolving populations
 *
 */
public abstract class GroupTorusPredPreyTask<T extends Network> extends GroupTask implements NetworkTask {

	public TorusPredPreyTask<T> task;

	/**
	 * construct a predPrey task based off of the torusPredPreyTask
	 * type that is being evolved
	 */
	public GroupTorusPredPreyTask() {
		task = getLonerTaskInstance();
	}

	/**
	 * gets and returns a loner task instance
	 * @return task, torusPredPreyTask instance
	 */
	public abstract TorusPredPreyTask<T> getLonerTaskInstance();

	/**
	 * an int designating the number of populations to be evolved
	 * @return number of population being evolved as an int
	 */
	@Override
	public int numberOfPopulations(){
		return task.objectives.size();
	}

	/**
	 * an integer array holding the fitness objectives for each population
         * @return number of objectives used by each population 
	 */
	@Override
	public int[] objectivesPerPopulation() {
		int[] result = new int[task.objectives.size()];
		for(int i = 0; i < result.length; i++) {
			result[i] = task.objectives.get(i).size();
		}
		return result;
	}

	/**
	 * an integer array holding the other scores for each population (fitness scores
	 * that are not actually being used in the evaluation and evolution of the agent(s))
         * @return number of other scores used by each population
	 */
	@Override
	public int[] otherStatsPerPopulation() {
		int[] result = new int[task.otherScores.size()];
		for(int i = 0; i < result.length; i++) {
			result[i] = task.otherScores.get(i).size();
		}
		return result;
	}

	/**
	 * gets and returns the time stamp of this task
	 * @return time stamp as a double
	 */
	@Override
	public double getTimeStamp() {
		return task.getTimeStamp();
	}

	/**
	 * nothing needs to be done here
	 */
	@Override
	public void finalCleanup() {
	}

	/**
	 * gets and returns the sensor labels for this task
	 * @return sensor labels in an array of strings
	 */
	@Override
	public String[] sensorLabels() {
		return task.sensorLabels();
	}

	/**
	 * gets and returns the output labels for this task
	 * @return output labels in an array of strings
	 */
	@Override
	public String[] outputLabels() {
		return task.outputLabels();
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	/**
	 * One genotype for each member of the team, and one score for each member
	 * as well
	 *
	 * @param team, list of the genotypes of the teammates
	 * @return list of scores, behaviors, and genotype for each member of the team
	 */
	public ArrayList<Score> evaluate(Genotype[] team) {
		ArrayList<Score> scores = new ArrayList<Score>();

		TorusPredPreyController[] predAgents = getPredAgents(team);
		TorusPredPreyController[] preyAgents = getPreyAgents(team);

		TorusPredPreyGame game = getLonerTaskInstance().runEval(predAgents, preyAgents);

		for(int i = 0; i < numberOfPopulations(); i++){
			//each score : Score(Genotype<T> individual, double[] scores, ArrayList<Double> behaviorVector, double[] otherStats)

			double[] fitnesses = new double[objectivesPerPopulation()[i]];
			double[] otherStats = new double[otherStatsPerPopulation()[i]];

			// Fitness function requires an organism, so make this genotype into an organism
			// this erases information stored about module usage, so was saved in
			// order to be reset after the creation of this organism
			Organism<T> organism = new NNTorusPredPreyAgent<T>(team[i], !TorusPredPreyTask.preyEvolve);
			for (int j = 0; j < fitnesses.length; j++) {
				fitnesses[j] = task.objectives.get(i).get(j).score(game, organism);
			}
			for (int j = 0; j < otherStats.length; j++) {
				otherStats[j] = task.otherScores.get(i).get(j).score(game,organism);
			}
			scores.add(new Score(team[i], fitnesses, null, otherStats));
		}
		return scores;
	}

	/**
	 * gets the prey agents
	 * @param team
	 * @return prey agents
	 */
	public abstract TorusPredPreyController[] getPreyAgents(Genotype<T>[] team);

	/**
	 * gets the pred agents
	 * @param team
	 * @return pred agents
	 */
	public abstract TorusPredPreyController[] getPredAgents(Genotype<T>[] team);

	@Override
	/**
	 * get the min scores of the first populations objectives
	 * @return min scores of first populations objectives as an array of doubles
	 */
	public final double[] minScores() {
		//TODO: this is potentially problematic, as it is not generalized to all populations

		double[] mins = new double[objectivesPerPopulation()[0]];
		for(int i = 0; i < objectivesPerPopulation()[0]; i++){
			mins[i] = task.objectives.get(0).get(i).minScore();
		}
		return mins;
	}

}
