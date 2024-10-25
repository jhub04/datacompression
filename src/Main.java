import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

public class Main {
  public static void main(String[] args) {
    try {
      // Compress file
      String filename = "diverse";
      String file_extension = ".lyx";
      String inputFile = filename + file_extension;
      String outputFile = filename + ".bin";
      String decompressed_file = filename + "_decompressed" + file_extension;

      compressFile(inputFile, outputFile);

      // Uncomment to test decompression
      List<Integer> decompressedData = Huffman.decompress(outputFile);
      // TODO: Convert decompressed LZW data back to original file
      LempelZivWelch lzw = new LempelZivWelch();
      lzw.decompress(decompressedData, decompressed_file);

    } catch (IOException e) {
      System.err.println("Error processing file: " + e.getMessage());
      e.printStackTrace();
    }
  }

  private static void compressFile(String inputFile, String outputFile) throws IOException {
    // Step 1: LZW Compression
    LempelZivWelch lzw = new LempelZivWelch();
    List<Integer> lzwCompressed = lzw.compress(inputFile);
    System.out.println("LZW compression completed. Output size: " + lzwCompressed.size());

    // Step 2: Huffman Compression
    Huffman.compress(lzwCompressed, outputFile);
    System.out.println("Huffman compression completed. File written to: " + outputFile);
  }
}
