package gitlet;

import java.util.*;

public class Stage {
  private Map<String, String> addedFiles;
  private List<String> removedFiles;

  public Stage() {
    addedFiles = new HashMap<>();
    removedFiles = new ArrayList<>();
  }

  public Map<String, String> getAddedFiles() {
    return addedFiles;
  }

  public List<String> getRemovedFiles() {
    return removedFiles;
  }
}
