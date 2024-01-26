package gitlet;

// TODO: any imports you need here

import java.util.Date; // TODO: You'll likely use this in this class
import java.util.*;

/**
 * Represents a gitlet commit object.
 *  TODO: It's a good idea to give a description here of what else this Class
 *  does at a high level.
 *
 * @author Qian Li
 */
public class Commit {
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
  private List<String> files;

  /* TODO: fill in the rest of this class. */
  public Commit(String message, List<String> parents) {
    this.message = message;
    this.parents = parents;
    this.timestamp = parents == null ? (new Date(0)).toString() : (new Date()).toString();
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

  public List<String> getFiles() {
    return files;
  }
}
