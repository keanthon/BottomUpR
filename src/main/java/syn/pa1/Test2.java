package syn.pa1;

import java.util.HashMap;
import java.util.Map;

import org.rosuda.REngine.Rserve.RserveException;

import syn.base.AST;
import syn.base.CFG;
import syn.base.Dataframe;
import syn.base.Production;

public class Test2 {

  public static void main(String[] args) throws RserveException {
    new Test2().test();
  }

  protected void test() throws RserveException {
    CFG cfg = mkCFG();
    Synthesizer1 syn = new Synthesizer1(cfg);
    Dataframe inEx = mkInEx();
    Dataframe outEx = mkOutEx();
    runSynthesizer(syn, inEx, outEx);
  }

  protected void runSynthesizer(Synthesizer1 syn, Dataframe inEx, Dataframe outEx) throws RserveException {
    System.out.println("Synthesizing...\n");
    AST ast = syn.run(inEx, outEx);
    if (ast == null) {
      System.out.println("Synthesis failed" + "\n");
    } else {
      System.out.println("Synthesized program: \n" + ast.toR() + "\n");
    }
    System.out.println("Iterations: " + syn.iterCounter);
    System.out.println("Synthesis time: " + syn.runTime + " milliseconds");
  }

  protected Dataframe mkInEx() {
    Dataframe inEx = Dataframe.mkDataframe(new String[] { "a", "b", "c" },
        new Object[][] { { "r1", "r2", "r3", }, { 22, 11, 22, }, { 33, 44, 33, }, });
    System.out.println("INPUT EXAMPLE: \n" + inEx.toR() + "\n");
    return inEx;
  }

  protected Dataframe mkOutEx() {
    Dataframe outEx = Dataframe.mkDataframe(new String[] { "tmp2", "tmp3", }, new Object[][] {
        { "tmp1", "tmp1", "tmp1", "c", "c", "c", }, { "r1_22", "r2_11", "r3_22", "33", "44", "33" }, });
    System.out.println("OUTPUT EXAMPLE: \n" + outEx.toR() + "\n");
    return outEx;
  }

  protected CFG mkCFG() {

    Map<String, Production[]> symbolToProductions = new HashMap<>();

    symbolToProductions.put("df", new Production[] {

        // df ::= x
        new Production("df", "x", new String[0]),
        // df ::= gather(df, newColName, newColName, oldColNum, oldColNum)
        new Production("df", "gather", new String[] { "df", "newColName", "newColName", "oldColNum", "oldColNum" }),
        // df ::= unite(df, newColName, oldColNum, oldColNum)
        new Production("df", "unite", new String[] { "df", "newColName", "oldColNum", "oldColNum" }),

    });
    symbolToProductions.put("newColName", new Production[] {

        // newColName ::= "tmp1"
        new Production("newColName", "tmp1", new String[0]),
        // newColName ::= "tmp2"
        new Production("newColName", "tmp2", new String[0]),
        // newColName ::= "tmp3"
        new Production("newColName", "tmp3", new String[0]),

    });

    symbolToProductions.put("oldColNum", new Production[] {

        // oldColNum ::= 1
        new Production("oldColNum", 1, new String[0]),
        // oldColNum ::= 2
        new Production("oldColNum", 2, new String[0]),

    });

    return new CFG(symbolToProductions, "df");
  }

}
