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

  public void setAddedFiles(String filename, String contentHashed, Commit currentCommit) {
    // delete the file from removedFiles if it exists in removedFiles
    removedFiles.remove(filename);

    // delete the file from addedFiles if it exists in addedFiles and identical to the version in
    // current commit
    if (currentCommit.getFiles().containsKey(filename)
            && currentCommit.getFiles().get(filename).equals(contentHashed)) {
      addedFiles.remove(filename);
      return;
    }

    // add/overwrite the file in addedFiles
    addedFiles.put(filename, contentHashed);
  }

  /**
   * test function
   */
  public void printStaging() {
    System.out.println("Added files:");
    for (String filename : addedFiles.keySet()) {
      System.out.println(filename + " - " + addedFiles.get(filename));
    }
    System.out.println("=============");

    System.out.println("Removed files:");
    for (String filename : removedFiles) {
      System.out.println(filename);
    }
    System.out.println("=============");
  }
}
