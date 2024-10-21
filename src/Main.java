import java.util.List;

public class Main {
  public static void main(String[] args) {
    LempelZivWelch compressor = new LempelZivWelch();
    List<Integer> compressed = compressor.compress("test.txt");
    for (int i:compressed) System.out.println(i);
  }
}
