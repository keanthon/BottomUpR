package syn.pa2;

import com.microsoft.z3.BoolExpr;
import com.microsoft.z3.Context;
import com.microsoft.z3.Solver;
import com.microsoft.z3.Status;

public class Test4 {

  public static void main(String[] args) {
    new Test4().test();
  }

  protected void test() {
    Context ctx = new Context();
    BoolExpr expr = ctx.mkEq(ctx.mkIntConst("x"), ctx.mkInt(1));
    Solver solver = ctx.mkSolver();
    solver.add(expr);
    Status s = solver.check();
    System.out.println(expr + " is " + s);
  }
}
