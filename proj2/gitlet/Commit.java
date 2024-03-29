package gitlet;

// TODO: any imports you need here

import java.io.File;
import java.io.Serializable;
import java.util.Date; // TODO: You'll likely use this in this class
import java.util.*;

/**
 * Represents a gitlet commit object.
 * Methods include updating tracked files, saving current commit object
 *  TODO: It's a good idea to give a description here of what else this Class
 *  does at a high level.
 *
 * @author Qian Li
 */
public class Commit implements Serializable {
  /**
   * TODO: add instance variables here.
   *
   * List all instance variables of the Commit class here with a useful
   * comment above them describing what that variable represents and how that
   * variable is used. We've provided one example for `message`.
   */

  /**
   * The message of this Commit.
   */
  private String message;
  private String timestamp;
  private List<String> parents;
  private Map<String, String> files;

  /* TODO: fill in the rest of this class. */
  public Commit(String message, List<String> parents) {
    this.message = message;
    this.parents = parents;
    this.timestamp = parents == null ? (new Date(0)).toString() : (new Date()).toString();
    files = new HashMap<>();
  }

  public String getMessage() {
    return message;
  }

  public String getTimestamp() {
    return timestamp;
  }

  public List<String> getParents() {
    return parents;
  }

  public Map<String, String> getFiles() {
    return files;
  }

  public String save() {
    String id = Utils.sha1(Utils.serialize(this));
    File f = Utils.join(Repository.COMMIT_DIR, id + ".txt");
    Utils.writeObject(f, this);
    return id;
  }

  public void updateFiles(Stage stagingArea, Map<String, String> files) {
    this.files = files;
    for (String filename : stagingArea.getAddedFiles().keySet()) {
      files.put(filename, stagingArea.getAddedFiles().get(filename));
    }
    for (String filename : stagingArea.getRemovedFiles()) {
      files.remove(filename);
    }
  }

  // test function

  public void printFiles() {
    System.out.println("Tracked files:");
    for (String filename : files.keySet()) {
      System.out.println(filename + ": " + files.get(filename));
    }
    System.out.println("=============");
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Commit commit = (Commit) o;
    return Objects.equals(message, commit.message) && Objects.equals(timestamp, commit.timestamp) && Objects.equals(parents, commit.parents) && Objects.equals(files, commit.files);
  }

  public String getHash() {
    return Utils.sha1(Utils.serialize(this));
  }
}
