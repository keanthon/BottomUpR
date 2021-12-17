package syn.project;

import java.util.HashMap;
import java.util.Map;

import org.rosuda.REngine.Rserve.RserveException;

import syn.base.CFG;
import syn.base.Dataframe;
import syn.base.Production;

public class Test10TF extends Test2TF {

  public static void main(String[] args) throws RserveException {
    new Test10TF().test();
  }

  @Override
  protected Dataframe mkInEx() {
    Dataframe inEx = Dataframe.mkDataframe(new String[] { "var", "val", "round", "nam", },
        new Object[][] { { 22, 11, 22, 11, }, { 0.1, 0.2, 0.5, 0.9, }, { "round1", "round2", "round1", "round2", },
            { "foo", "foo", "bar", "bar", }, });
    System.out.println("INPUT EXAMPLE: \n" + inEx.toR() + "\n");
    return inEx;
  }

  @Override
  protected Dataframe mkOutEx() {
    Dataframe outEx = Dataframe.mkDataframe(
        new String[] { "tmp1", "tmp2", },
        new Object[][] { {"nam","nam","nam","nam","tmp1","tmp1","tmp1","tmp1"},
        {"foo","foo","bar","bar","round1_22","round2_11","round1_22","round2_11"} });
    System.out.println("OUTPUT EXAMPLE: \n" + outEx.toR() + "\n");
    return outEx;
  }

  @Override
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
        // newColName ::= "tmp3"
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
