package syn.pa0;

import org.rosuda.REngine.Rserve.RserveException;

import syn.base.Dataframe;
import syn.base.Interpreter;

public class Test0 {

  public static void main(String[] args) throws RserveException {
    new Test0().test();
  }

  protected void test() throws RserveException {

    // create input dataframe
    Dataframe input = Dataframe.mkDataframe(new String[] { "a", "b", "c" },
        new Object[][] { { "r1", "r2", "r3", }, { 22, 11, 22, }, { 33, 44, 33, }, });
    System.out.println("Input in R: \n" + input.toR() + "\n");

    // create R program
    String rprog = "gather(unite(x,tmp1,1,2),tmp2,tmp3,1,2)";
    System.out.println("R program: \n" + rprog + "\n");

    // run R program on the input dataframe
    Interpreter interp = new Interpreter();
    Dataframe output = interp.eval(rprog, input);
    System.out.println("Produced output in R: \n" + output.toR() + "\n");

    // check the output against the desired output
    Dataframe output1 = Dataframe.mkDataframe(new String[] { "tmp2", "tmp3", }, new Object[][] {
        { "tmp1", "tmp1", "tmp1", "c", "c", "c", }, { "r1_22", "r2_11", "r3_22", "33", "44", "33" }, });
    System.out.println("Desired output in R: \n" + output1.toR() + "\n");
    System.out.println("Desired output is the same as the produced output: \n" + output1.equals(output) + "\n");

  }

}
