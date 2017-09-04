/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package micro.ai.ahtn;

import micro.ai.HasEvaluationFunction;
import micro.ai.abstraction.WorkerRush;
import java.util.LinkedList;
import java.util.List;

import micro.rts.GameState;
import micro.rts.PlayerAction;
import micro.rts.UnitAction;
import micro.rts.units.Unit;
import micro.util.Pair;
import micro.ai.ahtn.domain.DomainDefinition;
import micro.ai.ahtn.domain.MethodDecomposition;
import micro.ai.ahtn.domain.PredefinedOperators;
import micro.ai.ahtn.domain.Term;
import micro.ai.ahtn.planner.AdversarialBoundedDepthPlannerAlphaBeta;
import micro.ai.core.AI;
import micro.ai.core.AIWithComputationBudget;
import micro.ai.core.ParameterSpecification;
import micro.ai.evaluation.EvaluationFunction;
import micro.ai.evaluation.SimpleSqrtEvaluationFunction3;
import java.util.ArrayList;
import micro.rts.units.UnitTypeTable;
import edu.utexas.cs.nn.MMNEAT.MMNEAT;
import edu.utexas.cs.nn.tasks.microrts.*;
/**
 *
 * @author santi
 * strange and alien agent that parses code written in LISP
 */
public class AHTNAI extends AIWithComputationBudget implements HasEvaluationFunction{
    public static int DEBUG = 0;
    
    String domainFileName = null;
    DomainDefinition dd = null;

    EvaluationFunction ef = null;
    AI playoutAI = null;
    public int PLAYOUT_LOOKAHEAD = 100;
        
    List<MethodDecomposition> actionsBeingExecuted = null;
    
    public AHTNAI() {
        this("data/microRTS/ahtn/microrts-ahtn-definition-flexible-single-target-portfolio.lisp", 
             100, -1, 100, 
             new SimpleSqrtEvaluationFunction3(), 
             new WorkerRush(((MicroRTSTask) MMNEAT.task).getUnitTypeTable()));
    }
    
