REM Usage:   postParetoFrontWatch.bat <experiment directory> <log prefix> <run type> <run number> <number of trials per individual>
REM Example: postParetoFrontWatch.bat onelifeconflict OneLifeConflict OneModule 0 5
java -jar "target/MM-NEAT-0.0.1-SNAPSHOT.jar" runNumber:%4 experiment:edu.utexas.cs.nn.experiment.post.LoadAndWatchExperiment base:%1 log:%2-%3 saveTo:%3 trials:%5 watch:true showNetworks:true io:false netio:false onlyWatchPareto:true printFitness:true animateNetwork:false monitorInputs:true
