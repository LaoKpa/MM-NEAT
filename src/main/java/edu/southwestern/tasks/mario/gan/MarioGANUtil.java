package edu.southwestern.tasks.mario.gan;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import ch.idsia.ai.agents.Agent;
import ch.idsia.ai.agents.human.HumanKeyboardAgent;
import ch.idsia.ai.tasks.ProgressTask;
import ch.idsia.mario.engine.LevelRenderer;
import ch.idsia.mario.engine.level.Level;
import ch.idsia.tools.CmdLineOptions;
import ch.idsia.tools.EvaluationOptions;
import edu.southwestern.parameters.Parameters;
import edu.southwestern.tasks.mario.gan.reader.JsonReader;
import edu.southwestern.tasks.mario.level.OldLevelParser;
import edu.southwestern.tasks.mario.level.LevelParser;
import edu.southwestern.tasks.mario.level.MarioLevelUtil;
import edu.southwestern.util.PythonUtil;
import edu.southwestern.util.datastructures.ArrayUtil;
import edu.southwestern.util.graphics.DrawingPanel;

/**
 * Create Mario levels using a trained GAN as done in the Mario GAN paper.
 * 
 * @author Jacob Schrum
 *
 */
public class MarioGANUtil {

	private static GANProcess ganProcess = null;

	public static int latentVectorLength() {
		return getGANProcess().getLatentVectorSize();
	}
	
	/**
	 * Start the GAN process running in Python if it has not started already.
	 * Otherwise, just return the reference to the process.
	 * @return Process running the Mario GAN
	 */
	private static GANProcess getGANProcess() {
		// This code comes from the constructor for MarioEvalFunction in the MarioGAN project
		if(ganProcess == null) {
			PythonUtil.setPythonProgram();
			// set up process for GAN
			ganProcess = new GANProcess();
			ganProcess.start();
			// consume all start-up messages that are not data responses
			String response = "";
			while(!response.equals("READY")) {
				response = ganProcess.commRecv();
			}
		}
		return ganProcess;
	}
	
	/**
	 * From MarioGAN
	 * 
	 * Map the value in R to (-1, 1)
	 * @param valueInR
	 * @return Range restricted value
	 */
	public static double mapToOne(double valueInR) {
		return ( valueInR / Math.sqrt(1+valueInR*valueInR) );
	}

	/**
	 * From MarioGAN
	 * 
	 * Perform the operation above to a whole array
	 * 
	 * @param arrayInR
	 * @return Array with values in range
	 */
	public static double[] mapArrayToOne(double[] arrayInR) {
		double[] newArray = new double[arrayInR.length];
		for(int i=0; i<newArray.length; i++) {
			double valueInR = arrayInR[i];
			newArray[i] = mapToOne(valueInR);
		}
		return newArray;
	}

	/**
	 * Has same core functionality as the levelFromLatentVector method in MarioEvalFunction
	 * of MarioGAN project

	 * @param latentVector
	 * @return Mario level
	 */
	public static Level generateLevelFromGAN(double[] latentVector) {
		latentVector = mapArrayToOne(latentVector); // Range restrict the values
		int chunk_length = Integer.valueOf(getGANProcess().GANDim);
        String levelString = "";
        for(int i = 0; i < latentVector.length; i+=chunk_length){
            double[] chunk = Arrays.copyOfRange(latentVector, i, i+chunk_length);
            // Generate a level from the vector
            // Brackets required since generator.py expects of list of multiple levels, though only one is being sent here
            try {
            	getGANProcess().commSend("[" + Arrays.toString(chunk) + "]");
            } catch (IOException e) {
            	e.printStackTrace();
            	System.exit(1); // Cannot continue without the GAN process
            }
            String oneLevelChunk = getGANProcess().commRecv(); // Response to command just sent
            levelString = levelString + ", " + oneLevelChunk;  
        }
        // These two lines remove the , from the first append to an empty string
        levelString = levelString.replaceFirst(",", "");
        levelString = levelString.replaceFirst(" ", "");
        levelString = "["+levelString+"]"; // Make a bundle of several levels
        // Create one level from all
        List<List<List<Integer>>> allLevels = JsonReader.JsonToInt(levelString);
		// This list contains several separate levels. The following code
		// merges the levels by appending adjacent rows
		ArrayList<List<Integer>> oneLevel = new ArrayList<List<Integer>>();
		// Create the appropriate number of rows in the array
		for(@SuppressWarnings("unused") List<Integer> row : allLevels.get(0)) { // Look at first level (assume all are same size)
			oneLevel.add(new ArrayList<Integer>()); // Empty row
		}
		// Now fill up the rows, one level at a time
		for(List<List<Integer>> aLevel : allLevels) {
			int index = 0;
			for(List<Integer> row : aLevel) { // Loot at each row
				oneLevel.get(index++).addAll(row);
			}	
		}
		// Create one level from the merged level representation
		Level level = Parameters.parameters.booleanParameter("marioGANUsesOriginalEncoding") ? OldLevelParser.createLevelJson(oneLevel) : LevelParser.createLevelJson(oneLevel);
		return level;
	}	
	
