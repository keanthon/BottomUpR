package syn.project;

import java.rmi.server.ObjID;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.xml.crypto.Data;

import org.rosuda.REngine.Rserve.RserveException;

import syn.project.SynthesizerTF;
import syn.pa3.Synthesizer3;
import syn.base.CFG;
import syn.base.Dataframe;
import syn.base.Production;
import syn.project.RandomTestMaker;


public class testMaster3 {
    ArrayList<Dataframe> inputs;
    ArrayList<Dataframe> outputs;

    // RandomTestMaker maker;
    int depth;

    public static void main(String[] args) {
        try {
            testMaster3 master = new testMaster3(3);
            master.runAll();
        } catch (RserveException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public testMaster3(int depth) throws RserveException {
        this.inputs = new ArrayList<Dataframe>();
        this.outputs = new ArrayList<Dataframe>();
        this.depth = depth;
        // this.maker = new RandomTestMaker(depth);
    
    }


    public void runAll() throws RserveException {
        mkInputs();
        mkOutputs();
        runTest();
    }

    public void mkInputs() {
        inputs.add(
            Dataframe.mkDataframe(new String[] { "a", "b", "c" },
        new Object[][] { { "r1", "r2", "r3", }, { 22, 11, 22, }, { 33, 44, 33, }, })
        );
        inputs.add(
            Dataframe.mkDataframe(new String[] { "var", "val", "round", "nam", },
        new Object[][] { { 22, 11, 22, 11, }, { 0.1, 0.2, 0.5, 0.9, }, { "round1", "round2", "round1", "round2", },
            { "foo", "foo", "bar", "bar", }, })
        );
    
        inputs.add(
            Dataframe.mkDataframe(new String[] {"a", "b",}, new Object[][] {{"r1", "r2",}, {"r3", "r4",},})
        );
        inputs.add(
            Dataframe.mkDataframe(new String[] {"a", "b", "c"}, new Object[][] {{"r1", "r2", "r3"}, {"r4", "r5", "r6"},{1,2,3},})
        );
        inputs.add(
            Dataframe.mkDataframe(new String[]{"x","y",}, new Object[][] {{"r1", "r2", "r3"}, {"r3", "r4","r4"},})
        );
        


        return;
    }

    public void mkOutputs() {
        outputs.add(
            Dataframe.mkDataframe(new String[] { "tmp1", "tmp2"}, new Object[][] { { "r3_33","r2_44", }, { "c", "c", }, })
        );
        outputs.add(
            Dataframe.mkDataframe(new String[] { "tmp2","tmp1","bar", "foo" }, new Object [][] { { "round"}, { "round1"}, { 22.0}, { 22.0}, })
        );
        outputs.add(
            Dataframe.mkDataframe(new String[] { "tmp2", }, new Object [][] { { "r1_a","r3_b"}, })
        );
        outputs.add(
            Dataframe.mkDataframe(new String[] { "tmp1", "tmp2"}, new Object[][] {{"tmp1","tmp1"},{"3_r3","2_r2"}})
        );
        outputs.add(
            Dataframe.mkDataframe(new String[] { "r1", "r4"}, new Object[][] {{"r3"},{"r4"}})
        );
        return;
    }


    public void runTest() throws RserveException {
        CFG cfg = mkCFG();
        SynthesizerTF tf = new SynthesizerTF(cfg);
        Synthesizer3 syn3 = new Synthesizer3(cfg);
        SynthesizerTFNoDP noDP = new SynthesizerTFNoDP(cfg);


        for(int i = 0; i < inputs.size(); i++) {
            if(i==2) continue;
            Dataframe input = inputs.get(i);
            Dataframe output = outputs.get(i);
            System.out.println("Test " + (i+1));
        
            String rprog = tf.runTF(input, output);
            System.out.println("BottomR: "+tf.runTime);
    
            syn3.run(input, output);
            System.out.println("TopDown: "+syn3.runTime);

            noDP.runTF(input, output);
            System.out.println("noDP: "+noDP.runTime);
            System.out.println("Program: "+rprog);
            System.out.println();
        }
    }

    protected CFG mkCFG() {

        Map<String, Production[]> symbolToProductions = new HashMap<>();
    
        symbolToProductions.put("df", new Production[] {
    
            // df ::= x
            new Production("df", "x", new String[0]),
            // df ::= gather(df, newColName, newColName, oldColNum, oldColNum)
            new Production("df", "gather", new String[] { "df", "newColName", "newColName", "oldColNum", "oldColNum" }),
            // df ::= unite(df, newColName, oldColNum, oldColNum)
            new Production("df", "unite", new String[] { "df", "newColName", "oldColNum", "oldColNum" }),
            // df ::= spread(df, oldColNum, oldColNum)
            new Production("df", "spread", new String[] { "df", "oldColNum", "oldColNum" }),
            // df ::= select(df, oldColNum, oldColNum)
            new Production("df", "slice", new String[] { "df", "oldRowNum", "oldRowNum" }),
            // df ::= select(df, oldColNum, oldColNum, oldColNum)
    
        });
        symbolToProductions.put("newColName", new Production[] {
    
            // newColName ::= "tmp1"
            new Production("newColName", "tmp1", new String[0]),
            // newColName ::= "tmp2"
            new Production("newColName", "tmp2", new String[0]),
        });
    
        symbolToProductions.put("oldColNum", new Production[] {
    
            // oldColNum ::= 1
            new Production("oldColNum", 1, new String[0]),
            // oldColNum ::= 2
            new Production("oldColNum", 2, new String[0]),
            // oldColNum ::= 3
            new Production("oldColNum", 3, new String[0]),
    
        });
    
        symbolToProductions.put("oldRowNum", new Production[] {
    
          // oldColNum ::= 1
          new Production("oldRowNum", 1, new String[0]),
          // oldColNum ::= 2
          new Production("oldRowNum", 2, new String[0]),
          // oldColNum ::= 3
          new Production("oldRowNum", 3, new String[0]),
    
        });
    
        return new CFG(symbolToProductions, "df");
      }
}
