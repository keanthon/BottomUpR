package syn.base;

// a production is of the form 
// return symbol ::= operator( argSymbol, argSymbol, ... ) 
public class Production {

  // return symbol
  private String retSymbol;
  // operator (of Object type)
  // - operators that take at least one argument are of String type
  // - a nullary operator may have other types, such as Integer, String (in that
  // case, the operator itself is the value the operator evaluates to)
  private Object operator;
  // argument symbols
  private String[] argSymbols;

  public Production(String returnSymbol, Object operator, String[] argumentSymbols) {
    this.retSymbol = returnSymbol;
    this.operator = operator;
    this.argSymbols = argumentSymbols;
  }

  public String getReturnSymbol() {
    return retSymbol;
  }

  public Object getOperator() {
    return operator;
  }

  public String[] getArgumentSymbols() {
    return argSymbols;
  }

  @Override
  public String toString() {
    String ret = retSymbol + "::=" + operator;
    if (argSymbols.length == 0) {
      return ret;
    }
    ret += "(";
    for (int i = 0; i < argSymbols.length; i++) {
      if (i > 0) {
        ret += ",";
      }
      ret += this.argSymbols[i];
    }
    ret += ")";
    return ret;
  }

}
