package others;

import utils.FileOperation;

public class CopyFile {
  public static void main(String[] args) {
    new FileOperation().copyFile2Resources("knowledgePro1.json", "knowledgePro.json");
  }
}
