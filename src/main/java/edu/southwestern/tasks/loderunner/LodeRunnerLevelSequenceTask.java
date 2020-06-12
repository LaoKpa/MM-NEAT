package edu.southwestern.tasks.loderunner;

import java.util.List;

import edu.southwestern.evolution.genotypes.Genotype;
import edu.southwestern.parameters.Parameters;
import edu.southwestern.util.datastructures.ArrayUtil;
import edu.southwestern.util.datastructures.Pair;


public abstract class LodeRunnerLevelSequenceTask<T> extends LodeRunnerLevelTask<T> {

	public LodeRunnerLevelSequenceTask() {
		super();
	}

	/**
	 * Overrides the oneEval method of LodeRunnerLevelTask to 
	 * evaluate all of the levels of the sequence instead of just a single level
	 * @return The scores 
	 */
	@SuppressWarnings("unchecked")
	@Override
	public Pair<double[], double[]> oneEval(Genotype<T> individual, int num){
		List<List<Integer>>[] levelSequence = getLevelSequence(individual, 5);//right now I set it to have 5 levels in the sequence
		long genotypeId = individual.getId();
		Pair<double[], double[]>[] scoreSequence = new Pair[levelSequence.length];
		for(int i = 0; i < levelSequence.length; i++) {
			//takes in the level it is on, i, and the length of the levelSequence
			double psuedoRandomSeed = differentRandomSeedForEveryLevel(i, levelSequence.length); // TODO: Different seed for each level in the sequnce ... needs abstract method
			scoreSequence[i] = evaluateOneLevel(levelSequence[i], psuedoRandomSeed, genotypeId);
		}
		if(Parameters.parameters.booleanParameter("lodeRunnerLevelSequenceAverages")) {

		}
		else if(Parameters.parameters.booleanParameter("lodeRunnerLevelSequenceIndividual")) {

		}
		return null;	
	}


	/**
	 * Gets a level from the genotype
	 * @return A level 
	 */
	@SuppressWarnings("unchecked")
	@Override
	public List<List<Integer>> getLodeRunnerLevelListRepresentationFromGenotype(Genotype<T> individual) {
		return getLodeRunnerLevelListRepresentationFromStaticGenotype((List<Double>) individual.getPhenotype());
	}

	/**
	 * Calls the method written in LodeRunnerGANLevelTask to return a level from a phenotype
	 * @param phenotype
	 * @return
	 */
	private static List<List<Integer>> getLodeRunnerLevelListRepresentationFromStaticGenotype(List<Double> phenotype) {
		return LodeRunnerGANLevelTask.getLodeRunnerLevelListRepresentationFromGenotypeStatic(phenotype);
	}


	/**
	 * Gets a Random seed for the choosing of a spawn point for generated levels 
	 */
	@Override
	public double getRandomSeedForSpawnPoint(Genotype<T> individual) {
		return getRandomSeedForSpawnPointStatic(individual);
	}

	/**
	 * Called from non-static to return a random seed double 
	 * @param individual
	 * @return Random seed 
	 */
	@SuppressWarnings("unchecked")
	private double getRandomSeedForSpawnPointStatic(Genotype<T> individual) {
		List<Double> latentVector = (List<Double>) individual.getPhenotype(); //creates a double array for the spawn to be placed in GAN levels 
		double[] doubleArray = ArrayUtil.doubleArrayFromList(latentVector);
		double firstLatentVariable = doubleArray[0];
		return firstLatentVariable;
	}

	/**
	 * Gets a sequence of levels 
	 * @param individual Genoty[e
	 * @param numOfLevels Number of levels in the sequence
	 * @return An array holding the number of levels specified
	 */
	public abstract List<List<Integer>>[] getLevelSequence(Genotype<T> individual, int numOfLevels);

	/**
	 * Gets a different random seed for all of the levels in the sequence
	 * @param levelInSequence The level that needs a random seed
	 * @param lengthOfSequence Amount of levels in the sequence
	 * @return Random seed for the level specified 
	 */
	public abstract double differentRandomSeedForEveryLevel(int levelInSequence, int lengthOfSequence);


}