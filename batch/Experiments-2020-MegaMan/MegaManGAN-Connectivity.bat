cd ..
cd ..
java -jar target/MM-NEAT-0.0.1-SNAPSHOT.jar runNumber:%1 randomSeed:%1 base:megamanlevels log:MegaManLevels-AStarDist saveTo:AStarDist watch:false GANInputSize:10 trials:1 mu:100 maxGens:500 io:true netio:true genotype:edu.southwestern.evolution.genotypes.BoundedRealValuedGenotype mating:true fs:false task:megaManMaker.MegaManGANLevelTask useThreeGANsMegaMan:true megaManGANLevelChunks:10 megaManAllowsSimpleAStarPath:false megaManAllowsConnectivity:false cleanFrequency:-1 saveAllChampions:true, cleanOldNetworks:false logTWEANNData:false logMutationAndLineage:false steadyStateIndividualsPerGeneration:100 aStarSearchBudget:100000}