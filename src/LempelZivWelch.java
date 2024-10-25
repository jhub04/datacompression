import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LempelZivWelch {
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

  public void decompress(List<Integer> lzwCompressed) {
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

    try (BufferedWriter writer = new BufferedWriter(new FileWriter("file_decompressed.txt"))) {
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
    //Spesialtilfeller
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
