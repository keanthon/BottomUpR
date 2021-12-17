package syn.pa3;

import org.rosuda.REngine.Rserve.RserveException;

import syn.base.CFG;
import syn.base.Dataframe;
import syn.pa2.Test7;

public class Test9 extends Test7 {

  public static void main(String[] args) throws RserveException {
    new Test9().test();
  }

  protected void test() throws RserveException {
    CFG cfg = mkCFG();
    Synthesizer3 syn = new Synthesizer3(cfg);
    Dataframe inEx = mkInEx();
    Dataframe outEx = mkOutEx();
    runSynthesizer(syn, inEx, outEx);
  }

}
