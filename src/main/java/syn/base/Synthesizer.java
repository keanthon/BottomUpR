package syn.base;

import org.rosuda.REngine.Rserve.RserveException;

public abstract class Synthesizer {

  // the synthesizer will need an interpreter to evaluate programs
  public Interpreter interp;

  // this context-free grammar defines the search space
  public CFG cfg;
  // this is a bound on the search space
  public int bound = 3;

  public Synthesizer(CFG cfg) throws RserveException {
    this.interp = new Interpreter();
    this.cfg = cfg;
  }

  // given an input-output example, synthesize an ast
  public abstract AST run(Dataframe inEx, Dataframe outEx);

}
