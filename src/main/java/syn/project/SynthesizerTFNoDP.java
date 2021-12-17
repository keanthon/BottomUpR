package syn.project;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.xml.crypto.Data;

import com.microsoft.z3.BoolExpr;
import com.microsoft.z3.Context;
import com.microsoft.z3.IntExpr;
import com.microsoft.z3.Solver;
import com.microsoft.z3.Status;

import org.rosuda.REngine.Rserve.RserveException;

import syn.base.AST;
import syn.base.CFG;
import syn.base.Dataframe;
import syn.base.Production;
import syn.project.SavedValue;
import syn.base.Interpreter;

//bottom up approach to synthesize a r program that matches inEx and outEx
public class SynthesizerTFNoDP {
    // the synthesizer will need an interpreter to evaluate programs
    public Interpreter interp;

    // this context-free grammar defines the search space
    public CFG cfg;

    private Map<Integer,List<SavedValue>> saved = new HashMap<Integer,List<SavedValue>>();
    public List<ArrayList<String[]>> combinationsList = new ArrayList<ArrayList<String[]>>();
    private Map<Object,Integer> operatorWeight = new HashMap<Object,Integer>();
    private int penalty = 5;
    public int runTime = 0;
    public int weightsearched = 0;

    public SynthesizerTFNoDP(CFG cfg) throws RserveException {
        this.interp = new Interpreter();
        this.cfg = cfg;

        // populate saved values at each iteration
        Production[] productions = cfg.getProductions("df");
        for (int i=1; i< productions.length; i++) {

          
            ArrayList<String[]> combinations = new ArrayList<String[]>();
            combinations.add(new String[0]);

            String [] symbols = productions[i].getArgumentSymbols();
            for (int j=1; j< symbols.length; j++) {
                String[] tmp = cfg.getSymbolValues(symbols[j]);
                combinations = crossProduct(tmp, combinations);
            }

            combinationsList.add(combinations);

            // initialize all weights to 1
          
            operatorWeight.put(productions[i].getOperator(), 1);
            
        

        }
        

    }

    public String runTF(Dataframe inEx, Dataframe outEx) throws RserveException {
        // start timer
        long start = System.currentTimeMillis();
        Dataframe df = Dataframe.mkDataframe(new String[]{}, new Object[][]{{}});
        // run reweight

        Production[] productions = cfg.getProductions("df");

        for (int W = 1; W < 100; W++) {
            // watch for one indexing error, because the first element is df
            for (int i=1; i<productions.length; i++) {

                
                Object op = productions[i].getOperator();
                int weightRequired = W-operatorWeight.get(op);

                if(W!=1 && !saved.containsKey(weightRequired)) {
                    continue;
                }

                
                // i-1 because we did not store the df combination, only combinations for operators
                for (String[] combination : combinationsList.get(i-1)) {
                    StringBuilder sb_x = new StringBuilder();
                    sb_x.append(op.toString());
                    sb_x.append("(x");
                    for (String comb : combination) {
                        sb_x.append(",");
                        sb_x.append(comb);
                    }
                    sb_x.append(")");

                    String x_prog = sb_x.toString();

                    // first time set weights for functions
                    if(W==1) {
                        Dataframe df_x = interp.eval(x_prog, inEx);
                        int weight = operatorWeight.get(op);
                        if(df_x != null){
                            if(df_x.equals(outEx)){
                                return x_prog;
                            }
                            if(saved.containsKey(weight)){
                                saved.get(W).add(new SavedValue(x_prog, df));
                            }
                            else{
                                saved.put(weight, new ArrayList<SavedValue>());
                                saved.get(weight).add(new SavedValue(x_prog, df));
                            }
                        }

                        continue;
                    }

                    for (SavedValue val: saved.get(weightRequired)) {
                        int weight = W;
                        if(val.rProgram.indexOf(op.toString())==0) {
                            weight+=this.penalty;
                        }
                        String out_prog = x_prog.replaceFirst("(?:x)+", val.rProgram);

                        // System.out.println(out_prog);

                        Dataframe df_x = interp.eval(out_prog, inEx);
                        if(df_x != null){
                            if(df_x.equals(outEx)){
                                runTime = (int) (System.currentTimeMillis() - start);
                                weightsearched = W;
                                return out_prog;
                            }
                            if(saved.containsKey(weight)){
                                saved.get(weight).add(new SavedValue(out_prog, df));
                            }
                            else{
                                saved.put(weight, new ArrayList<SavedValue>());
                                saved.get(weight).add(new SavedValue(out_prog, df));
                            }
                        }
                        
                    }

                }
        
                
            }
        }
    
        return "";
    }
    // hashmap that map total weight to a R program
    // private Map<Integer, String> weightsToR = new HashMap<Integer, String>();
    // private Map<String, Dataframe> rToDF = new HashMap<String, Dataframe>();

    public ArrayList<String[]> crossProduct(String[] strings, ArrayList<String[]> combi) {
        ArrayList<String[]> ret = new ArrayList<String[]>();
        for (String[] comb: combi) {
            for (String str: strings) {
                if (comb.length>0 && comb[comb.length-1].equals(str)) {
                    continue;
                }
                String[] newComb = new String[comb.length + 1];
                for (int i = 0; i < comb.length; i++) {
                    newComb[i] = comb[i];
                }
                newComb[comb.length] = str;
                ret.add(newComb);
            }
        }

        return ret;

    }

   
}
