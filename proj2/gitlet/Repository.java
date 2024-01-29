package gitlet;

import java.io.File;
import java.util.*;

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
   * - branch/
   * - HEAD.txt
   * - master.txt
   * - stage.txt/
   * - commit/
   * - blob/
   */
  public static final File GITLET_DIR = join(CWD, ".gitlet");
  static final File BRANCH_DIR = join(GITLET_DIR, "branch");
  static final File STAGE_DIR = join(GITLET_DIR, "stage.txt");
  static final File COMMIT_DIR = join(GITLET_DIR, "commit");
  static final File BLOB_DIR = join(GITLET_DIR, "blob");
  static final File HEAD_DIR = join(BRANCH_DIR, "HEAD.txt");


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
    if (GITLET_DIR.exists()) {
      System.out.println("Gitlet was already inited in current directory");
      System.exit(0);
    }

    GITLET_DIR.mkdir();
    BRANCH_DIR.mkdir();
    COMMIT_DIR.mkdir();
    BLOB_DIR.mkdir();

    // make initial commit
    Commit initialCommit = new Commit("initial commit", null);
    String initialCommitId = initialCommit.save();
    // make master branch
    File master = join(BRANCH_DIR, "master.txt");
    Utils.writeContents(master, initialCommitId);
    // make HEAD file and write current branch into HEAD
    Utils.writeContents(HEAD_DIR, "master");
    // make staging area file
    Utils.writeObject(STAGE_DIR, new Stage());

    System.out.println("Gitlet has been successfully initialised.");
  }

  public static Commit getCurrentHead() {
    String branch = Utils.readContentsAsString(HEAD_DIR);
    String commitId = Utils.readContentsAsString(Utils.join(BRANCH_DIR, branch + ".txt"));
    return Utils.readObject(Utils.join(COMMIT_DIR, commitId + ".txt"), Commit.class);
  }

  public static Stage getCurrentStage() {
    return Utils.readObject(STAGE_DIR, Stage.class);
  }

  /**
   * Adds a copy of the file as it currently exists to the staging area (see the description of the
   * commit command). For this reason, adding a file is also called staging the file for addition.
   * Staging an already-staged file overwrites the previous entry in the staging area with the new
   * contents. The staging area should be somewhere in .gitlet. If the current working version of
   * the file is identical to the version in the current commit, do not stage it to be added, and
   * remove it from the staging area if it is already there (as can happen when a file is changed,
   * added, and then changed back to it’s original version). The file will no longer be staged for
   * removal (see gitlet rm), if it was at the time of the command.
   *
   * @param args args input by users in command line
   */
  public static void addCommand(String[] args) {
    Commit head = getCurrentHead();
    Stage stagingArea = getCurrentStage();

    for (String filename : args) {
      if (filename.equals("add")) {
        continue;
      }

      File f = new File(filename);
      if (!f.exists()) {
        System.out.println("Cannot find the file " + filename);
        System.exit(0);
      }

      String contentHashed = Utils.sha1(Utils.readContentsAsString(f));
      stagingArea.setAddedFiles(filename, contentHashed, head);
      stagingArea.printStaging(); // for test
    }
  }

  /**
   * Saves a snapshot of tracked files in the current commit and staging area so they can be
   * restored at a later time, creating a new commit. The commit is said to be tracking the saved
   * files. By default, each commit’s snapshot of files will be exactly the same as its parent
   * commit’s snapshot of files; it will keep versions of files exactly as they are, and not update
   * them. A commit will only update the contents of files it is tracking that have been staged for
   * addition at the time of commit, in which case the commit will now include the version of the
   * file that was staged instead of the version it got from its parent. A commit will save and
   * start tracking any files that were staged for addition but weren’t tracked by its parent.
   * Finally, files tracked in the current commit may be untracked in the new commit as a result
   * being staged for removal by the rm command (below).
   * The bottom line: By default a commit has the same file contents as its parent. Files staged for
   * addition and removal are the updates to the commit. Of course, the date (and likely the mesage)
   * will also different from the parent.
   *
   * @param message Commit message
   */
  public static void commitCommand(String message) {
    // read from filesystem the head commit and the staging area
    Stage stagingArea = getCurrentStage();
    if (stagingArea.getAddedFiles().size() == 0 && stagingArea.getRemovedFiles().size() == 0) {
      System.out.println("The staging area is clean. Nothing to commit.");
      System.exit(0);
    }

    Commit head = getCurrentHead();
    File currentBranchFile = Utils.join(BRANCH_DIR, Utils.readContentsAsString(HEAD_DIR) + ".txt");
    String headCommitId = Utils.readContentsAsString(currentBranchFile);

    // clone the head commit
    Map<String, String> lastCommitFiles = head.getFiles();
    Map<String, String> lastCommitFilesClone = new HashMap<>();
    for (String key : lastCommitFiles.keySet()) {
      lastCommitFilesClone.put(key, lastCommitFiles.get(key));
    }
    // modify its message, timestamp, parent commit according to user input
    Commit thisCommit = new Commit(message, List.of(headCommitId));
    // update the tracking files by the use of staging area
    thisCommit.updateFiles(stagingArea, lastCommitFilesClone);

    // write back any new object made
    String commitId = thisCommit.save();
    // make current commit the head
    Utils.writeContents(currentBranchFile, commitId);

    // make new staging area / clear the staging area
    Utils.writeObject(STAGE_DIR, new Stage());

    // for test
    thisCommit.printFiles();
  }

  /**
   * Unstage the file if it is currently staged for addition. If the file is tracked in the current
   * commit, stage it for removal and remove the file from the working directory if the user has not
   * already done so (do not remove it unless it is tracked in the current commit).
   *
   * If the file is neither staged nor tracked by the head commit, print the error message "No
   * reason to remove the file."
   *
   * @param args args input by users in command line
   */
  public static void rmCommand(String args[]) {
    Stage stagingArea = getCurrentStage();
    Commit head = getCurrentHead();

    for (String filename : args) {
      if (filename.equals("rm")) {
        continue;
      }

      File f = new File(filename);
      if (!f.exists()
              && !stagingArea.getAddedFiles().containsKey(filename)
              && !head.getFiles().containsKey(filename)) {
        System.out.println("No reason to remove the file.");
        System.exit(0);
      }

      stagingArea.setRemovedFiles(filename, head);
      stagingArea.printStaging(); // for test
    }
  }

  /**
   * Starting at the current head commit, display information about each commit backwards along the
   * commit tree until the initial commit, following the first parent commit links, ignoring any
   * second parents found in merge commits. (In regular Git, this is what you get with "git log
   * --first-parent"). This set of commit nodes is called the commit’s history. For every node in
   * this history, the information it should display is the commit id, the time the commit was made,
   * and the commit message.
   *
   * Here is an example of the exact format it should follow:
   * ===
   * commit a0da1ea5a15ab613bf9961fd86f010cf74c7ee48
   * Date: Thu Nov 9 20:00:05 2017 -0800
   * A commit message.
   *
   * ===
   * commit 3e8bf1d794ca2e9ef8a4007275acf3751c7170ff
   * Merge: 4975af1 2c1ead1
   * Date: Thu Nov 9 12:30:00 2017 -0800
   * Merged development into master.
   */
  public static void logCommand() {
    File currentBranchFile = Utils.join(BRANCH_DIR, Utils.readContentsAsString(HEAD_DIR) + ".txt");
    String curr = Utils.readContentsAsString(currentBranchFile);
    while (curr != null) {
      Commit currCommit = Utils.readObject(Utils.join(COMMIT_DIR, curr + ".txt"), Commit.class);
      if (currCommit.getParents() == null || currCommit.getParents().size() == 1) {
        System.out.println(String.format("===\ncommit %s\nDate: %s\n%s\n"
                , curr, currCommit.getTimestamp(), currCommit.getMessage()));
      } else {
        String parents = "";
        for (String parent : currCommit.getParents()) {
          parents += parent.substring(0, 7);
        }
        System.out.println(String.format("===\ncommit %s\nMerge: %s\nDate: %s\n%s\n"
                , curr, parents, currCommit.getTimestamp(), currCommit.getMessage()));
      }
      if (currCommit.getParents() == null) {
        break;
      }
      curr = currCommit.getParents().get(0);
    }
  }
}
