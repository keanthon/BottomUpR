package syn.base;

import java.util.ArrayList;
import java.util.List;

import org.rosuda.REngine.REXPDouble;
import org.rosuda.REngine.REXPInteger;
import org.rosuda.REngine.REXPFactor;
import org.rosuda.REngine.REXPString;
import org.rosuda.REngine.RList;

public class Dataframe {

  // an array of column names
  public String[] colNames;

  // actual content in the data frame
  // elements in the table are stored column-wise, that is, content[0] is the
  // first column, content[1] is the second, etc.
  private Object[][] content;

  // one has to use mkDataframe functions to create Dataframe objects
  private Dataframe(String[] colNames, Object[][] content) {
    this.colNames = colNames;
    this.content = content;
    canonicalize();
  }

  // this function makes sure:
  // - each int element is converted to a double
  // - each double element is converetd to a doulbe with one decimal point
  // - each string element remains the same
  // - each NULL element is converted to NA
  private void canonicalize() {
    for (int i = 0; i < content.length; i++) {
      for (int j = 0; j < content[0].length; j++) {
        Object o = content[i][j];
        if (o == null) {
          content[i][j] = "NA";
        } else if (o instanceof Double) {
          content[i][j] = ((int) (((Double) o) * 10)) / 10.0;
        } else if (o instanceof Integer) {
          content[i][j] = ((int) o) * 1.0;
        } else if (o instanceof String) {
        } else {
          throw new RuntimeException();
        }
      }
    }
  }

  public Boolean containNAs() {
    for (int i = 0; i < content.length; i++) {
      for (int j = 0; j < content[0].length; j++) {
        if(content[i][j]=="NA" ) {
          return true;
        }
        if(content[i][j] instanceof Double && ((Double) content[i][j]) ==0.0) {
          return true;
        }
        
      }
    }
    return false;
  }
  // cnames: a list of column names
  // cols: all columns
  public static Dataframe mkDataframe(String[] cnames, Object[][] cols) {
    return new Dataframe(cnames, cols);
  }

  public static Dataframe mkDataframe(List<String> cnames, List<List<Object>> cols) {
    String[] cnames1 = new String[cnames.size()];
    Object[][] cols1 = new Object[cnames.size()][];
    for (int i = 0; i < cnames1.length; i++) {
      cnames1[i] = cnames.get(i);
      cols1[i] = cols.get(i).toArray();
    }
    return mkDataframe(cnames1, cols1);
  }

  // given a dataframe of RList type (REngine's datatype), create a dataframe in
  // our own representation
  public static Dataframe mkDataframe(RList rlist) {
    List<String> cnames = new ArrayList<>();
    List<List<Object>> cols = new ArrayList<>();
    for (Object cname : rlist.names) {
      cnames.add((String) cname);
    }
    for (Object o : rlist) {
      List<Object> col = new ArrayList<>();
      if (o instanceof REXPDouble) {
        for (Object e : ((REXPDouble) o).asDoubles()) {
          col.add(e);
        }
      } else if (o instanceof REXPFactor) {
        for (Object e : ((REXPFactor) o).asStrings()) {
          col.add(e);
        }
      } else if (o instanceof REXPString) {
        for (Object e : ((REXPString) o).asStrings()) {
          col.add(e);
        }
      } else {
        throw new RuntimeException();
      }
      cols.add(col);
    }
    return mkDataframe(cnames, cols);
  }

  public int numOfCols() {
    return colNames.length;
  }

  public int numOfRows() {
    return content[0].length;
  }

  // convert our dataframe representation to an R dataframe (which can be directly
  // executed in R studio)
  public String toR() {
    String ret = "data.frame(";
    for (int i = 0; i < this.colNames.length; i++) {
      if (i > 0) {
        ret += ",";
      }
      ret += this.colNames[i] + "=c(";
      for (int j = 0; j < this.content[i].length; j++) {
        if (j > 0) {
          ret += ",";
        }
        Object o = this.content[i][j];
        if (o instanceof String) {
          ret += "\"" + o + "\"";
        } else if (o instanceof Double) {
          ret += o;
        } else {
          throw new RuntimeException();
        }
      }
      ret += ")";
    }
    ret += ")";
    return ret;
  }

  @Override
  public String toString() {
    return toR();
  }

  @Override
  public int hashCode() {
    return colNames.hashCode() << 10 + content.hashCode();
  }

  @Override
  public boolean equals(Object o) {
    if (o == null) {
      return false;
    }
    if (o == this) {
      return true;
    }
    if (!(o instanceof Dataframe)) {
      return false;
    }
    Dataframe other = (Dataframe) o;
    if (other.colNames.length != colNames.length) {
      return false;
    }
    for (int i = 0; i < colNames.length; i++) {
      if (!other.colNames[i].equals(colNames[i])) {
        return false;
      }
    }
    if (other.content[0].length != content[0].length) {
      return false;
    }
    for (int i = 0; i < this.content.length; i++) {
      for (int j = 0; j < this.content[i].length; j++) {
        if (!this.content[i][j].equals(other.content[i][j])) {
          return false;
        }
      }
    }
    return true;
  }

public String[] getColNames() {
    return colNames;
}

public Object[] firstRow() {
    return content[0];
}

}
