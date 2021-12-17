package syn.base;

import org.rosuda.REngine.REXPMismatchException;
import org.rosuda.REngine.RList;
import org.rosuda.REngine.Rserve.RConnection;
import org.rosuda.REngine.Rserve.RserveException;

public class Interpreter {

  private RConnection con;

  public Interpreter() throws RserveException {
    con = new RConnection();
    con.eval("library(tidyr)");
    con.eval("library(dplyr)");
  }

  // rprog is an R program that has one single variable x
  // input is the dataframe that x binds to
  // if rprog executes successfully, this function returns a dataframe
  // however, rprog may in general be invalid or crash during execution, in that
  // case, this function would return NULL
  // it's also possible this function would throw an exception, in that case, that
  // means there is some issue with the implementation
  public Dataframe eval(String rprog, Dataframe input) {
    try {
      con.eval("x <- " + input.toR());
      RList res = con.eval(rprog).asList();
      Dataframe ret = Dataframe.mkDataframe(res);
      return ret;
    } catch (RserveException e) {
      return null;
    } catch (REXPMismatchException e) {
      throw new RuntimeException();
    }
  }

  // evaluate the given ast on the given input
  public Dataframe eval(AST ast, Dataframe input) {
    return eval(ast.toR(), input);
  }

  // this function evaluates the sub-tree of ast that's rooted at a specific node
  public Dataframe eval(AST ast, ASTNode node, Dataframe input) {
    return eval(ast.toR(node), input);
  }

}