    public AHTNAI(UnitTypeTable utt) throws Exception {
        this("data/microRTS/ahtn/microrts-ahtn-definition-flexible-single-target-portfolio.lisp", 
             100, -1, 100, 
             new SimpleSqrtEvaluationFunction3(), 
             new WorkerRush(utt));
    }
    
    
    public AHTNAI(String a_domainFileName, int available_time, int max_playouts, int playoutLookahead, EvaluationFunction a_ef, AI a_playoutAI) {
        super(available_time, max_playouts);
        domainFileName = a_domainFileName;
        try {
			dd = DomainDefinition.fromLispFile(domainFileName);
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("AHTNAI: Error looking for lisp file.");
			//if this throws an error too often we may have to abandon AHTNAI -Alice
			System.exit(1);
		}
        PLAYOUT_LOOKAHEAD = playoutLookahead;
        ef = a_ef;
        playoutAI = a_playoutAI;
       
        actionsBeingExecuted = new LinkedList<>();
    }
    
    
    public void reset() {
        actionsBeingExecuted = new LinkedList<>();       
        AdversarialBoundedDepthPlannerAlphaBeta.clearStatistics();
    }

    
    public PlayerAction getAction(int player, GameState gs) throws Exception {
        Term goal1 = Term.fromString("(destroy-player "+player+" "+(1-player)+")");
        Term goal2 = Term.fromString("(destroy-player "+(1-player)+" "+player+")");
        if (gs.canExecuteAnyAction(player)) {
            Pair<MethodDecomposition,MethodDecomposition> plan = AdversarialBoundedDepthPlannerAlphaBeta.getBestPlanIterativeDeepening(goal1, goal2, player, TIME_BUDGET, ITERATIONS_BUDGET, PLAYOUT_LOOKAHEAD, gs, dd, ef, playoutAI);
            PlayerAction pa = new PlayerAction();
            if (plan!=null) {
                MethodDecomposition toExecute = plan.m_a;
                List<Pair<Integer,List<Term>>> l = (toExecute!=null ? toExecute.convertToOperatorList():new LinkedList<>());

                if (DEBUG>=1) {
                    List<Pair<Integer,List<Term>>> l2 = (plan.m_b!=null ? plan.m_b.convertToOperatorList():new LinkedList<>());        
                    System.out.println("---- ---- ---- ----");
                    System.out.println(gs);
                    System.out.println("Max plan:");
                    for(Pair<Integer, List<Term>> a:l) System.out.println("  " + a.m_a + ": " + a.m_b);
                    System.out.println("Min plan:");
                    for(Pair<Integer, List<Term>> a:l2) System.out.println("  " + a.m_a + ": " + a.m_b);
                }
                if (DEBUG>=2) {
                    System.out.println("Detailed Max plan:");
                    plan.m_a.printDetailed();                    
                }
                
                actionsBeingExecuted.clear();
                while(!l.isEmpty()) {
                    Pair<Integer,List<Term>> tmp = l.remove(0);
                    if (tmp.m_a!=gs.getTime()) break;
                    List<Term> actions = tmp.m_b;
                    for(Term action:actions) {
                        MethodDecomposition md = new MethodDecomposition(action);
                        actionsBeingExecuted.add(md);
                    }
                }
            }

            if (DEBUG>=1) 
            {
                System.out.println("Actions being executed:");
                for(MethodDecomposition md:actionsBeingExecuted) {
                    System.out.println("    " + md.getTerm());
                }
            }

            List<MethodDecomposition> toDelete = new LinkedList<>();
            for(MethodDecomposition md:actionsBeingExecuted) {
                if (PredefinedOperators.execute(md, gs, pa)) toDelete.add(md);
                
                for(Pair<Unit,UnitAction> ua:pa.getActions()) {
                    if (gs.getUnit(ua.m_a.getID())==null) {
                        pa.removeUnitAction(ua.m_a, ua.m_b);
                    }
                }
            }
            actionsBeingExecuted.removeAll(toDelete);

            if (DEBUG>=1) {
                System.out.println("Result in the following unit actions:");
                System.out.println("    " + pa);
            }
            
            pa.fillWithNones(gs, player, 10);
            return pa;
        } else {
            return new PlayerAction();        
        }
    }

    
    @Override
    public AI clone() {
        try {
            return new AHTNAI(domainFileName, TIME_BUDGET, ITERATIONS_BUDGET, PLAYOUT_LOOKAHEAD, ef, playoutAI);
        }catch(Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    @Override
    public String toString() {
        return getClass().getSimpleName() + "(" + domainFileName + ", " + TIME_BUDGET + ", " + ITERATIONS_BUDGET + ", " + PLAYOUT_LOOKAHEAD + ", " + ef + ", " + playoutAI + ")";
    }
    
    public String statisticsString() {
        return "Max depth: " + AdversarialBoundedDepthPlannerAlphaBeta.max_iterative_deepening_depth +
               ", Average depth: " + (AdversarialBoundedDepthPlannerAlphaBeta.average_iterative_deepening_depth/AdversarialBoundedDepthPlannerAlphaBeta.n_iterative_deepening_runs) +
               ", Max tree leaves: " + AdversarialBoundedDepthPlannerAlphaBeta.max_tree_leaves + 
               ", Average tree leaves: " + (AdversarialBoundedDepthPlannerAlphaBeta.average_tree_leaves/AdversarialBoundedDepthPlannerAlphaBeta.n_trees) +
               ", Max tree nodes: " + AdversarialBoundedDepthPlannerAlphaBeta.max_tree_nodes + 
               ", Average tree nodes: " + (AdversarialBoundedDepthPlannerAlphaBeta.average_tree_nodes/AdversarialBoundedDepthPlannerAlphaBeta.n_trees) +
               ", Max tree depth: " + AdversarialBoundedDepthPlannerAlphaBeta.max_tree_depth + 
               ", Average tree depth: " + (AdversarialBoundedDepthPlannerAlphaBeta.average_tree_depth/AdversarialBoundedDepthPlannerAlphaBeta.n_trees) +
               ", Max time depth: " + AdversarialBoundedDepthPlannerAlphaBeta.max_time_depth + 
               ", Average time depth: " + (AdversarialBoundedDepthPlannerAlphaBeta.average_time_depth/AdversarialBoundedDepthPlannerAlphaBeta.n_trees);
    }
    
    
    @Override
    public List<ParameterSpecification> getParameters()
    {
        List<ParameterSpecification> parameters = new ArrayList<>();
        
        parameters.add(new ParameterSpecification("DomainFileName",String.class,"data/microRTS/ahtn/microrts-ahtn-definition-flexible-single-target-portfolio.lisp"));
        parameters.add(new ParameterSpecification("TimeBudget",int.class,100));
        parameters.add(new ParameterSpecification("IterationsBudget",int.class,-1));
        parameters.add(new ParameterSpecification("PlayoutLookahead",int.class,100));
        parameters.add(new ParameterSpecification("PlayoutAI",AI.class, playoutAI));
        parameters.add(new ParameterSpecification("EvaluationFunction", EvaluationFunction.class, new SimpleSqrtEvaluationFunction3()));
        
        return parameters;
    }
    
    
    public String getDomainFileName()
    {
        return domainFileName;
    }
    
    
    public void setDomainFileName(String a_domainFileName) throws Exception {
        domainFileName = a_domainFileName;
        dd = DomainDefinition.fromLispFile(domainFileName);
    }    
    
    
    public int getPlayoutLookahead() 
    {
        return PLAYOUT_LOOKAHEAD;
    }
    
    
    public void setPlayoutLookahead(int a_pla) {
        PLAYOUT_LOOKAHEAD = a_pla;
    }
    
    
    public AI getPlayoutAI()
    {
        return playoutAI;
    }
    
    
    public void setPlayoutAI(AI a_poAI) {
        playoutAI = a_poAI;
    }


    public EvaluationFunction getEvaluationFunction()
    {
        return ef;
    }
    
    
    public void setEvaluationFunction(EvaluationFunction a_ef) {
        ef = a_ef;
    }
}
