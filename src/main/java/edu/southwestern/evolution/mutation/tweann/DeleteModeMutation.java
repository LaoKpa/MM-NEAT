package edu.utexas.cs.nn.evolution.mutation.tweann;

import edu.utexas.cs.nn.evolution.genotypes.Genotype;
import edu.utexas.cs.nn.evolution.genotypes.TWEANNGenotype;
import edu.utexas.cs.nn.networks.TWEANN;
import edu.utexas.cs.nn.parameters.CommonConstants;

/**
 *
 *This mutation operator currently does not work because of
 *details with how the network archetype manages innovation numbers
 *and node layout. DO NOT USE.
 *
 * @author Jacob Schrum
 */
public class DeleteModeMutation extends TWEANNMutation {

	/**
	 * Default constructor
	 */
	public DeleteModeMutation() {
		//command line parameter, "Mutation rate for deleting network modes"
		super("deleteModeRate");
		throw new UnsupportedOperationException("Mode deletion currently does not work");
	}

	/**
	 * mutates genotype by deleting least used module
	 * or a random module
	 * @param genotype TWEANNGenotype to be mutated
	 */
	public void mutate(Genotype<TWEANN> genotype) {
		if (CommonConstants.deleteLeastUsed) {
			((TWEANNGenotype) genotype).deleteLeastUsedModeMutation();
		} else {
			((TWEANNGenotype) genotype).deleteRandomModeMutation();
		}
	}
}
