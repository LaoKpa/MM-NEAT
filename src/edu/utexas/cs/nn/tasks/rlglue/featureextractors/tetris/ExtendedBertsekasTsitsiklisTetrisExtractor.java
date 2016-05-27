package edu.utexas.cs.nn.tasks.rlglue.featureextractors.tetris;

import org.rlcommunity.environments.tetris.TetrisState;
import org.rlcommunity.rlglue.codec.types.Observation;

public class ExtendedBertsekasTsitsiklisTetrisExtractor extends BertsekasTsitsiklisTetrisExtractor {

	/**
	 * Returns the number of features for this extractor
	 */
	@Override
	public int numFeatures() {
		return super.numFeatures() + TetrisState.worldWidth;
		// column heights + column diffs + Max Height + total holes + bias + column holes
	}

	/**
	 * Scales the given inputs to fit the range [0 to 1]
	 * @param inputs double[]
	 * @return scaled inputs double[]
	 */
	@Override
	public double[] scaleInputs(double[] inputs) {
		double[] original = super.scaleInputs(inputs);
		int originalFeatures = TetrisState.worldWidth + (TetrisState.worldWidth - 1) + 3;
		for (int i = originalFeatures; i < inputs.length; i++) {
			original[i] = inputs[i] / TetrisState.worldHeight;
		}
		return original;
	}

	/**
	 * Returns the feature labels for the given extractor
	 */
	@Override
	public String[] featureLabels() {
		String[] labels = super.featureLabels();
		int originalFeatures = TetrisState.worldWidth + (TetrisState.worldWidth - 1) + 3; 
		for (int i = originalFeatures; i < labels.length; i++) {
			labels[i] = "Column " + i + " Holes";
		}
		return labels;
	}

	/**
	 * Extract extends from BertsekasTsitsiklisTetrisExtractor and adds 
	 * the number of holes per column
	 * 
	 * @param o Observation 
	 * @return array of inputs
	 */
	@Override
	public double[] extract(Observation o) {
		double[] base = super.extract(o);

		int[] worldState = new int[worldWidth * worldHeight];
		System.arraycopy(o.intArray, 0, worldState, 0, worldWidth * worldHeight);
		double[] blockIndicator = new double[possibleBlocks.size()];
		for (int i = 0; i < possibleBlocks.size(); i++) {
			blockIndicator[i] = o.intArray[worldState.length + i];
		}

		double[] added = new double[worldWidth];
		for (int i = 0; i < added.length; i++) { // finds the number of holes
													// for the current column
													// and adds that to Added
			double h = columnHeight(i, worldState);
			added[i] = columnHoles(i, worldState, (int) h);
		}

		double[] combined = new double[super.numFeatures() + added.length];
		System.arraycopy(base, 0, combined, 0, super.numFeatures());
		System.arraycopy(added, 0, combined, super.numFeatures(), added.length);

		// System.out.println(Arrays.toString(combined));
		return combined;
	}
}
