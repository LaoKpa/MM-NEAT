cd ..
cd ..
java -jar target/MM-NEAT-0.0.1-SNAPSHOT.jar runNumber:$1 randomSeed:$1 zeldaDungeonDistanceFitness:false zeldaDungeonFewRoomFitness:false zeldaDungeonTraversedRoomFitness:false zeldaPercentDungeonTraversedRoomFitness:false zeldaDungeonRandomFitness:true watch:false trials:1 mu:100 makeZeldaLevelsPlayable:false base:zeldagan log:ZeldaGAN-Random saveTo:Random zeldaGANLevelWidthChunks:10 zeldaGANLevelHeightChunks:10 zeldaGANModel:ZeldaDungeonsAll3Tiles_10000_10.pth maxGens:500 io:true netio:true GANInputSize:10 mating:true fs:false task:edu.southwestern.tasks.zelda.ZeldaGANDungeonTask cleanOldNetworks:false zeldaGANUsesOriginalEncoding:false cleanFrequency:-1 saveAllChampions:true genotype:edu.southwestern.evolution.genotypes.BoundedRealValuedGenotype