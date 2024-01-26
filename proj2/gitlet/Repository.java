package gitlet;

import java.io.File;

import static gitlet.Utils.*;

// TODO: any imports you need here

/**
 * Represents a gitlet repository.
 *  TODO: It's a good idea to give a description here of what else this Class
 *  does at a high level.
 *
 * @author Qian Li
 */
public class Repository {
  /**
   * TODO: add instance variables here.
   *
   * List all instance variables of the Repository class here with a useful
   * comment above them describing what that variable represents and how that
   * variable is used. We've provided two examples for you.
   */

  /**
   * The current working directory.
   */
  public static final File CWD = new File(System.getProperty("user.dir"));
  /**
   * The .gitlet directory.
   * .getlet
   *  - branch/
   *    - HEAD.txt
   *    - master.txt
   *  - stage/
   *  - commit/
   *  - blob/
   */
  public static final File GITLET_DIR = join(CWD, ".gitlet");
  public static final File BRANCH_DIR = join(GITLET_DIR, "branch");
  public static final File STAGE_DIR = join(GITLET_DIR, "stage");
  public static final File COMMIT_DIR = join(GITLET_DIR, "commit");
  public static final File BLOB_DIR = join(GITLET_DIR, "blob");
  public static final File HEAD = join(BRANCH_DIR, "HEAD.txt");

  /* TODO: fill in the rest of this class. */

  /**
   * Creates a new Gitlet version-control system in the current directory.
   * This system will automatically start with one commit: a commit that contains
   * no files and has the commit message initial commit (just like that, with no punctuation).
   * It will have a single branch: master, which initially points to this initial commit,
   * and master will be the current branch. The timestamp for this initial commit will be
   * 00:00:00 UTC, Thursday, 1 January 1970 in whatever format you choose for dates
   * (this is called “The (Unix) Epoch”, represented internally by the time 0.)
   * Since the initial commit in all repositories created by Gitlet will have exactly the same content,
   * it follows that all repositories will automatically share this commit (they will all have the same UID)
   * and all commits in all repositories will trace back to it.
   */
  public static void initCommand() {
    // init repo
    // make master branch
    // make initial commit
    // make HEAD file

  }

  public static void addCommand(String[] args) {

  }

  public static void commitCommand(String message) {
    // read from filesystem the head commit and the staging area

    // clone the head commit
    // modify its message and timestamp according to user input
    // update the tracking files by the use of staging area
    // update the parent commit

    // write back any new object made
  }
}
