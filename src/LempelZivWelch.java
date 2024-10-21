import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LempelZivWelch {

  public List<Integer> compress(String fileName) {
    int dictionarySize = 256;
    Map<String, Integer> dictionary = new HashMap<>();
    for (int i = 0; i < dictionarySize; i++) {
      dictionary.put(String.valueOf((char) i), i); //initialize some values to dictionary
    }
    List<Integer> result = new ArrayList<>();

    try (FileInputStream input = new FileInputStream(fileName)) {

      String foundChars = "";

      int currentByte;
      while ((currentByte = input.read()) != -1) {
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
}
