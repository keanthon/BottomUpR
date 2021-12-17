package syn.project;

import syn.base.Production;
import syn.base.CFG;
import syn.base.Dataframe;

public class SavedValue {
    // int weight;
    String rProgram;
    Dataframe output;
    public SavedValue(String rProg, Dataframe output) {
        // this.weight = weight;
        this.rProgram = rProg;
        this.output = output;
    }

    public String toR() {
        // return string in the format operator(x, arg1, arg2, ...)
        return rProgram;
    }

    // public int getWeight() {
    //     return weight;
    // }

    public Dataframe getOutput() {
        return output;
    }
}
