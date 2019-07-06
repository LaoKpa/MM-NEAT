package edu.southwestern.tasks.gvgai.zelda.level;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
import java.util.UUID;

import edu.southwestern.tasks.gvgai.zelda.ZeldaVGLCUtil;
import me.jakerg.rougelike.Tile;

public class OriginalLoader implements LevelLoader {

	List<List<List<Integer>>> levels;
	
	public OriginalLoader() {
		levels = new LinkedList<>();
		String[] dungeonsToLoad = new String[] {"tloz1_1_flip", "tloz2_1_flip", "tloz3_1_flip", "tloz4_1_flip", "tloz5_1_flip", "tloz6_1_flip"};
		loadLevels(dungeonsToLoad);
		
	}
	
	
	private void loadLevels(String[] dungeonsToLoad) {
		Path filePath = new File("data/VGLC/Zelda/Processed").toPath();
		for(String folder : dungeonsToLoad) {
			File dir = filePath.resolve(folder).toFile();
			for(File entry : dir.listFiles()) {
				loadOneLevel(entry);
			}
		}
		
	}

	private void loadOneLevel(File file) {
		String[] levelString = new String[11];
		Scanner scanner;
		try {
			scanner = new Scanner(file);
			int i = 0;
			while(scanner.hasNextLine())
				levelString[i++] = scanner.nextLine();
				
			List<List<Integer>> levelInt = ZeldaVGLCUtil.convertZeldaLevelVGLCtoRoomAsList(levelString);
			removeDoors(levelInt);
			levels.add(levelInt);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		
	}

	private void removeDoors(List<List<Integer>> levelInt) {
		for(int y = 0; y < levelInt.size(); y++) {
			for(int x = 0; x < levelInt.get(y).size(); x++) {
				Integer num = levelInt.get(y).get(x);
				Tile tile = Tile.findNum(num);
				switch(tile) {
				case DOOR:
				case LOCKED_DOOR:
					num = Tile.WALL.getNum();
					break;
				case TRIFORCE:
					num = Tile.FLOOR.getNum();
					break;
				}
				levelInt.get(y).set(x, num);
					
			}
		}
	}


	@Override
	public List<List<List<Integer>>> getLevels() {
		return new LinkedList<>(levels);
	}

}