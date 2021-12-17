package syn.project;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.rosuda.REngine.Rserve.RserveException;

import syn.base.CFG;
import syn.base.Dataframe;
import syn.base.Production;
import syn.project.SynthesizerTF;
import java.util.Random;

import javax.xml.crypto.Data;

public class RandomTestMaker {
  ArrayList<Dataframe> inputs = new ArrayList<Dataframe>();
  private SynthesizerTF syn;
  private CFG cfg;
  private Random rand;
  private int depth;
  public Dataframe output;
  public String x_prog="";

  public RandomTestMaker(int depth) throws RserveException {
    this.cfg = mkCFG();
    this.syn = new SynthesizerTF(cfg);
    this.rand = new Random();
    this.depth = depth;
    mkInputs();
  }
  public static void main(String[] args) throws RserveException {
    new RandomTestMaker(3).run();
  }

  public void run() throws RserveException {
    
    Dataframe input = inputs.get(4);
    Dataframe out;
    String curString;
    
    do {
      Production[] productions = cfg.getProductions("df");
      int prev = -1;
      int rnd = -1;
      curString = "x";
      

      for (int i=0; i<depth; i++) {
        
        while(prev==rnd) {
          rnd = rand.nextInt(productions.length-1) +1;
        }
        prev = rnd;
        int rd = new Random().nextInt(syn.combinationsList.get(rnd-1).size());

        Object op = productions[rnd].getOperator();
        StringBuilder sb_x = new StringBuilder();
        sb_x.append(op.toString());
        sb_x.append("(x");

        for (String comb : syn.combinationsList.get(rnd-1).get(rd)) {
          sb_x.append(",");
          sb_x.append(comb);
        }
        sb_x.append(")");
        String next = sb_x.toString();
        curString = curString.replaceFirst("(?:x)+", next);
      }

      // System.out.println(curString);
      // System.out.println(input);
      out = syn.interp.eval(curString, input);
      // System.out.println(out);

      
    }
    while(out==null || out.containNAs());
    this.output = out;
    this.x_prog = curString;
    System.out.println("Input:");
    System.out.println(input);
    System.out.println();
    System.out.println("Output:");
    System.out.println(this.output);
    System.out.println();
    System.out.println("Program:");
    System.out.println(curString);
  }
 
  public void mkInputs() {
    inputs.add(
        Dataframe.mkDataframe(new String[] { "a", "b", "c" },
    new Object[][] { { "r1", "r2", "r3", }, { 22, 11, 22, }, { 33, 44, 33, }, })
    );
    inputs.add(
        Dataframe.mkDataframe(new String[] { "var", "val", "round", "nam", },
    new Object[][] { { 22, 11, 22, 11, }, { 0.1, 0.2, 0.5, 0.9, }, { "round1", "round2", "round1", "round2", },
        { "foo", "foo", "bar", "bar", }, })
    );

    inputs.add(
        Dataframe.mkDataframe(new String[] {"a", "b",}, new Object[][] {{"r1", "r2",}, {"r3", "r4",},})
    );
    inputs.add(
        Dataframe.mkDataframe(new String[] {"a", "b", "c"}, new Object[][] {{"r1", "r2", "r3"}, {"r4", "r5", "r6"},{1,2,3},})
    );
    inputs.add(
        Dataframe.mkDataframe(new String[]{"x","y",}, new Object[][] {{"r1", "r2", "r3"}, {"r3", "r4","r4"},})
    );
    


    return;
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
        // df ::= spread(df, oldColNum, oldColNum)
        new Production("df", "spread", new String[] { "df", "oldColNum", "oldColNum" }),
        // df ::= select(df, oldColNum, oldColNum)
        new Production("df", "slice", new String[] { "df", "oldRowNum", "oldRowNum" }),
        // df ::= select(df, oldColNum, oldColNum, oldColNum)

    });
    symbolToProductions.put("newColName", new Production[] {

        // newColName ::= "tmp1"
        new Production("newColName", "tmp1", new String[0]),
        // newColName ::= "tmp2"
        new Production("newColName", "tmp2", new String[0]),
    });

    symbolToProductions.put("oldColNum", new Production[] {

        // oldColNum ::= 1
        new Production("oldColNum", 1, new String[0]),
        // oldColNum ::= 2
        new Production("oldColNum", 2, new String[0]),
        // oldColNum ::= 3
        new Production("oldColNum", 3, new String[0]),

    });

    symbolToProductions.put("oldRowNum", new Production[] {

      // oldColNum ::= 1
      new Production("oldRowNum", 1, new String[0]),
      // oldColNum ::= 2
      new Production("oldRowNum", 2, new String[0]),
      // oldColNum ::= 3
      new Production("oldRowNum", 3, new String[0]),

    });

    return new CFG(symbolToProductions, "df");
  }

}
