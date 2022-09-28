package others;

import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.List;

import utils.WriteJSON;
import utils.ReadJSON;

import org.junit.Test;

public class jsonHelper {

  @Test
  public void knowledgePro() {
    Map <String, Double> labelProMap = new HashMap<String, Double>();
    labelProMap.put("47631_杨浦", 0.1);
    labelProMap.put("47630_完全", 1.2);
    WriteJSON.knowledgePro(labelProMap, 1);
  }

  @Test
  public void readKnowledges() {
    List<String> result = ReadJSON.knowledges();
    System.out.println(result);
  }

  @Test
  public void readKnowledgePro() {
    Map<String, Double> result = ReadJSON.knowledgePro();
    System.out.println(result);
  }
}
