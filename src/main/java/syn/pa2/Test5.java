package syn.pa2;
import com.microsoft.z3.BoolExpr;
import com.microsoft.z3.Context;
import com.microsoft.z3.Solver;
import com.microsoft.z3.Status;

public class Test5 extends Test4 {

  public static void main(String[] args) {
    new Test5().test();
  }

  @Override
  protected void test() {
    // add your code here
    Context ctx = new Context();
    BoolExpr expr1 = ctx.mkGt(ctx.mkIntConst("x"), ctx.mkIntConst("y"));
    BoolExpr expr2 = ctx.mkEq(ctx.mkIntConst("x"), ctx.mkInt(2));
    BoolExpr expr3 = ctx.mkEq(ctx.mkIntConst("y"), ctx.mkInt(3));
    BoolExpr expr = ctx.mkAnd(expr1, expr2, expr3);
    Solver solver = ctx.mkSolver();
    solver.add(expr);
    Status s = solver.check();
    System.out.println(expr + " is " + s);
  }

}
