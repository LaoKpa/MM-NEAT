package edu.southwestern.tasks.zelda;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import edu.southwestern.MMNEAT.MMNEAT;
import edu.southwestern.evolution.mapelites.BinLabels;
import edu.southwestern.parameters.Parameters;

public class ZeldaMAPElitesDistinctAndBackTrackRoomsBinLabels implements BinLabels {
	
	List<String> labels = null;
		
	@Override
	public List<String> binLabels() {
		if(labels == null) { // Create once and re-use, but wait until after Parameters are loaded
			int maxNumRooms = Parameters.parameters.integerParameter("zeldaGANLevelWidthChunks") * Parameters.parameters.integerParameter("zeldaGANLevelHeightChunks");
			labels = new ArrayList<String>((maxNumRooms+1)*(maxNumRooms+1)*(maxNumRooms+1));
			for(int i = 0; i <= maxNumRooms; i++) { // Wall tile percent
				for(int j = 0; j <= maxNumRooms; j++) { // Water tile percent
					for(int r = 0; r <= maxNumRooms; r++) {
						labels.add("DistinctRooms["+i+"]BackTrackedRooms["+j+"]Rooms"+r);
					}
				}
			}
		}
		return labels;
	}
	//"mapElitesBinLabels:edu.southwestern.tasks.zelda.ZeldaMAPElitesDistinctAndBackTrackRoomsBinLabels"
	public static void main(String[] args) throws FileNotFoundException, NoSuchMethodException{

		MMNEAT.main("runNumber:0 randomSeed:0 zeldaDungeonDistanceFitness:false zeldaDungeonFewRoomFitness:false zeldaDungeonTraversedRoomFitness:true zeldaPercentDungeonTraversedRoomFitness:true zeldaDungeonRandomFitness:false watch:false trials:1 mu:10 makeZeldaLevelsPlayable:false base:zeldacppntogan log:ZeldaCPPNtoGAN-MAPElites saveTo:MAPElites zeldaGANLevelWidthChunks:5 zeldaGANLevelHeightChunks:5 zeldaGANModel:ZeldaDungeonsAll3Tiles_10000_10.pth maxGens:50000 io:true netio:true GANInputSize:10 mating:true fs:false task:edu.southwestern.tasks.zelda.ZeldaCPPNtoGANDungeonTask cleanOldNetworks:false zeldaGANUsesOriginalEncoding:false allowMultipleFunctions:true ftype:0 netChangeActivationRate:0.3 cleanFrequency:-1 recurrency:false saveAllChampions:true includeFullSigmoidFunction:true includeFullGaussFunction:true includeCosineFunction:true includeGaussFunction:false includeIdFunction:true includeTriangleWaveFunction:true includeSquareWaveFunction:true includeFullSawtoothFunction:true includeSigmoidFunction:false ea:edu.southwestern.evolution.mapelites.MAPElites experiment:edu.southwestern.experiment.evolution.SteadyStateExperiment mapElitesBinLabels:edu.southwestern.tasks.zelda.ZeldaMAPElitesDistinctAndBackTrackRoomsBinLabels steadyStateIndividualsPerGeneration:100".split(" "));
		

	}
}
