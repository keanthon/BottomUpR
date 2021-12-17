package syn.pa0;

import org.rosuda.REngine.Rserve.RserveException;

import syn.base.Dataframe;
import syn.base.Interpreter;

public class Test1 extends Test0 {

  public static void main(String[] args) throws RserveException {
    new Test1().test();
  }

  @Override
  protected void test() throws RserveException {
    // add your code here
    // create input dataframe
    Dataframe input = Dataframe.mkDataframe(new String[] { "col1", "col2", "col3", "col4" },
        new Object[][] { { "x", "y", "z", }, { 1, 3, 5, }, { 2, 44, 10, }, { 12, 15, 17, },});
    System.out.println("Input in R: \n" + input.toR() + "\n");

    // create R program
    String rprog = "unite(gather(x, tmp1, tmp2, 1, 2, 3, 4), ALL, 1, 2)";
    System.out.println("R program: \n" + rprog + "\n");

    // run R program on the input dataframe
    Interpreter interp = new Interpreter();
    Dataframe output = interp.eval(rprog, input);
    System.out.println("Produced output in R: \n" + output.toR() + "\n");

    // check the output against the desired output
    Dataframe output1 = Dataframe.mkDataframe(new String[] { "ALL", }, new Object[][] {
        { "col1_x", "col1_y", "col1_z", "col2_1", "col2_3", "col2_5", "col3_2", "col3_44", "col3_10", "col4_12", "col4_15", "col4_17" }, });
    System.out.println("Desired output in R: \n" + output1.toR() + "\n");
    System.out.println("Desired output is the same as the produced output: \n" + output1.equals(output) + "\n");
  }

}
