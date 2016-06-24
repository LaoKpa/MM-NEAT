package edu.utexas.cs.nn.tasks.mspacman.sensors.blocks.hyperneat;

import edu.utexas.cs.nn.tasks.mspacman.MsPacManTask;
import edu.utexas.cs.nn.tasks.mspacman.facades.GameFacade;
import edu.utexas.cs.nn.tasks.mspacman.sensors.blocks.MsPacManSensorBlock;
/**
 * Gets sensor block for power pill substrate in hyperNEAT msPacMan task
 * @author Lauren Gillespie
 *
 */
public class SubstratePowerPillSensorBlock extends MsPacManSensorBlock {

	@Override
	/**
	 * adds sensor inputs from this block of sensor
	 * @param inputs inputs array
	 * @param startPoint starting point in inputs array for this sensor block
	 * @param lastDirection ms PacMan's last direction
	 * @return last used index in inputs array from this sensor block
	 */
	public int incorporateSensors(double[] inputs, int startPoint, GameFacade gf, int lastDirection) {
		int[] powerPillIndices = gf.getActivePowerPillsIndices();
		int endPoint = startPoint;
		for(int i = 0; i < powerPillIndices.length; i++) {
			int x = gf.getNodeXCoord(powerPillIndices[i]);
			int y = gf.getNodeYCoord(powerPillIndices[i]);
			if(x < MsPacManTask.MS_PAC_MAN_SUBSTRATE_WIDTH / 2 && y < MsPacManTask.MS_PAC_MAN_SUBSTRATE_HEIGHT) { 
				inputs[startPoint] = 1;
			} else if(x > MsPacManTask.MS_PAC_MAN_SUBSTRATE_WIDTH / 2 && y < MsPacManTask.MS_PAC_MAN_SUBSTRATE_HEIGHT){
				inputs[startPoint++] = 1;
			} else if(x < MsPacManTask.MS_PAC_MAN_SUBSTRATE_WIDTH / 2 && y > MsPacManTask.MS_PAC_MAN_SUBSTRATE_HEIGHT) {
				inputs[startPoint + 2] = 1;
			} else {
				inputs[startPoint + 3] = 1;
			}
		}
		return endPoint + 4;
	}

	@Override
	/**
	 * Gets the string label associated with the x-y coordinates
	 * from sensor label
	 * @return String sensor label
	 */
	public int incorporateLabels(String[] labels, int in) {
		labels[in++] = "Upper Left Power Pill";
		labels[in++] = "Upper Right Power Pill";
		labels[in++] = "Lower Left Power Pill";
		labels[in++] = "Lower Right Power Pill";
		return in;
	}

	@Override
	/**
	 * Returns number of sensors added
	 * @return num sensors added
	 */
	public int numberAdded() {
		return 4; // magic number!
	}

	
	
}
