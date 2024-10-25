import java.io.IOException;
import java.util.List;

public class DecompressionProgram {
  public static void main(String[] args) {
    try {
      // File configuration
      String filename = "Twenty_thousand_leagues_under_the_sea";
      String fileExtension = ".txt";
      String inputFile = filename + ".bin";
      String outputFile = filename + "_decompressed" + fileExtension;

      decompressFile(inputFile, outputFile);
      System.out.println("Decompression completed successfully!");
      System.out.println("Decompressed file saved as: " + outputFile);

    } catch (IOException e) {
      System.err.println("Error during decompression: " + e.getMessage());
      e.printStackTrace();
    }
  }

  private static void decompressFile(String inputFile, String outputFile) throws IOException {
    // Step 1: Huffman Decompression
    System.out.println("Performing Huffman decompression...");
    List<Integer> huffmanDecompressed = Huffman.decompress(inputFile);
    System.out.println("Huffman decompression completed.");

    // Step 2: LZW Decompression
    System.out.println("Performing LZW decompression...");
    LempelZivWelch lzw = new LempelZivWelch();
    lzw.decompress(huffmanDecompressed, outputFile);
    System.out.println("LZW decompression completed.");
  }
}