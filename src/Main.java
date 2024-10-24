import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class Main {
  public static void main(String[] args) {
    LempelZivWelch compressor = new LempelZivWelch();
    List<Integer> compressed = compressor.compress("test.txt");

    String huffmanCompressed = Huffman.huffmanCompress(compressed);


    try (FileOutputStream fos = new FileOutputStream("test_encoded.bin")) {
      writeBitsToFile(huffmanCompressed, fos);
      System.out.println("Huffman encoded data written to file: huffman_encoded.txt");
    } catch (IOException e) {
      e.printStackTrace();
    }
  }


  public static void writeBitsToFile(String bits, FileOutputStream fos) throws IOException {
    int length = bits.length();
    int byteIndex = 0;
    byte currentByte = 0;

    for (int i = 0; i < length; i++) {
      currentByte  <<= 1;

      if (bits.charAt(i) == '1') {
        currentByte |= 1;
      }

      byteIndex++;
      if (byteIndex == 8) {
        fos.write(currentByte);
        byteIndex = 0;
        currentByte = 0;
      }
    }

    if (byteIndex > 0) {
      currentByte <<= (8 - byteIndex);
      fos.write(currentByte);
    }
  }
}
