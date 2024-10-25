import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
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

  public static void writeCompressedData(List<Integer> lzwOutput, String outputPath) throws IOException {
    // Find max LZW code to determine array size
    int maxCode = lzwOutput.stream().mapToInt(Integer::intValue).max().orElse(0);

    // Create and fill frequency array
    int[] frequencies = new int[maxCode + 1];
    for (int code : lzwOutput) {
      frequencies[code]++;
    }

    // Build Huffman tree using non-zero frequencies
    HuffmanNode root = buildHuffmanTreeFromArray(frequencies);
    Map<Integer, String> huffmanCodes = new HashMap<>();
    generateHuffmanCodes(root, "", huffmanCodes);
    String encoded = encode(lzwOutput, huffmanCodes);

    try (DataOutputStream dos = new DataOutputStream(new FileOutputStream(outputPath))) {
      // Write max code value (for array size during decompression)
      dos.writeInt(maxCode);

      // Write number of non-zero frequencies
      int nonZeroCount = 0;
      for (int freq : frequencies) {
        if (freq > 0) nonZeroCount++;
      }
      dos.writeInt(nonZeroCount);

      // Write only non-zero frequencies with their indices
      for (int i = 0; i < frequencies.length; i++) {
        if (frequencies[i] > 0) {
          dos.writeShort(i);         // Index (LZW code)
          dos.writeInt(frequencies[i]); // Frequency
        }
      }

      // Write encoded data length
      dos.writeInt(encoded.length());

      // Write encoded data
      int currentByte = 0;
      int bitCount = 0;
      for (char bit : encoded.toCharArray()) {
        currentByte = (currentByte << 1) | (bit - '0');
        bitCount++;

        if (bitCount == 8) {
          dos.write(currentByte);
          currentByte = 0;
          bitCount = 0;
        }
      }

      if (bitCount > 0) {
        currentByte = currentByte << (8 - bitCount);
        dos.write(currentByte);
      }
    }
  }

  public static List<Integer> readCompressedData(String inputPath) throws IOException {
    try (DataInputStream dis = new DataInputStream(new FileInputStream(inputPath))) {
      // Read max code value
      int maxCode = dis.readInt();
      int[] frequencies = new int[maxCode + 1];

      // Read number of non-zero frequencies
      int nonZeroCount = dis.readInt();

      // Read non-zero frequencies
      for (int i = 0; i < nonZeroCount; i++) {
        int index = dis.readShort();
        int freq = dis.readInt();
        frequencies[index] = freq;
      }

      // Rebuild Huffman tree
      HuffmanNode root = buildHuffmanTreeFromArray(frequencies);

      // Read encoded data length
      int totalBits = dis.readInt();

      // Decode data
      List<Integer> decodedOutput = new ArrayList<>();
      HuffmanNode current = root;
      int bitsRead = 0;

      while (bitsRead < totalBits) {
        int currentByte = dis.read();
        if (currentByte == -1) break;

        for (int i = 7; i >= 0 && bitsRead < totalBits; i--) {
          int bit = (currentByte >> i) & 1;
          current = (bit == 0) ? current.left : current.right;

          if (current.left == null && current.right == null) {
            decodedOutput.add((int) current.c);
            current = root;
          }
          bitsRead++;
        }
      }

      return decodedOutput;
    }
  }

  private static HuffmanNode buildHuffmanTreeFromArray(int[] frequencies) {
    Queue<HuffmanNode> priorityQueue = new PriorityQueue<>(
        Comparator.comparing(node -> node.data)
    );

    // Create leaf nodes only for non-zero frequencies
    for (int i = 0; i < frequencies.length; i++) {
      if (frequencies[i] > 0) {
        HuffmanNode node = new HuffmanNode();
        node.data = frequencies[i];
        node.c = (char) i;
        priorityQueue.add(node);
      }
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

  // Modified compress method to use file I/O
  public static void compress(List<Integer> lzwOutput, String outputPath) throws IOException {
    writeCompressedData(lzwOutput, outputPath);
  }

  // New decompress method
  public static List<Integer> decompress(String inputPath) throws IOException {
    return readCompressedData(inputPath);
  }


}
