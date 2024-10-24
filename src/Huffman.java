import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;

class HuffmanNode {
  int data;
  char c;
  HuffmanNode left;
  HuffmanNode right;
}

class Huffman {

  public static Map<Integer, Integer> calculateFrequency(List<Integer> lwzOutput) {
    Map<Integer, Integer> frequencyMap = new HashMap<>();

    for (int code : lwzOutput) {
      frequencyMap.put(code, frequencyMap.getOrDefault(code, 0) + 1);
    }
    return frequencyMap;
  }

  public static HuffmanNode buildHuffmanTree(Map<Integer, Integer> frequencyMap) {
    Queue<HuffmanNode> priorityQueue = new PriorityQueue<>(Comparator.comparing(node -> node.data));

    // Create leaf node for each entry in the frequencyMap
    for (Map.Entry<Integer, Integer> set : frequencyMap.entrySet()) {
      HuffmanNode node = new HuffmanNode();
      node.data = set.getValue();
      node.c = (char) (int) set.getKey();
      node.left = null;
      node.right = null;
      priorityQueue.add(node);
    }

    while (priorityQueue.size() > 1) {
      HuffmanNode x = priorityQueue.poll();
      HuffmanNode y = priorityQueue.poll();

      HuffmanNode newNode = new HuffmanNode();
      newNode.data = x.data + y.data;
      newNode.c = '-';
      newNode.left = x;
      newNode.right = y;

      priorityQueue.add(newNode);
    }

    return priorityQueue.poll();
  }

  public static void generateHuffmanCodes(HuffmanNode root, String code, Map<Integer, String> huffmanCodes) {
    if (root.left == null && root.right == null) {
      huffmanCodes.put((int) root.c, code);
      return;
    }

    generateHuffmanCodes(root.left, code + "0", huffmanCodes);
    generateHuffmanCodes(root.right, code + "1", huffmanCodes);
  }

  public static String encode(List<Integer> lzwOutput, Map<Integer, String> huffmanCodes) {
    StringBuilder encodedOutput = new StringBuilder();
    for (int code : lzwOutput) {
      encodedOutput.append(huffmanCodes.get(code));
    }
    return encodedOutput.toString();
  }

  public static String huffmanCompress(List<Integer> lzwOutput) {
    Map<Integer, Integer> frequencyMap = calculateFrequency(lzwOutput);
    HuffmanNode root = buildHuffmanTree(frequencyMap);

    Map<Integer, String> huffmanCodes = new HashMap<>();
    generateHuffmanCodes(root, "", huffmanCodes);
    String encoded = encode(lzwOutput, huffmanCodes);

    return encoded;
  }


}
