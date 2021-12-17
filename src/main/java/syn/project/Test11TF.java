package syn.project;

import java.util.HashMap;
import java.util.Map;

import org.rosuda.REngine.Rserve.RserveException;

import syn.base.CFG;
import syn.base.Dataframe;
import syn.base.Production;

public class Test11TF extends Test10TF {

  public static void main(String[] args) throws RserveException {
    new Test11TF().test();
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
        new String[] { "tmp2", "tmp1", },
        new Object[][] { {"tmp2","tmp2","tmp2","tmp2","tmp2","tmp2","tmp2","tmp2","nam","nam","nam","nam","nam","nam","nam","nam",},
        {"round1","round2","round1","round2","0.1_22","0.2_11","0.5_22","0.9_11","foo","foo","bar","bar","foo","foo","bar","bar",} });
    System.out.println("OUTPUT EXAMPLE: \n" + outEx.toR() + "\n");
    return outEx;
  }

  

}
