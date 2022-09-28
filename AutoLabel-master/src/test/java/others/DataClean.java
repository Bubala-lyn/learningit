package others;

import utils.CommonUtils;

public class DataClean {
  public static void main(String[] args) {
    String input = "<latex>\\(  x&gt;\\ln   \\left (1+x\\right ) \\)</latex>";
    String text = CommonUtils.getTextOnly("1", input);
    System.out.println(text);
  }
}
