package syn.base;

import java.util.Map;

public class CFG {

  // this maps each symbol s to all productions that have s as return symbol
  public Map<String, Production[]> symbolToProductions;
  // start symbol of the grammar
  private String startSymbol;

  public CFG(Map<String, Production[]> symbolToProductions, String startSymbol) {
    this.symbolToProductions = symbolToProductions;
    this.startSymbol = startSymbol;
  }

  public String getStartSymbol() {
    return startSymbol;
  }

  public Production[] getProductions(String symbol) {
    return symbolToProductions.get(symbol);
  }

  public String[] getSymbolValues(String symbol) {
    String [] ret = new String[symbolToProductions.get(symbol).length];

    for (int i =0; i< symbolToProductions.get(symbol).length; i++) {
      // System.out.println(symbolToProductions.get(symbol)[i].getOperator());
      ret[i] = symbolToProductions.get(symbol)[i].getOperator().toString();
    }
    return ret;
  }
}
