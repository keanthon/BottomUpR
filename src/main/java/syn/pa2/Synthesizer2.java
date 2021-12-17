package syn.pa2;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import com.microsoft.z3.BoolExpr;
import com.microsoft.z3.Context;
import com.microsoft.z3.IntExpr;
import com.microsoft.z3.Solver;
import com.microsoft.z3.Status;

import org.rosuda.REngine.Rserve.RserveException;

import syn.base.AST;
import syn.base.ASTNode;
import syn.base.CFG;
import syn.base.Dataframe;
import syn.base.Production;
import syn.pa1.Synthesizer1;

public class Synthesizer2 extends Synthesizer1 {

  public Synthesizer2(CFG cfg) throws RserveException {
    super(cfg);
    createSpecs();
  }

  // this counter counts the number of attempts for pruning
  public int attmptCounter = 0;
  // this counter counts the actual number of pruned (partial) programs
  public int prunedCounter = 0;

  public AST run(Dataframe inEx, Dataframe outEx) {

    long start = System.currentTimeMillis();

    LinkedList<AST> worklist = new LinkedList<>();

    worklist.add(mkInitialAST());

    AST ret = null;

    while (!worklist.isEmpty()) {

      iterCounter++;

      AST ast = worklist.removeFirst();

      ASTNode openNode = selectOpenNode(ast);

      if (openNode == null) {
        Dataframe output = interp.eval(ast, inEx);
        if (outEx.equals(output)) {
          ret = ast;
          break;
        }
      } else {
        for (Production prod : cfg.getProductions(openNode.getSymbol())) {
          AST expanded = ast.expand(openNode, prod);
          if (expanded.numOfOperators() > bound) {
            continue;
          }
          if (attemptToPrune(expanded)) {
            attmptCounter++;
            if (prune(expanded, inEx, outEx)) {
              prunedCounter++;
              continue;
            }
          }
          worklist.add(expanded);
        }
      }
    }

    long end = System.currentTimeMillis();
    runTime = (int) (end - start) / 1000;

    return ret;
  }

  protected Context ctx = new Context();
  // # input columns
  protected IntExpr xin = ctx.mkIntConst("xin");
  // # input rows
  protected IntExpr yin = ctx.mkIntConst("yin");
  // # output columns
  protected IntExpr xout = ctx.mkIntConst("xout");
  // # output rows
  protected IntExpr yout = ctx.mkIntConst("yout");
  protected Map<String, BoolExpr> operatorToSpec = new HashMap<>();

  protected void createSpecs() {
    {
      BoolExpr b1 = ctx.mkLe(xout, xin);
      BoolExpr b2 = ctx.mkGe(yout, yin);
      BoolExpr b = ctx.mkAnd(b1, b2);
      operatorToSpec.put("gather", b);
    }
    {
      BoolExpr b1 = ctx.mkEq(xout, ctx.mkSub(xin, ctx.mkInt(1)));
      BoolExpr b2 = ctx.mkEq(yout, yin);
      BoolExpr b = ctx.mkAnd(b1, b2);
      operatorToSpec.put("unite", b);
    }
    {
      BoolExpr b1 = ctx.mkGe(xout, xin);
      BoolExpr b2 = ctx.mkLe(yout, yin);
      BoolExpr b = ctx.mkAnd(b1, b2);
      operatorToSpec.put("spread", b);
    }
  }

  protected boolean attemptToPrune(AST ast) {
    // add your code here

    ASTNode n=ast.getRoot();
    if (ast.getBottomLeftNode().isHole()) {
      return false;
    }

    while(!ast.isLeaf(n)) {
      ASTNode[] children = ast.getChildren(n);
      n = children[0];
      // if(n.isHole()) return false;

      for(int i=1; i<children.length; i++){
        if(!children[i].isHole()) return false;    
      }

    }

    return true;
  }

  protected boolean prune(AST ast, Dataframe inEx, Dataframe outEx) {
    // add your code here
    int nameCount = 0;
    IntExpr x = ctx.mkIntConst("x"+nameCount);
    IntExpr y = ctx.mkIntConst("y"+nameCount);
    IntExpr xprev = x;
    IntExpr yprev = y;
    ASTNode node = ast.getBottomLeftNode();
    BoolExpr b1 = ctx.mkEq(x, ctx.mkInt(inEx.numOfCols()));
    BoolExpr b2 = ctx.mkEq(y, ctx.mkInt(inEx.numOfRows()));
    BoolExpr constraint = ctx.mkAnd(b1, b2);
  

    while(node!=ast.getRoot()) {
      node = ast.getParent(node);
      nameCount++;
      xprev = x;
      yprev = y;
      x = ctx.mkIntConst("x"+nameCount);
      y = ctx.mkIntConst("y"+nameCount);

      BoolExpr b = operatorToSpec.get(node.getOperator());
  
      // System.out.println(x);
      // System.out.println(xprev);
      // System.out.println(b);
      b = (BoolExpr) b.substitute(xin, xprev);
      b = (BoolExpr) b.substitute(xout, x);
      b = (BoolExpr) b.substitute(yin, yprev);
      b = (BoolExpr) b.substitute(yout, y);
      // System.out.println(b);

      constraint = ctx.mkAnd(constraint, b);

    }

    BoolExpr b3 = ctx.mkEq(x, ctx.mkInt(outEx.numOfCols()));
    BoolExpr b4 = ctx.mkEq(y, ctx.mkInt(outEx.numOfRows()));
    constraint = ctx.mkAnd(constraint, b3, b4);


    Solver solver = ctx.mkSolver();
    solver.add(constraint);
    return solver.check()==Status.UNSATISFIABLE;
  }

}
