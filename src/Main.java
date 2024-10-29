import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;

public class Main {
  public static void main(String[] args) {
    try {
      compressFile("opg6-kompr.lyx", "o.bin");
      //decompressFile("o.bin", "dec.lyx");
      //compressFile("diverse.lyx", "d_comp.bin");
      //decompressFile("d_comp.bin", "diverse_decompr.lyx");
    } catch (IOException e) {
      System.err.println("Error processing file: " + e.getMessage());
      e.printStackTrace();
    }
  }

  private static void compressFile(String inputFile, String outputFile) throws IOException {
    LempelZivWelch lzw = new LempelZivWelch();
    List<Integer> lzwCompressed = lzw.compress(inputFile);
    System.out.println("LZW compression completed. Output size: " + lzwCompressed.size());

    Huffman.compress(lzwCompressed, outputFile);
    System.out.println("Huffman compression completed. File written to: " + outputFile);
  }

  private static void decompressFile(String inputFile, String outputFile) throws IOException {
    System.out.println("Performing Huffman decompression...");
    List<Integer> huffmanDecompressed = Huffman.decompress(inputFile);
    System.out.println("Huffman decompression completed.");

    System.out.println("Performing LZW decompression...");
    LempelZivWelch lzw = new LempelZivWelch();
    lzw.decompress(huffmanDecompressed, outputFile);
    System.out.println("LZW decompression completed.");
  }
}

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

    // Create frequency array
    int[] frequencies = new int[maxCode + 1];
    for (int code : lzwOutput) {
      if (code < 0 || code > maxCode) {
        throw new IllegalArgumentException("Invalid LZW code: " + code);
      }
      frequencies[code]++;
    }

    // Build Huffman tree and generate codes
    HuffmanNode root = buildHuffmanTreeFromArray(frequencies);
    Map<Integer, String> huffmanCodes = new HashMap<>();
    generateHuffmanCodes(root, "", huffmanCodes);

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


      // Write non-zero frequencies with their indices
      for (int i = 0; i < frequencies.length; i++) {
        if (frequencies[i] > 0) {
          dos.writeChar(i);
          dos.writeChar(frequencies[i]);
        }
      }

      // Write encoded data length and data
      dos.writeInt(encoded.length());
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

      int[] frequencies = new int[maxCode + 1];

      // Read number of non-zero frequencies
      int nonZeroCount = dis.readInt();

      // Read frequencies
      for (int i = 0; i < nonZeroCount; i++) {
        /**
         int highByte = dis.read();
         int lowByte = dis.read();
         int code = (highByte << 8) | (lowByte & 0xFF);*/
        int code = dis.readChar();
        int freq = dis.readChar();
        frequencies[code] = freq;
      }

      // Rebuild Huffman tree
      HuffmanNode root = buildHuffmanTreeFromArray(frequencies);

      // Read encoded data length
      int totalBits = dis.readInt();

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

class LempelZivWelch {
  int dictionarySize;

  public List<Integer> compress(String fileName) {
    Map<String, Integer> dictionary = getDictionaryStart();
    List<Integer> result = new ArrayList<>();

    try (FileInputStream input = new FileInputStream(fileName);
        InputStreamReader reader = new InputStreamReader(input, StandardCharsets.UTF_8)) {


      String foundChars = "";

      int currentByte;
      while ((currentByte = reader.read()) != -1) {
        char currentChar = (char) currentByte;
        String charsToAdd = foundChars + currentChar;

        if (dictionary.containsKey(charsToAdd)) {
          foundChars = charsToAdd;
        } else {
          result.add(dictionary.get(foundChars));
          dictionary.put(charsToAdd, dictionarySize++);
          foundChars = String.valueOf(currentChar);
        }
      }

      if(!foundChars.isEmpty()) result.add(dictionary.get(foundChars));
    } catch (IOException e) {
      e.printStackTrace();
    }
    return result;
  }

  public void decompress(List<Integer> lzwCompressed, String outputPath) {
    Map<String, Integer> dictionary = getDictionaryStart();
    Map<Integer, String> reversedDictionary = reverseKeysAndValues(dictionary);

    String characters = String.valueOf((char) lzwCompressed.remove(0).intValue());
    StringBuilder decompressedData = new StringBuilder(characters);

    for(int i : lzwCompressed) {
      String decompressedString = "";
      if (reversedDictionary.containsKey(i)) {
        decompressedString = reversedDictionary.get(i);
      } else {
        decompressedString = characters + characters.charAt(0);
      }
      decompressedData.append(decompressedString);
      reversedDictionary.put(dictionarySize++, characters + decompressedString.charAt(0));
      characters = decompressedString;
    }

    try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputPath))) {
      writer.write(decompressedData.toString());
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public Map<Integer, String> reverseKeysAndValues(Map<String, Integer> map) {
    Map<Integer, String> reversed = new HashMap<>();
    for (Map.Entry<String, Integer> entry : map.entrySet()) {
      reversed.put(entry.getValue(), entry.getKey());
    }
    return reversed;
  }

  public Map<String, Integer> getDictionaryStart() {
    dictionarySize = 256;
    Map<String, Integer> dictionary = new HashMap<>();
    for (int i = 0; i < dictionarySize; i++) {
      dictionary.put(String.valueOf((char) i), i); //initialize some values to dictionary
    }

    dictionary.put("æ", dictionarySize++);
    dictionary.put("ø", dictionarySize++);
    dictionary.put("å", dictionarySize++);
    dictionary.put("é", dictionarySize++);
    dictionary.put("•", dictionarySize++);
    dictionary.put("ô", dictionarySize++);
    dictionary.put("Ω", dictionarySize++);
    dictionary.put("…", dictionarySize++);
    dictionary.put("–", dictionarySize++);
    return dictionary;
  }
}


