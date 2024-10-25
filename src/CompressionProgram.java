import java.io.IOException;
import java.util.List;

public class CompressionProgram {
  public static void main(String[] args) {
    try {
      // File configuration
      String filename = "Twenty_thousand_leagues_under_the_sea";
      String fileExtension = ".txt";
      String inputFile = filename + fileExtension;
      String outputFile = filename + ".bin";

      compressFile(inputFile, outputFile);
      System.out.println("Compression completed successfully!");
      System.out.println("Compressed file saved as: " + outputFile);

    } catch (IOException e) {
      System.err.println("Error during compression: " + e.getMessage());
      e.printStackTrace();
    }
  }

  private static void compressFile(String inputFile, String outputFile) throws IOException {
    // Step 1: LZW Compression
    System.out.println("Performing LZW compression...");
    LempelZivWelch lzw = new LempelZivWelch();
    List<Integer> lzwCompressed = lzw.compress(inputFile);
    System.out.println("LZW compression completed. Output size: " + lzwCompressed.size());

    // Step 2: Huffman Compression
    System.out.println("Performing Huffman compression...");
    Huffman.compress(lzwCompressed, outputFile);
    System.out.println("Huffman compression completed.");
  }
}