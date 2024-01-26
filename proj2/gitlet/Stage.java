package gitlet;

import java.io.Serializable;
import java.util.*;

public class Stage implements Serializable {
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
