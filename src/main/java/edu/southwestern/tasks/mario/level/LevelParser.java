/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.southwestern.tasks.mario.level;

import ch.idsia.mario.engine.level.Level;
import ch.idsia.mario.engine.level.SpriteTemplate;
import ch.idsia.mario.engine.sprites.Enemy;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author vv
 */
public class LevelParser {

	/**
	 *
	 * @param args
	 */
	public LevelParser(){

	}


	/*"tiles" : {
    0    "X" : ["solid","ground"],
    1    "S" : ["solid","breakable"],
    2    "-" : ["passable","empty"],
    3    "?" : ["solid","question block", "full question block"],
    4    "Q" : ["solid","question block", "empty question block"],
    5    "E" : ["enemy","damaging","hazard","moving"],
    6    "<" : ["solid","top-left pipe","pipe"],
    7    ">" : ["solid","top-right pipe","pipe"],
    8    "[" : ["solid","left pipe","pipe"],
    9    "]" : ["solid","right pipe","pipe"],
    10   "o" : ["coin","collectable","passable"]
	 */

	public Level test(){
		// TODO: Fix hard coding
		Level level = new Level(202,14);
		level.setBlock(1, 13, (byte) 9);
		level.setBlock(2, 13, (byte) 9);
		level.setBlock(3, 13, (byte) 9);
		level.setBlock(4, 13, (byte) 9);
		level.setBlock(5, 13, (byte) 9);
		level.setBlock(6, 13, (byte) 9);
		level.setBlock(7, 13, (byte) 9);

		return level;
	}

	/**
	 * Create level from text file with 2D arrangement of
	 * level content.
	 * 
	 * @param filename
	 * @return
	 */
	public Level createLevelASCII(String filename)
	{
		//Read in level representation
		ArrayList<String> lines = new ArrayList<String>();
		try {
			File file = new File(filename);
			FileReader fileReader = new FileReader(file);
			BufferedReader bufferedReader = new BufferedReader(fileReader);
			String line;
			while ((line = bufferedReader.readLine()) != null) {
				lines.add(line);
			}
			fileReader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return createLevelASCII(lines);
	}        

	/**
	 * Create level from list of Strings corresponding to the 2D
	 * layout of the level.
	 * 
	 * @param lines
	 * @return
	 */
	public Level createLevelASCII(ArrayList<String> lines)
	{
		int width = lines.get(0).length();
		int height = lines.size();
		int extraStones = 15;
		Level level = new Level(width+2*extraStones,height);

		//Set Level Exit
		//Extend level by that
		level.xExit = width+extraStones;
		level.yExit = height-1;

		for(int i=0; i<extraStones; i++){
			level.setBlock(i, height-1, (byte) 9);
		}


		for(int i=0; i<extraStones; i++){
			level.setBlock(width+i+extraStones, height-1, (byte) 9);
		}



		//set Level map
		for(int i=0; i<height; i++){
			for(int j=0; j<lines.get(i).length(); j++){
				String code = String.valueOf(lines.get(i).charAt(j));
				if("E".equals(code)){
					//set Enemy
					//new SpriteTemplate(type, boolean winged)
					level.setSpriteTemplate(j+extraStones, i+1, new SpriteTemplate(Enemy.ENEMY_GOOMBA, false));
					//System.out.println("j: "+j+" i:"+i);
					//set passable tile: everything not set is passable
				}else{
					int encoded = codeParserASCII(code);
					if(encoded !=0){
						level.setBlock(j+extraStones, i+1, (byte) encoded);
						//System.out.println("j: "+j+" i:"+i+" encoded: "+encoded);
					}
				}
			}
		}

		return level;
	}


	public Level createLevelJson(List<List<Integer>> input)
	{
		int width = input.get(0).size();
		int height = input.size();
		int extraStones = 15;
		Level level = new Level(width+2*extraStones,height);

		//Set Level Exit
		//Extend level by that
		level.xExit = width+extraStones;
		level.yExit = height-1;

		for(int i=0; i<extraStones; i++){
			level.setBlock(i, height-1, (byte) 9);
		}
		for(int i=0; i<extraStones; i++){
			level.setBlock(width+i+extraStones, height-1, (byte) 9);
		}

		//set Level map
		for(int i=0; i<height; i++){
			for(int j=0; j<width; j++){
				int code = input.get(i).get(j);
				if(5==code){
					//set Enemy
					//new SpriteTemplate(type, boolean winged)
					level.setSpriteTemplate(j+extraStones, i, new SpriteTemplate(Enemy.ENEMY_GOOMBA, false));
					//System.out.println("j: "+j+" i:"+i);
					//set passable tile: everything not set is passable
				}else{
					int encoded = codeParser(code);
					if(encoded !=0){
						level.setBlock(j+extraStones, i, (byte) encoded);
						//System.out.println("j: "+j+" i:"+i+" encoded: "+encoded);
					}
				}
			}
		}

		return level;
	}



	public int codeParser(int code){
		int output = 0;
		switch(code){
		case 0: output = 9; break; //rocks
		case 1: output = 16; break; //"S" : ["solid","breakable"]
		case 3: output = 21; break; //"?" : ["solid","question block", "full question block"]
		case 6: output = 10; break; //"<" : ["solid","top-left pipe","pipe"]
		case 7: output = 11; break; //">" : ["solid","top-right pipe","pipe"]
		case 8: output = 26; break; //"[" : ["solid","left pipe","pipe"]
		case 9: output = 27; break; //"]" : ["solid","right pipe","pipe"]
		case 10: output = 34; break; //"o" : ["coin","collectable","passable"]
		default: output=0; break; //"-" : ["passable","empty"],  "Q" : ["solid","question block", "empty question block"],  "E" : ["enemy","damaging","hazard","moving"],
		}
		return output;
	}

	public int codeParserASCII(String code){
		int output = 0;
		switch(code){
		case "X": output = 9; break; //rocks
		case "S": output = 16; break; //"S" : ["solid","breakable"]
		case "?": output = 21; break; //"?" : ["solid","question block", "full question block"]
		case "<": output = 10; break; //"<" : ["solid","top-left pipe","pipe"]
		case ">": output = 11; break; //">" : ["solid","top-right pipe","pipe"]
		case "[": output = 26; break; //"[" : ["solid","left pipe","pipe"]
		case "]": output = 27; break; //"]" : ["solid","right pipe","pipe"]
		case "o": output = 34; break; //"o" : ["coin","collectable","passable"]
		default: output=0; break; //"-" : ["passable","empty"],  "Q" : ["solid","question block", "empty question block"],  "E" : ["enemy","damaging","hazard","moving"],
		}
		return output;
	}

}
