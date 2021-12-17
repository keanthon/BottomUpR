package syn.pa3;

import java.util.Comparator;
import java.util.HashSet;
import java.util.PriorityQueue;
import java.util.Stack;

import org.rosuda.REngine.Rserve.RserveException;

import syn.base.AST;
import syn.base.ASTNode;
import syn.base.CFG;
import syn.base.Dataframe;
import syn.base.Production;
import syn.pa2.Synthesizer2;

public class Synthesizer3 extends Synthesizer2 {

  public Synthesizer3(CFG cfg) throws RserveException {
    super(cfg);
  }

  @Override
  public AST run(Dataframe inEx, Dataframe outEx) {

    long start = System.currentTimeMillis();

    AST ret = null;

    // a worklist based on priority queue
    Comparator<AST> comparator = new ASTComparator();
    PriorityQueue<AST> worklist = new PriorityQueue<>(comparator);

    worklist.add(mkInitialAST());

    while (!worklist.isEmpty()) {

      iterCounter++;

      AST ast = worklist.poll();

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
    runTime = (int) (end - start);

    return ret;
  }

  class ASTComparator implements Comparator<AST> {
    @Override
    public int compare(AST ast1, AST ast2) {
      return computeAstCost(ast1) - computeAstCost(ast2);
    }
  }

  @Override
  protected boolean attemptToPrune(AST ast) {
    return false;
  }

  @Override
  protected boolean prune(AST ast, Dataframe inEx, Dataframe outEx) {
    return false;
  }

  protected int computeAstCost(AST ast) {
    // add your code here
    int sum=0;
    Stack<ASTNode> frontier = new Stack<ASTNode>();
    frontier.add(ast.getRoot());
    while(!frontier.empty()) {
      ASTNode curr = frontier.pop();

      // each operator is worth one point if not hole
      Object currOpr = curr.getOperator();
      if(currOpr!=null) {
        sum+=1;

        // if consecutive operator, penalty
        if (ast.getParent(curr)!=null && ast.getParent(curr).getOperator()==currOpr) {
          sum+=2;
        }
      }

      HashSet<Object> operators = new HashSet<Object>();
      for (ASTNode child : ast.getChildren(curr)) {
        Object childOpr = child.getOperator();
        
        // if repeated symbols, penalty
        if(childOpr!=null && operators.contains(childOpr)) {
          sum+=2;
        }
        operators.add(childOpr);
        frontier.add(child);
      }

    }

    return sum;
  }

}