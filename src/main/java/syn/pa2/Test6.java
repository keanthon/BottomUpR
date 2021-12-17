package syn.pa2;

import org.rosuda.REngine.Rserve.RserveException;

import syn.base.AST;
import syn.base.CFG;
import syn.base.Dataframe;
import syn.pa1.Test2;

public class Test6 extends Test2 {

  public static void main(String[] args) throws RserveException {
    new Test6().test();
  }

  protected void test() throws RserveException {
    CFG cfg = mkCFG();
    Synthesizer2 syn = new Synthesizer2(cfg);
    Dataframe inEx = mkInEx();
    Dataframe outEx = mkOutEx();
    runSynthesizer(syn, inEx, outEx);
  }

  protected void runSynthesizer(Synthesizer2 syn, Dataframe inEx, Dataframe outEx) throws RserveException {
    System.out.println("Synthesizing...\n");
    AST ast = syn.run(inEx, outEx);
    if (ast == null) {
      System.out.println("Synthesis failed" + "\n");
    } else {
      System.out.println("Synthesized program: \n" + ast.toR() + "\n");
    }
    System.out.println("Iterations: " + syn.iterCounter);
    System.out.println("Attempts: " + syn.attmptCounter);
    System.out.println("Pruned: " + syn.prunedCounter);
    System.out.println("Synthesis time: " + syn.runTime + " seconds");
  }

}
