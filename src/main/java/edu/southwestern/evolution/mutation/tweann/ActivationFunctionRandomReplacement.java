package edu.utexas.cs.nn.evolution.mutation.tweann;

import edu.utexas.cs.nn.evolution.genotypes.Genotype;
import edu.utexas.cs.nn.evolution.genotypes.TWEANNGenotype;
import edu.utexas.cs.nn.evolution.genotypes.TWEANNGenotype.NodeGene;
import edu.utexas.cs.nn.networks.ActivationFunctions;
import edu.utexas.cs.nn.networks.TWEANN;

public class ActivationFunctionRandomReplacement extends TWEANNMutation{

	public ActivationFunctionRandomReplacement() {
		super(1.0);//for its use, will always happen, rate will be 1
	}
	
	/**
	 * sets all ftypes in the genotype to random activation functions available in ActivationFunctions
	 */
	@Override
	public void mutate(Genotype<TWEANN> genotype) {//randomizes all ftypes based on available activation functions
		TWEANNGenotype geno = (TWEANNGenotype) genotype;
		for(NodeGene node: geno.nodes){ //loops through all ftypes
			node.ftype = ActivationFunctions.randomFunction();
		}
		
	}

}
