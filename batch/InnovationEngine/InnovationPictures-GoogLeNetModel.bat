cd ..
cd ..
java -jar target/MM-NEAT-0.0.1-SNAPSHOT.jar runNumber:%1 randomSeed:%1 base:innovation mu:400 maxGens:2000000 io:true netio:true mating:true task:edu.southwestern.tasks.innovationengines.PictureInnovationTask log:InnovationPictures-GoogLeNetModel saveTo:GoogLeNetModel allowMultipleFunctions:true ftype:0 netChangeActivationRate:0.3 cleanFrequency:400 recurrency:false logTWEANNData:false logMutationAndLineage:true ea:edu.southwestern.evolution.mapelites.MAPElites experiment:edu.southwestern.experiment.evolution.SteadyStateExperiment mapElitesBinLabels:edu.southwestern.tasks.innovationengines.ImageNetBinMapping fs:true imageNetModel:edu.southwestern.networks.dl4j.GoogLeNetWrapper pictureInnovationSaveThreshold:1.1 includeSigmoidFunction:true includeTanhFunction:false includeIdFunction:false includeFullApproxFunction:false includeApproxFunction:false includeGaussFunction:true includeSineFunction:true includeSawtoothFunction:true includeAbsValFunction:true includeHalfLinearPiecewiseFunction:true includeStretchedTanhFunction:false includeReLUFunction:false includeSoftplusFunction:false includeLeakyReLUFunction:false includeFullSawtoothFunction:false includeTriangleWaveFunction:false includeSquareWaveFunction:false