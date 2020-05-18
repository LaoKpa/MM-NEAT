package edu.southwestern.tasks.loderunner;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import edu.southwestern.parameters.Parameters;
import edu.southwestern.util.MiscUtil;
import gvgai.tools.IO;

/**
 * This class converts VGLC LodeRunner levels into JSON files 
 * @author kdste
 *
 */
public class LodeRunnerVGLCUtil {
	public static final String LODE_RUNNER_LEVEL_PATH = "data/VGLC/Lode Runner/Processed/";
	public static final int LODE_RUNNER_COLUMNS = 32; // This is actually the room height from the original game, since VGLC rotates rooms
	public static final int LODE_RUNNER_ROWS = 22; // Equivalent to width in original game
	
	/**
	 * Converts all the levels in the VGLC to JSON form 
	 * @param args
	 */
	public static void main(String[] args) {
		//Parameters.initializeParameterCollections(new String[] {});
		HashSet<List<List<Integer>>> levelSet = new HashSet<>(); //creates set to represent the level 
		for(int i = 1; i <= 150; i++) {
			String file = "Level " + i + ".txt"; //format for the LodeRunner level files 
			List<List<Integer>> levelList = convertLodeRunnerLevelFileVGLCtoListOfLevel(LODE_RUNNER_LEVEL_PATH + file); //converts to JSON 
			levelSet.add(levelList); //adds the converted list to the set for the level 
		}
		System.out.println(levelSet); //prints converted JSON files to the console 
	}

	/**
	 * Converts the VGLC level of LodeRunner to JSON form to be able to be passed into the GAN
	 * @param fileName File that holds the VGLC of a lode runner level 
	 * @return
	 */
	public static List<List<Integer>> convertLodeRunnerLevelFileVGLCtoListOfLevel(String fileName) {
		String[] level = new IO().readFile(fileName);
		List<List<Integer>> complete = new ArrayList<>(LODE_RUNNER_ROWS);
		//loops through levels to get characters and convert them 
		for(int i = 0; i < level.length; i++) { 
			List<Integer> row = new ArrayList<>(LODE_RUNNER_COLUMNS);
			//System.out.println(level[i]);
			//complete.add(new ArrayList<>()); //adds a new array list to the list at index i 
			for(int j = 0; j < level[i].length(); j++) { //fills that array list that got added to create the row
				if(level[i].charAt(j) != '[' || level[i].charAt(j) != ']') {
					//System.out.print(level[i].charAt(j));
					int tileCode = convertLodeRunnerTileVGLCtoNumberCode(level[i].charAt(j));
					//System.out.println(tileCode);
					row.add(tileCode);
					//System.out.println("row = " + row);
					//complete.get(i).addAll(row); //adds the row that has been converted
				}
			}
			//System.out.println(row);
			//MiscUtil.waitForReadStringAndEnterKeyPress();
			
			complete.add(row); //adds a new array list to the list at index i 
			//complete.get(i).addAll(row); //adds the tile code for conversion
		}
		return complete;
	}

	/**
	 * Converts tile codes to numbers for JSON conversion
	 * @param tile Character describing the tile 
	 * @return The number associated with that tile
	 */
	private static int convertLodeRunnerTileVGLCtoNumberCode(char tile) {
		switch(tile) {
		case '.': //empty, passable
			return 0;	
		case 'G': //gold, passable, pickupable
			return 1; 
		case 'M': //spawn, passable 
			return 2;	
		case 'B': //regular ground, solid
			return 3;
		case 'b': //diggable ground, solid 
			return 4;	 
		case 'E': //enemy, damaging 
			return 5; 
		case '#': //ladder, passable, climbable
			return 6;
		case '-': //rope, passable, climbable 
			return 7;
		default:
			throw new IllegalArgumentException("Invalid Zelda tile from VGLV: " + tile);

		}
	}
}
