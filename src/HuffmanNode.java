import java.util.Comparator;

public class HuffmanNode {
  int code;
  int frequency;
  HuffmanNode left, right;

  public HuffmanNode(int code, int frequency) {
    this.code = code;
    this.frequency = frequency;
    left = right = null;
  }
}

