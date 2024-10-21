import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;

public class Huffman {

  public static Map<Integer, String> huffmanCodes = new HashMap<>();

  public static Map<Integer, Integer> calculateFrequency(List<Integer> lwzOutput) {
    Map<Integer, Integer> frequencyMap = new HashMap<>();

    for (int code : lwzOutput) {
      frequencyMap.put(code, frequencyMap.getOrDefault(code, 0) + 1);
    }
    return frequencyMap;
  }

  public static HuffmanNode buildHuffmanTree(Map<Integer, Integer> frequencyMap) {
    Queue<HuffmanNode> priorityQueue = new PriorityQueue<>(new HuffmanComparator());

    // Create leaf node for each entry in the frequencyMap
    for (Map.Entry<Integer, Integer> set : frequencyMap.entrySet()) {
      priorityQueue.add(new HuffmanNode(set.getKey(), set.getValue()));
    }

    while (priorityQueue.size() > 1) {
      HuffmanNode left = priorityQueue.poll();
      HuffmanNode right = priorityQueue.poll();

      HuffmanNode newNode = new HuffmanNode(-1, left.frequency + right.frequency);
      newNode.left = left;
      newNode.right = right;

      priorityQueue.add(newNode);
    }

    return priorityQueue.poll();
  }

  public static void generateHuffmanCodes(HuffmanNode root, String code) {
    if (root == null) return;

    if (root.code != -1) {
      huffmanCodes.put(root.code, code);
    }

    // Traverse left
    generateHuffmanCodes(root.left, code + "0");
    // Traverse right
    generateHuffmanCodes(root.right, code + "1");

  }

  public static String encode(List<Integer> lzwOutput, Map<Integer, String> huffmanCodes) {
    StringBuilder encodedOutput = new StringBuilder();
    for (int code : lzwOutput) {
      encodedOutput.append(huffmanCodes.get(code));
    }
    return encodedOutput.toString();
  }
}
