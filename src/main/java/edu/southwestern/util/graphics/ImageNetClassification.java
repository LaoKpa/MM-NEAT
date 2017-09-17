package edu.southwestern.util.graphics;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.datavec.image.loader.NativeImageLoader;
import org.deeplearning4j.nn.graph.ComputationGraph;
import org.deeplearning4j.nn.modelimport.keras.InvalidKerasConfigurationException;
import org.deeplearning4j.nn.modelimport.keras.KerasModelImport;
import org.deeplearning4j.nn.modelimport.keras.Model;
import org.deeplearning4j.nn.modelimport.keras.UnsupportedKerasConfigurationException;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.zoo.PretrainedType;
import org.deeplearning4j.zoo.ZooModel;
import org.deeplearning4j.zoo.util.imagenet.ImageNetLabels;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.api.preprocessor.DataNormalization;
import org.nd4j.linalg.dataset.api.preprocessor.VGG16ImagePreProcessor;
import org.nd4j.linalg.factory.Nd4j;

import edu.southwestern.util.ClassCreation;
import edu.southwestern.util.datastructures.ArrayUtil;
import edu.southwestern.util.stats.StatisticsUtilities;

public class ImageNetClassification {
	public static final int NUM_IMAGE_NET_CLASSES = 1000;
	public static final int IMAGE_NET_INPUT_HEIGHT = 227; //224;
	public static final int IMAGE_NET_INPUT_WIDTH = 227; //224;
	public static final int IMAGE_NET_INPUT_CHANNELS = 3;
	
	// Do not take the time to initialize this if not needed
	private static ComputationGraph imageNet = null;
	//private static MultiLayerNetwork imageNet = null; // For a Keras model
	private static ImageNetLabels imageNetLabels = null;
	/**
	 * Initialize the ImageNet if it hasn't been done yet. This is only done
	 * once because the net weights should never change. Saving the result allows
	 * it to be re-used without re-initialization
	 */
	public static void initImageNet() {
		// This was my attempt to import a pre-trained set of AlexNet weights from a Keras model for ImageNet
//		try {
//			String modelHdf5Filename = "../alexnet_weights.h5";
//			imageNet = KerasModelImport.importKerasSequentialModelAndWeights(modelHdf5Filename);
//		} catch (IOException | InvalidKerasConfigurationException | UnsupportedKerasConfigurationException e) {
//			System.out.println("Could not initialize ImageNet!");
//			e.printStackTrace();
//			System.exit(1);
//		}
		
		try {
			@SuppressWarnings("rawtypes")
			ZooModel model = (ZooModel) ClassCreation.createObject("imageNetModel");
			imageNet = (ComputationGraph) model.initPretrained(PretrainedType.IMAGENET);
		} catch (IOException | NoSuchMethodException e) {
			System.out.println("Could not initialize ImageNet!");
			e.printStackTrace();
			System.exit(1);
		}
		// If image net is being used, then the labels will be needed as well
		imageNetLabels = new ImageNetLabels();
	}
	
	/**
	 * Creates an INDArray from a BufferedImage, assuming that the image needs to be loaded
	 * into a size appropriate for ImageNet.
	 * @param image Buffered Image, such as those generated by CPPNs
	 * @return Image stored in an INDArray
	 */
	public static INDArray bufferedImageToINDArray(BufferedImage image) {
		NativeImageLoader loader = new NativeImageLoader(IMAGE_NET_INPUT_HEIGHT, IMAGE_NET_INPUT_WIDTH, IMAGE_NET_INPUT_CHANNELS);
		try {
			return loader.asMatrix(image);
		} catch (IOException e) {
			System.out.println("Could not convert image to INDArray");
			e.printStackTrace();
			System.exit(1);
		}
		return null;
	}
	
	/**
	 * Takes an image represented within an INDArray and returns all of the scores
	 * that ImageNet assigns for each of its 1000 labels. Image Net may need to be
	 * initialized first.
	 * 
	 * @param image Image is a 2D matrix within DL4J's INDArray class
	 * @param preprocess Whether the image/matrix needs to be scaled to the appropriate size for ImageNet
	 * @return map of label to score for each of 1000 labels
	 */
	public static Map<String, Float> getImageNetLabelledPredictions(INDArray image, boolean preprocess) {		
		INDArray currentBatch = getImageNetPredictions(image, preprocess);
		return getImageNetLabelledPredictions(currentBatch);
	}
	
	/**
	 * Take an INDArray already that has already been processes by ImageNet (the output scores)
	 * and assign the ImageNet labels to them.
	 * @param precomputerScores Output of getImageNetPredictions
	 * @return Map of ImageNet labels to scores
	 */
	public static Map<String, Float> getImageNetLabelledPredictions(INDArray precomputedScores) {
		Map<String, Float> result = new HashMap<>();		
		for (int i = 0; i < NUM_IMAGE_NET_CLASSES; i++) {
			//System.out.println(labels.getLabel(i) + ": "+(currentBatch.getFloat(0,i)*100) + "%");
			result.put(imageNetLabels.getLabel(i), precomputedScores.getFloat(0,i));
		}
		return result;
	}
	
	/**
	 * Get raw ImageNet prediction scores from ImageNet without any labels.
	 * @param image Image is a 2N matrix within DL4J's INDArray class
	 * @param preprocess Whether the image/matrix needs to be scaled to the appropriate size for ImageNet
	 * @return INDArray of prediction scores for ImageNet's categories/labels
	 */
	public static INDArray getImageNetPredictions(INDArray image, boolean preprocess) {
		if(imageNet == null) initImageNet();
		if(preprocess) {
			DataNormalization scaler = new VGG16ImagePreProcessor();
			scaler.transform(image);
		}		
		INDArray predictions = imageNet.outputSingle(image);
		//INDArray predictions = imageNet.output(image);
		return predictions.getRow(0).dup(); // Should I duplicate with dup? Worth the load? Needed?
	}
	
	/**
	 * Get ImageNet label with the highest score in the collection of prediction scores
	 * @param precomputedScores Computed by getImageNetPredictions
	 * @return String label with highest score
	 */
	public static String bestLabel(INDArray precomputedScores) {
		int index = Nd4j.argMax(precomputedScores, 1).getInt(0, 0);
		return imageNetLabels.getLabel(index);
	}

	/**
	 * Same as above, but for when the scores have been converted to an ArrayList, as required
	 * to store them as a behavior characterization in a Score instance.
	 * @param precomputedScores from a Score, but based off of getImageNetPredictions
	 * @return String label with highest score
	 */
	public static String bestLabel(ArrayList<Double> precomputedScores) {
		int index = StatisticsUtilities.argmax(ArrayUtil.doubleArrayFromList(precomputedScores));
		return imageNetLabels.getLabel(index);
	}
		
	/**
	 * Get best/max score, the one corresponding to the predicted label
	 * @param precomputedScores Computed by getImageNetPredictions
	 * @returnhighest score
	 */
	public static double bestScore(INDArray precomputedScores) {
		return Nd4j.max(precomputedScores, 1).getDouble(0,0);
	}

}
