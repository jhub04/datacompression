import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
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
  private static final Boolean DEBUG = true;

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
    // Find max LZW code
    int maxCode = lzwOutput.stream().mapToInt(Integer::intValue).max().orElse(0);
    if (DEBUG) System.out.println("Max LZW code: " + maxCode);

    // Create frequency array
    int[] frequencies = new int[maxCode + 1];
    for (int code : lzwOutput) {
      if (code < 0 || code > maxCode) {
        throw new IllegalArgumentException("Invalid LZW code: " + code);
      }
      frequencies[code]++;
    }

    if (DEBUG) {
      System.out.println("Non-zero frequencies:");
      for (int i = 0; i < frequencies.length; i++) {
        if (frequencies[i] > 0) {
          System.out.printf("Code %d: %d times%n", i, frequencies[i]);
        }
      }
    }

    // Build Huffman tree and generate codes
    HuffmanNode root = buildHuffmanTreeFromArray(frequencies);
    Map<Integer, String> huffmanCodes = new HashMap<>();
    generateHuffmanCodes(root, "", huffmanCodes);

    if (DEBUG) {
      System.out.println("Huffman codes:");
      huffmanCodes.forEach((code, bits) ->
          System.out.printf("Code %d: %s%n", code, bits));
    }

    String encoded = encode(lzwOutput, huffmanCodes);

    try (DataOutputStream dos = new DataOutputStream(new FileOutputStream(outputPath))) {
      // Write max code value
      dos.writeInt(maxCode);

      // Count and write number of non-zero frequencies
      int nonZeroCount = 0;
      for (int freq : frequencies) {
        if (freq > 0) nonZeroCount++;
      }
      dos.writeInt(nonZeroCount);

      if (DEBUG) System.out.println("Number of non-zero frequencies: " + nonZeroCount);

      // Write non-zero frequencies with their indices
      for (int i = 0; i < frequencies.length; i++) {
        if (frequencies[i] > 0) {
          dos.writeShort(i);
          dos.writeInt(frequencies[i]);
        }
      }

      // Write encoded data length and data
      dos.writeInt(encoded.length());
      if (DEBUG) System.out.println("Encoded data length: " + encoded.length());

      // Write bits
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

      // Write remaining bits
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
      if (DEBUG) System.out.println("Reading - Max code: " + maxCode);

      int[] frequencies = new int[maxCode + 1];

      // Read number of non-zero frequencies
      int nonZeroCount = dis.readInt();
      if (DEBUG) System.out.println("Reading - Non-zero frequencies count: " + nonZeroCount);

      // Read frequencies
      for (int i = 0; i < nonZeroCount; i++) {
        int code = dis.readShort();
        int freq = dis.readInt();
        frequencies[code] = freq;
        if (DEBUG) System.out.printf("Reading - Code %d: %d times%n", code, freq);
      }

      // Rebuild Huffman tree
      HuffmanNode root = buildHuffmanTreeFromArray(frequencies);
      if (DEBUG) {
        System.out.println("Reconstructed Huffman codes:");
        Map<Integer, String> reconstructedCodes = new HashMap<>();
        generateHuffmanCodes(root, "", reconstructedCodes);
        reconstructedCodes.forEach((code, bits) ->
            System.out.printf("Code %d: %s%n", code, bits));
      }

      // Read encoded data length
      int totalBits = dis.readInt();
      if (DEBUG) System.out.println("Reading - Total bits: " + totalBits);

      // Decode data
      List<Integer> decodedOutput = new ArrayList<>();
      HuffmanNode current = root;
      int bitsRead = 0;
      StringBuilder bitSequence = new StringBuilder();  // For debugging

      while (bitsRead < totalBits) {
        int currentByte = dis.read();
        if (currentByte == -1) {
          throw new EOFException("Unexpected end of file");
        }

        for (int i = 7; i >= 0 && bitsRead < totalBits; i--) {
          int bit = (currentByte >> i) & 1;
          if (DEBUG) bitSequence.append(bit);

          current = (bit == 0) ? current.left : current.right;

          if (current.left == null && current.right == null) {
            decodedOutput.add((int) current.c);
            current = root;
          }
          bitsRead++;
        }
      }

      if (DEBUG) {
        System.out.println("Read bit sequence: " + bitSequence);
        System.out.println("Decoded " + decodedOutput.size() + " symbols");
      }

      return decodedOutput;
    }
  }

  private static HuffmanNode buildHuffmanTreeFromArray(int[] frequencies) {
    Queue<HuffmanNode> priorityQueue = new PriorityQueue<>(
        (a, b) -> {
          if (a.data != b.data) {
            return Integer.compare(a.data, b.data);
          }
          return Integer.compare(a.c, b.c);  // Ensure consistent ordering
        }
    );

    // Create leaf nodes for non-zero frequencies
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
      // Ensure consistent ordering of children
      if (x.data < y.data || (x.data == y.data && x.c < y.c)) {
        newNode.left = x;
        newNode.right = y;
      } else {
        newNode.left = y;
        newNode.right = x;
      }

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
