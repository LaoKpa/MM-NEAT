package edu.utexas.cs.nn.evolution.metaheuristics;

import edu.utexas.cs.nn.evolution.EvolutionaryHistory;
import edu.utexas.cs.nn.evolution.genotypes.TWEANNGenotype;
import edu.utexas.cs.nn.networks.TWEANN;
import edu.utexas.cs.nn.parameters.Parameters;
import edu.utexas.cs.nn.scores.Score;

/**
 * Having excessive links per network module is penalized.
 * If penalizeLinksPerMode is false, then links in general
 * are penalized (ignoring modules)
 *
 * @author Jacob Schrum
 */
public class LinkPenalty implements Metaheuristic<TWEANN> {

	private final int populationIndex;
	private final boolean modeAvg;

	public LinkPenalty() {
		this(0);
	}

	public LinkPenalty(int populationIndex) {
		this.populationIndex = populationIndex;
		this.modeAvg = Parameters.parameters.booleanParameter("penalizeLinksPerMode");
	}

	public void augmentScore(Score<TWEANN> s) {
		s.extraScore(getScore((TWEANNGenotype) s.individual));
	}

	public double minScore() {
		int nodes = EvolutionaryHistory.archetypeSize(populationIndex);
		return -(nodes * nodes); // Every node connected to every other
	}

	public double startingTUGGoal() {
		return minScore();
	}

	public double getScore(TWEANNGenotype g) {
		return -(g.links.size() / (1.0 * (modeAvg ? g.numModules : 1)));
	}
}
