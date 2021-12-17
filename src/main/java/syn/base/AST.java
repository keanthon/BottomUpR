package syn.base;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class AST {

  // root node of this ast
  private ASTNode root;

  // each non-leaf node is mapped to its children
  // leaf nodes are not contained as keys in this map
  private Map<ASTNode, ASTNode[]> nodeToChildren;

  // each node is mapped to its parent
  // root node is explicitly mapped to NULL
  private Map<ASTNode, ASTNode> nodeToParent;

  // this is bottom-left node which is of dataframe type
  private ASTNode bottomLeftNode;

  public AST(ASTNode root, Map<ASTNode, ASTNode[]> nodeToChildren, Map<ASTNode, ASTNode> nodeToParent) {
    this.root = root;
    this.nodeToChildren = nodeToChildren;
    this.nodeToParent = nodeToParent;
    ASTNode n = root;
    for (; !isLeaf(n); n = getChildren(n)[0])
      ;
    this.bottomLeftNode = n;
  }

  public Iterator<ASTNode> iterator() {
    return nodeToParent.keySet().iterator();
  }

  public boolean isLeaf(ASTNode node) {
    return nodeToChildren.get(node) == null;
  }

  public ASTNode[] getChildren(ASTNode node) {
    ASTNode[] ret = nodeToChildren.get(node);
    return ret == null ? new ASTNode[0] : ret;
  }

  public ASTNode getParent(ASTNode node) {
    return nodeToParent.get(node);
  }

  public ASTNode getRoot() {
    return root;
  }

  public ASTNode getBottomLeftNode() {
    return bottomLeftNode;
  }

  public int numOfOperators() {
    return nodeToChildren.size();
  }

  // translate this ast to an R program
  public String toR() {
    return toR(root);
  }

  // translate the sub-ast of this ast that's rooted at a specific node to R
  protected String toR(ASTNode node) {
    String ret = node.toR();
    if (isLeaf(node)) {
      return ret;
    }
    ASTNode[] children = getChildren(node);
    ret += "(";
    for (int i = 0; i < children.length; i++) {
      if (i > 0) {
        ret += ",";
      }
      ret += toR(children[i]);
    }
    ret += ")";
    return ret;
  }

  // @node must be an existing leaf node in the ast
  public AST expand(ASTNode node, Production prod) {

    // this is the new ndoe that we're going to create
    ASTNode newNode = new ASTNode(prod.getReturnSymbol(), prod.getOperator());

    Map<ASTNode, ASTNode[]> nodeToChildren1 = new HashMap<>();
    Map<ASTNode, ASTNode> nodeToParent1 = new HashMap<>();

    if (node == root) {
      // map the new node to NULL b/c this is the root node
      nodeToParent1.put(newNode, null);
      // now add new children for this new node
      String[] argSymbols = prod.getArgumentSymbols();
      // we add new children only if the new node has at least one child
      if (argSymbols.length != 0) {
        ASTNode[] children = new ASTNode[argSymbols.length];
        for (int i = 0; i < children.length; i++) {
          ASTNode child = new ASTNode(argSymbols[i], null);
          children[i] = child;
          nodeToParent1.put(child, newNode);
        }
        nodeToChildren1.put(newNode, children);
      }
      return new AST(newNode, nodeToChildren1, nodeToParent1);
    } else {
      // first, make a copy of the existing ast
      {
        for (ASTNode k : nodeToChildren.keySet()) {
          ASTNode[] children = nodeToChildren.get(k);
          ASTNode[] children1 = new ASTNode[children.length];
          System.arraycopy(children, 0, children1, 0, children.length);
          nodeToChildren1.put(k, children1);
        }
        nodeToParent1 = new HashMap<>(nodeToParent);
      }
      // now we update this copy
      // we need to make sure the old node is completely removed
      ASTNode parent = nodeToParent1.get(node);
      ASTNode[] siblings = nodeToChildren1.get(parent);
      for (int i = 0; i < siblings.length; i++) {
        if (siblings[i].equals(node)) {
          siblings[i] = newNode;
          break;
        }
      }
      nodeToParent1.remove(node);
      nodeToChildren1.remove(node);
      // now at this point, the old node completely gets removed
      nodeToParent1.put(newNode, parent);
      // now let's add new children of the new node
      String[] argSymbols = prod.getArgumentSymbols();
      if (argSymbols.length != 0) {
        ASTNode[] children = new ASTNode[argSymbols.length];
        for (int i = 0; i < children.length; i++) {
          ASTNode child = new ASTNode(argSymbols[i], null);
          children[i] = child;
          nodeToParent1.put(child, newNode);
        }
        nodeToChildren1.put(newNode, children);
      }
      return new AST(root, nodeToChildren1, nodeToParent1);
    }
  }

  @Override
  public String toString() {
    return toR();
  }

}