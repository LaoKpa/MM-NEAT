cd ..
cd ..
java -jar target/MM-NEAT-0.0.1-SNAPSHOT.jar runNumber:%1 randomSeed:%1 base:mario trials:10 maxGens:500 mu:100 io:true netio:true mating:true task:edu.utexas.cs.nn.tasks.mario.MarioTask cleanOldNetworks:true fs:false log:Mario-Control saveTo:Control watch:false marioInputStartX:-3 marioInputStartY:-2 marioInputWidth:12 marioInputHeight:5 showMarioInputs:false