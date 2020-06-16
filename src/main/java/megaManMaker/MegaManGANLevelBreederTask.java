package megaManMaker;

import java.awt.Dimension;
import java.awt.Font;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Scanner;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import com.google.common.io.Files;

import edu.southwestern.MMNEAT.MMNEAT;
import edu.southwestern.parameters.Parameters;
import edu.southwestern.tasks.interactive.InteractiveGANLevelEvolutionTask;
import edu.southwestern.tasks.mario.gan.GANProcess;
import edu.southwestern.util.datastructures.ArrayUtil;
import edu.southwestern.util.datastructures.Pair;
import edu.southwestern.util.search.AStarSearch;
import edu.southwestern.util.search.Search;
import megaManMaker.MegaManState.MegaManAction;

public class MegaManGANLevelBreederTask extends InteractiveGANLevelEvolutionTask{
	public static final int LEVEL_MIN_CHUNKS = 1;
	public static final int LEVEL_MAX_CHUNKS = 10;
	public static final int SAVE_BUTTON_INDEX = -19; 
	public static GANProcess ganProcessVertical = null;
	public static GANProcess ganProcessHorizontal = null;
	private boolean initializationComplete = false;
	public static List<List<Integer>> level;
	protected JSlider levelChunksSlider;
	public MegaManGANLevelBreederTask() throws IllegalAccessException {
		super();
		//save button
		JButton save = new JButton("SaveMMLV");
		// Name is first available numeric label after the input disablers
		save.setName("" + SAVE_BUTTON_INDEX);
		save.setToolTipText("Save a selected level.");
		save.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				saveLevel();
			}
		});
		
		if(Parameters.parameters.booleanParameter("bigInteractiveButtons")) {
			save.setFont(new Font("Arial", Font.PLAIN, BIG_BUTTON_FONT_SIZE));
		}
		
		top.add(save);
		
		levelChunksSlider = new JSlider(JSlider.HORIZONTAL, LEVEL_MIN_CHUNKS, LEVEL_MAX_CHUNKS, Parameters.parameters.integerParameter("megaManGANLevelChunks"));
		levelChunksSlider.setToolTipText("Determines the number of distinct latent vectors that are sent to the GAN to create level chunks which are patched together into a single level.");
		levelChunksSlider.setMinorTickSpacing(1);
		levelChunksSlider.setPaintTicks(true);
		Hashtable<Integer,JLabel> labels = new Hashtable<>();
		JLabel shorter = new JLabel("Shorter Level");
		JLabel longer = new JLabel("Longer Level");
		if(Parameters.parameters.booleanParameter("bigInteractiveButtons")) {
			shorter.setFont(new Font("Arial", Font.PLAIN, 23));
			longer.setFont(new Font("Arial", Font.PLAIN, 23));
		}
		labels.put(LEVEL_MIN_CHUNKS, shorter);
		labels.put(LEVEL_MAX_CHUNKS, longer);
		levelChunksSlider.setLabelTable(labels);
		levelChunksSlider.setPaintLabels(true);
		levelChunksSlider.setPreferredSize(new Dimension((int)(200 * (Parameters.parameters.booleanParameter("bigInteractiveButtons") ? 1.4 : 1)), 40 * (Parameters.parameters.booleanParameter("bigInteractiveButtons") ? 2 : 1)));

		/**
		 * Changed level width picture previews
		 */
		levelChunksSlider.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				if(!initializationComplete) return;
				// get value
				JSlider source = (JSlider)e.getSource();
				if(!source.getValueIsAdjusting()) {

					int oldValue = Parameters.parameters.integerParameter("megaManGANLevelChunks");
					int newValue = (int) source.getValue();
					Parameters.parameters.setInteger("megaManGANLevelChunks", newValue);
					//Parameters.parameters.setInteger("GANInputSize", 5*newValue); // Default latent vector size

					if(oldValue != newValue) {
						int oldLength = oldValue * GANProcess.latentVectorLength();
						int newLength = newValue * GANProcess.latentVectorLength();

						resizeGenotypeVectors(oldLength, newLength);
						resetButtons(true);

						// reset buttons
					}
				}
			}
		});

		if(!Parameters.parameters.booleanParameter("simplifiedInteractiveInterface")) {
			top.add(levelChunksSlider);	
		}

		initializationComplete = true;
		
		JPanel effectsCheckboxes = new JPanel();
		JCheckBox showSolutionPath = new JCheckBox("ShowSolutionPath", Parameters.parameters.booleanParameter("interactiveMegaManAStarPaths"));
		showSolutionPath.setName("interactiveMegaManAStarPaths");
		showSolutionPath.getAccessibleContext();
		showSolutionPath.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Parameters.parameters.changeBoolean("interactiveMegaManAStarPaths");
				resetButtons(true);
			}
		});
		effectsCheckboxes.add(showSolutionPath);
		//top.add(effectsCheckboxes);
		
		//JPanel useBothGANs = new JPanel();
		JCheckBox useBothGANs = new JCheckBox("UseBothGANs", Parameters.parameters.booleanParameter("useBothGANsMegaMan"));
		useBothGANs.setName("useBothGANsMegaMan");
		useBothGANs.getAccessibleContext();
		useBothGANs.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Parameters.parameters.changeBoolean("useBothGANsMegaMan");
				if(Parameters.parameters.booleanParameter("useBothGANsMegaMan")) {
					
				GANProcess.terminateGANProcess();
				//PythonUtil.setPythonProgram();
				ganProcessHorizontal = new GANProcess(GANProcess.PYTHON_BASE_PATH+"MegaManGAN"+ File.separator + Parameters.parameters.stringParameter("MegaManGANHorizontalModel"), 
						Parameters.parameters.integerParameter("GANInputSize"), 
						/*Parameters.parameters.stringParameter("MegaManGANModel").startsWith("HORIZONTALONLYMegaManAllLevel") ? */MegaManGANUtil.MEGA_MAN_ALL_TERRAIN /*: MegaManGANUtil.MEGA_MAN_FIRST_LEVEL_ALL_TILES*/,
						GANProcess.MEGA_MAN_OUT_WIDTH, GANProcess.MEGA_MAN_OUT_HEIGHT);
				ganProcessVertical = new GANProcess(GANProcess.PYTHON_BASE_PATH+"MegaManGAN"+ File.separator + Parameters.parameters.stringParameter("MegaManGANVerticalModel"), 
						Parameters.parameters.integerParameter("GANInputSize"), 
						/*Parameters.parameters.stringParameter("MegaManGANModel").startsWith("HORIZONTALONLYMegaManAllLevel") ? */MegaManGANUtil.MEGA_MAN_ALL_TERRAIN /*: MegaManGANUtil.MEGA_MAN_FIRST_LEVEL_ALL_TILES*/,
						GANProcess.MEGA_MAN_OUT_WIDTH, GANProcess.MEGA_MAN_OUT_HEIGHT);
				ganProcessVertical.start();
				ganProcessHorizontal.start();
				String response = "";
				while(!response.equals("READY")) {
					response = ganProcessVertical.commRecv();
					response = ganProcessHorizontal.commRecv();
				}
//				while(!response.equals("READY")) {
//					
//				}
//				ganProcessVertical.initBuffers();
//				ganProcessHorizontal.initBuffers();
				}else {
//					System.out.println("wait");
//					System.out.println(ganProcessHorizontal);
//					MiscUtil.waitForReadStringAndEnterKeyPress();
					ganProcessHorizontal.terminate();
					ganProcessVertical.terminate();
					GANProcess.getGANProcess();
				}
				resetButtons(true);
			}
		});
		effectsCheckboxes.add(useBothGANs);
		top.add(effectsCheckboxes);
		
		
		JPanel AStarBudget = new JPanel();
		JLabel AStarLabel = new JLabel("UpdateAStarBudget");
		JTextField updateAStarBudget = new JTextField(10);
		updateAStarBudget.setText(String.valueOf(Parameters.parameters.integerParameter("aStarSearchBudget")));
		updateAStarBudget.addKeyListener(new KeyListener() {
			@Override
			public void keyPressed(KeyEvent e) {
				if(e.getKeyCode()==KeyEvent.VK_ENTER) {
					String budget = updateAStarBudget.getText();
					if(!budget.matches("\\d+")) {
						return;
					}
					int value = Integer.parseInt(budget);
					Parameters.parameters.setInteger("aStarSearchBudget", value);
					resetButtons(true);
				}
			}
			@Override
			public void keyReleased(KeyEvent e) {}
			@Override
			public void keyTyped(KeyEvent e) {}
		});
		AStarBudget.add(AStarLabel);
		AStarBudget.add(updateAStarBudget);
		top.add(AStarBudget);
	
	
	}

	@Override
	public void configureGAN() { //sets GAN to megaman
		GANProcess.type = GANProcess.GAN_TYPE.MEGA_MAN;
		
	}

	@Override
	public String getGANModelParameterName() { 
		// TODO Auto-generated method stub
		return "MegaManGANModel";
	}

	@Override
	public List<List<Integer>> levelListRepresentation(double[] latentVector) { //gets the horizontal stretch list<list<integer>> representation
		// TODO Auto-generated method stub
		return MegaManGANUtil.generateOneLevelListRepresentationFromGAN(latentVector);
	}

	@Override
	public Pair<Integer, Integer> resetAndReLaunchGAN(String model) {
		return staticResetAndReLaunchGAN(model);
	}
	
	public static Pair<Integer, Integer> staticResetAndReLaunchGAN(String model) {
		int megaManGANLevelChunks = Parameters.parameters.integerParameter("megaManGANLevelChunks");
		int oldLength = megaManGANLevelChunks * GANProcess.latentVectorLength();
		Parameters.parameters.setInteger("GANInputSize", 5); // Default latent vector size
		
		GANProcess.terminateGANProcess();
		// Because Python process was terminated, latentVectorLength will reinitialize with the new params
		int newLength = megaManGANLevelChunks * GANProcess.latentVectorLength(); // new model
		return new Pair<>(oldLength,newLength);
	}
	@Override
	public String getGANModelDirectory() {
		return "src"+File.separator+"main"+File.separator+"python"+File.separator+"GAN"+File.separator+"MegaManGAN";
	}

	/**
	 * saves the level in the directory of MegaManMaker levels
	 */
	public void saveLevel() {
		File mmlvFilePath = new File("MegaManMakerLevelPath.txt"); //file containing the path
		//System.out.println(mmlvFile);
//		if(!mmlvFile.exists()) {
//			throw new FileNotFoundException();
//		}
		
		Scanner scan;
		//When the button is pushed, ask for the name input
		try {
			scan = new Scanner(mmlvFilePath);
			//scan.next();
			String mmlvPath = scan.nextLine();
			String mmlvFileName = JOptionPane.showInputDialog(null, "What do you want to name your level?");
			File mmlvFileFromEvolution = new File(mmlvPath+mmlvFileName+".mmlv");
			File mmlvFile;
			scan.close();
			if(selectedItems.size() != 1) {
				JOptionPane.showMessageDialog(null, "Select exactly one level to save.");
				return; // Nothing to explore
			}

			ArrayList<Double> phenotype = scores.get(selectedItems.get(selectedItems.size() - 1)).individual.getPhenotype();
			double[] doubleArray = ArrayUtil.doubleArrayFromList(phenotype);
			//List<List<Integer>> level;
			generateLevelFromGAN(doubleArray);
			int levelNumber = 2020;
			mmlvFile = MegaManVGLCUtil.convertMegaManLevelToMMLV(level, levelNumber);
			try {
				Files.copy(mmlvFile, mmlvFileFromEvolution);
				mmlvFile.delete();
				JOptionPane.showMessageDialog(frame, "Level saved");

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			//System.out.println(mmlvPath);
			
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
			String errorMessage = "You need to create a local text file in the MMNEAT directory called \n MegaManMakerLevelPath.txt which contains the path to where MegaManMaker stores levels on your device. \n It will likely look like this: C:\\Users\\[Insert User Name]\\AppData\\Local\\MegaMaker\\Levels\\";
			JOptionPane.showMessageDialog(frame, errorMessage);
		}
		
		
	}
	@Override
	//Will eventually launch megamanmaker
	public void playLevel(ArrayList<Double> phenotype) {
		double[] doubleArray = ArrayUtil.doubleArrayFromList(phenotype);
		List<List<Integer>> level = levelListRepresentation(doubleArray);
		int levelNumber = 2020;
		MegaManVGLCUtil.convertMegaManLevelToMMLV(level, levelNumber);
		
		//save level and play
	}

	@Override
	protected String getWindowTitle() {
		// TODO Auto-generated method stub
		return "MegaManGANLevelBreeder";
	}

	@Override
	/**
	 * Determines whether or not to allow vertical or horizontal stretching based on the GAN model
	 */
	protected BufferedImage getButtonImage(ArrayList<Double> phenotype, int width, int height,
			double[] inputMultipliers) {
		double[] doubleArray = ArrayUtil.doubleArrayFromList(phenotype);
		//List<List<Integer>> level;
		generateLevelFromGAN(doubleArray);
		//MegaManVGLCUtil.printLevel(level);
		BufferedImage[] images;
		//sets the height and width for the rendered level to be placed on the button 
		int width1 = MegaManRenderUtil.renderedImageWidth(level.get(0).size());
		int height1 = MegaManRenderUtil.renderedImageHeight(level.size());
		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		if(Parameters.parameters.booleanParameter("interactiveMegaManAStarPaths")) {
			MegaManState start = new MegaManState(level);
			Search<MegaManAction,MegaManState> search = new AStarSearch<>(MegaManState.manhattanToOrb);
			HashSet<MegaManState> mostRecentVisited = null;
			ArrayList<MegaManAction> actionSequence = null;
			try {
				//tries to find a solution path to solve the level, tries as many time as specified by the last int parameter 
				//represented by red x's in the visualization 
				actionSequence = ((AStarSearch<MegaManAction, MegaManState>) search).search(start, true, Parameters.parameters.integerParameter("aStarSearchBudget"));
			} catch(Exception e) {
				System.out.println("failed search");
				e.printStackTrace();
			}
			//get all of the visited states, all of the x's are in this set but the white ones are not part of solution path 
			mostRecentVisited = ((AStarSearch<MegaManAction, MegaManState>) search).getVisited();
			try {
				image = MegaManState.vizualizePath(level,mostRecentVisited,actionSequence,start);
			}catch(IOException e) {
				e.printStackTrace();
	
			}
		}else {
		try {
			//

			images = MegaManRenderUtil.loadImagesForASTAR(MegaManRenderUtil.MEGA_MAN_TILE_PATH); //7 different tiles to display 
			image = MegaManRenderUtil.createBufferedImage(level,width1,height1, images);
		} catch (IOException e) {
			e.printStackTrace();
		}
		}
		return image;
	}

	private void generateLevelFromGAN(double[] doubleArray) {
		if(Parameters.parameters.stringParameter("MegaManGANModel").startsWith("HORIZONTALONLY")) { //if horiontal GAN model
			level = levelListRepresentation(doubleArray);
			placeSpawnAndLevelOrbHorizontal(level);
		}else if(Parameters.parameters.stringParameter("MegaManGANModel").startsWith("VERTICALONLY")){ //if vertical GAN model
			level = MegaManGANUtil.generateOneLevelListRepresentationFromGANVertical(doubleArray);
			placeSpawnAndLevelOrbVertical(level);
		}else if (Parameters.parameters.booleanParameter("useBothGANsMegaMan")){
			//System.out.println(ganProcessHorizontal);
			//System.out.println(ganProcessVertical);
			//for(double i : doubleArray)System.out.println(i+", ");
			level = MegaManGANUtil.generateOneLevelListRepresentationFromGANVerticalAndHorizontal(ganProcessHorizontal,ganProcessVertical,doubleArray);
			placeSpawnAndLevelOrbHorizontal(level);
		}else {
			level = MegaManGANUtil.generateOneLevelListRepresentationFromGANVerticalAndHorizontal(GANProcess.getGANProcess(), GANProcess.getGANProcess(), doubleArray);
			placeSpawnAndLevelOrbHorizontal(level);			
		}
	}

	
	private void placeSpawnAndLevelOrbVertical(List<List<Integer>> level) {
		boolean placed = false;
		for(int x = 0;x<level.get(0).size();x++) {
			for(int y = level.size()-1;y>=0;y--) {
				if(y-2>=0&&level.get(y).get(x)==1&&level.get(y-1).get(x)==0&&level.get(y-2).get(x)==0) {
					level.get(y-1).set(x, 8);
					placed = true;
					break;
				}
			}
			if(placed) {
				break;
			}
			
		
		}
		for(int i = 0; i<level.get(0).size();i++) {
			if(!placed) {
				level.get(level.size()-1).set(0, 1);
				level.get(level.size()-2).set(0, 8);
				placed = true;
			}
		}
		placed = false;
		for(int y = 0; y<level.size();y++) {
			for(int x = level.get(0).size()-1;x>=0; x--) {
				if(y-1>=0&&level.get(y).get(x)==2&&level.get(y-1).get(x)==0) {
					level.get(y-1).set(x, 7);
					placed=true;
					break;
					
				}else if(y-1>=0&&level.get(y).get(x)==1&&level.get(y-1).get(x)==0) {
					level.get(y-1).set(x, 7);
					placed=true;
					break;
				}
			}
			if(placed) break;
		}
	}

	private void placeSpawnAndLevelOrbHorizontal(List<List<Integer>> level) { //7 orb 8 spawn
		//int prevY = 0;
		boolean rtrn = false;
		for(int x = 0;x<level.get(0).size();x++) {
			for(int y = 0;y<level.size();y++) {
				if(y-2>=0&&level.get(y).get(x)==1&&level.get(y-1).get(x)==0&&level.get(y-2).get(x)==0) {
					level.get(y-1).set(x, 8);
					rtrn  = true;
					break;
				}
			}
			if(rtrn) {
				rtrn = false;
				break;
			}
		}
		
		
		for(int x = level.get(0).size()-1;x>=0; x--) {
			for(int y = 0; y<level.size();y++) {
				if(y-1>=0&&level.get(y).get(x)==1&&level.get(y-1).get(x)==0) {
					level.get(y-1).set(x, 7);
					rtrn = true;
					break;
				}
			}
			if(rtrn) break;
		}
	}

	/**
	 * Launches the level breeder, sets GAN input size to 5
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			MMNEAT.main(new String[]{"runNumber:0","randomSeed:1","bigInteractiveButtons:false","MegaManGANModel:BOTHVERTICALANDHORIZONTALMegaManAllLevelsBut7With7Tiles_5_Epoch4091.pth","GANInputSize:"+MegaManGANUtil.LATENT_VECTOR_SIZE,"showKLOptions:false","trials:1","mu:16","maxGens:500","io:false","netio:false","mating:true","fs:false","task:megaManMaker.MegaManGANLevelBreederTask","watch:true","cleanFrequency:-1","genotype:edu.southwestern.evolution.genotypes.BoundedRealValuedGenotype","simplifiedInteractiveInterface:false","saveAllChampions:true","ea:edu.southwestern.evolution.selectiveBreeding.SelectiveBreedingEA","imageWidth:2000","imageHeight:2000","imageSize:200"});
		} catch (FileNotFoundException | NoSuchMethodException e) {
			e.printStackTrace();
		}
	}

}
