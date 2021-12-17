package syn.pa2;

import java.util.HashMap;
import java.util.Map;

import org.rosuda.REngine.Rserve.RserveException;

import syn.base.CFG;
import syn.base.Dataframe;
import syn.base.Production;

public class Test7 extends Test6 {

  public static void main(String[] args) throws RserveException {
    new Test7().test();
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
        new String[] { "nam", "val_round1", "val_round2", "var_round1", "var_round2", },
        new Object[][] { { "bar", "foo", }, { 0.5, 0.1 }, { 0.9, 0.2 }, { 22, 22 }, { 11, 11 }, });
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

    return new CFG(symbolToProductions, "df");
  }

}
