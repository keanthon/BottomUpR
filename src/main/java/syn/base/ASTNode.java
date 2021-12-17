package syn.base;

public class ASTNode {

  // DSL grammar symbol that this ast node corresponds to
  // no matter this ast node is a hole (unknown) or concrete, symbol is not null
  private String symbol;

  // DSL operator that this ast node corresponds to
  // if this ast node is a hole, then operator is null
  private Object operator;

  public ASTNode(String symbol, Object operator) {
    this.symbol = symbol;
    this.operator = operator;
  }

  public Object getOperator() {
    return operator;
  }

  public String getSymbol() {
    return symbol;
  }

  public boolean isHole() {
    return operator == null;
  }

  // translate this node to R
  public String toR() {
    if (operator == null) {
      return "?";
    } else {
      return operator.toString();
    }
  }

  @Override
  public int hashCode() {
    return operator == null ? symbol.hashCode() : operator.hashCode();
  }

  @Override
  public boolean equals(Object o) {
    return o == this;
  }

  @Override
  public String toString() {
    return toR();
  }

}