	/**
	 * From MarioGAN
	 * 
	 * Takes a json String representing several levels 
	 * and returns an array of all of those Mario levels.
	 * In order to convert a single level, it needs to be put into
	 * a json array by adding extra square brackets [ ] around it.
	 * @param json Json String representation of multiple Mario levels
	 * @return Array of those levels
	 */
	public static Level[] marioLevelsFromJson(String json) {
		List<List<List<Integer>>> allLevels = JsonReader.JsonToInt(json);
		Level[] result = new Level[allLevels.size()];
		int index = 0;
		for(List<List<Integer>> listRepresentation : allLevels) {
			result[index++] = Parameters.parameters.booleanParameter("marioGANUsesOriginalEncoding") ? OldLevelParser.createLevelJson(listRepresentation) : LevelParser.createLevelJson(listRepresentation);
		}
		return result;
	}

	/**
	 * For quick testing
	 * @param args
	 */
	public static void main(String[] args) {
		Parameters.initializeParameterCollections(new String[0]);
		PythonUtil.setPythonProgram();
		double[] latentVector = new double[] {0.9881835842209917, -0.9986077315374948, 0.9995512051242508, 0.9998643432807639, -0.9976165917284504, -0.9995247114230822, -0.9997001909358728, 0.9995694511739592, -0.9431036754879115, 0.9998155541290887, 0.9997863689962382, -0.8761392912669269, -0.999843833016589, 0.9993230720045649, 0.9995470247917402, -0.9998847606084427, -0.9998322053148382, 0.9997707200294411, -0.9998905141832997, -0.9999512510490688, -0.9533512808031753, 0.9997703088007039, -0.9992229823819915, 0.9953917828622341, 0.9973473366437476, 0.9943030781608361, 0.9995290290713732, -0.9994945079679955, 0.9997109900652238, -0.9988379572928884, 0.9995070647543864, 0.9994132207570211};
		//Level level = generateLevelFromGAN(latentVector); // 688
		//Level level = generateLevelFromGAN(ArrayUtil.combineArrays(latentVector,latentVector)); // 1136
		Level level = generateLevelFromGAN(ArrayUtil.combineArrays(latentVector,ArrayUtil.combineArrays(latentVector,latentVector))); // 1584

		// Code below is the same as in MarioLevelUtil, which generates CPPN levels.
		// Should these utility classes be merged?
		
		Agent controller = new HumanKeyboardAgent(); //new SergeyKarakovskiy_JumpingAgent();
		EvaluationOptions options = new CmdLineOptions(new String[]{});
		options.setAgent(controller);
		ProgressTask task = new ProgressTask(options);

		// Added to change level
        options.setLevel(level);

		task.setOptions(options);

		int relevantWidth = (level.width - (2*OldLevelParser.BUFFER_WIDTH)) * MarioLevelUtil.BLOCK_SIZE;
		DrawingPanel levelPanel = new DrawingPanel(relevantWidth,level.height*MarioLevelUtil.BLOCK_SIZE, "Level");
		LevelRenderer.renderArea(levelPanel.getGraphics(), level, 0, 0, OldLevelParser.BUFFER_WIDTH*MarioLevelUtil.BLOCK_SIZE, 0, relevantWidth, level.height*MarioLevelUtil.BLOCK_SIZE);
		
		System.out.println ("Score: " + task.evaluate(options.getAgent())[0]);
				
	}
}
