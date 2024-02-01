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
   * - HEAD.txt -> name: HEAD.txt; content: branch name of current HEAD
   * - master.txt -> name: {branchname}.txt; content: commidid of the branch head
   * - stage.txt -> name: stage.txt; conteng: object of current staging area
   * - commit/ -> contains commit files (name: {commitid}.txt; content: object of the commit)
   * - blob/ -> contains blob files (name: {blobid}.txt; content: content of some version of a file)
   */
  public static final File GITLET_DIR = join(CWD, ".gitlet");
  static final File BRANCH_DIR = join(GITLET_DIR, "branch");
  static final File STAGE_FILE = join(GITLET_DIR, "stage.txt");
  static final File COMMIT_DIR = join(GITLET_DIR, "commit");
  static final File BLOB_DIR = join(GITLET_DIR, "blob");
  static final File HEAD_FILE = join(BRANCH_DIR, "HEAD.txt");


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
    Utils.writeContents(HEAD_FILE, "master");
    // make staging area file
    Utils.writeObject(STAGE_FILE, new Stage());

    System.out.println("Gitlet has been successfully initialised.");
  }

  private static Commit getCurrentHead() {
    String branch = Utils.readContentsAsString(HEAD_FILE);
    String commitId = Utils.readContentsAsString(Utils.join(BRANCH_DIR, branch + ".txt"));
    return Utils.readObject(Utils.join(COMMIT_DIR, commitId + ".txt"), Commit.class);
  }

  private static Stage getCurrentStage() {
    return Utils.readObject(STAGE_FILE, Stage.class);
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

      String blobId = Utils.sha1(Utils.readContentsAsString(f));
      stagingArea.setAddedFiles(filename, blobId, head);
      // save blob
      File blob = Utils.join(BLOB_DIR, blobId + ".txt");
      Utils.writeContents(blob, Utils.readContents(f));
    }
    stagingArea.printStaging(); // for test
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
    File currentBranchFile = Utils.join(BRANCH_DIR, Utils.readContentsAsString(HEAD_FILE) + ".txt");
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
    Utils.writeObject(STAGE_FILE, new Stage());

    // for test
    thisCommit.printFiles();
  }

  /**
   * Unstage the file if it is currently staged for addition. If the file is tracked in the current
   * commit, stage it for removal and remove the file from the working directory if the user has not
   * already done so (do not remove it unless it is tracked in the current commit).
   * <p>
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
    }
    stagingArea.printStaging(); // for test
  }

  /**
   * Starting at the current head commit, display information about each commit backwards along the
   * commit tree until the initial commit, following the first parent commit links, ignoring any
   * second parents found in merge commits. (In regular Git, this is what you get with "git log
   * --first-parent"). This set of commit nodes is called the commit’s history. For every node in
   * this history, the information it should display is the commit id, the time the commit was made,
   * and the commit message.
   * <p>
   * Here is an example of the exact format it should follow:
   * ===
   * commit a0da1ea5a15ab613bf9961fd86f010cf74c7ee48
   * Date: Thu Nov 9 20:00:05 2017 -0800
   * A commit message.
   * <p>
   * ===
   * commit 3e8bf1d794ca2e9ef8a4007275acf3751c7170ff
   * Merge: 4975af1 2c1ead1
   * Date: Thu Nov 9 12:30:00 2017 -0800
   * Merged development into master.
   */
  public static void logCommand() {
    File currentBranchFile = Utils.join(BRANCH_DIR, Utils.readContentsAsString(HEAD_FILE) + ".txt");
    String curr = Utils.readContentsAsString(currentBranchFile);
    while (curr != null) {
      Commit currCommit = Utils.readObject(Utils.join(COMMIT_DIR, curr + ".txt"), Commit.class);
      if (currCommit.getParents() == null || currCommit.getParents().size() == 1) {
        System.out.printf("===\ncommit %s\nDate: %s\n%s\n%n"
                , curr, currCommit.getTimestamp(), currCommit.getMessage());
      } else {
        String parents = "";
        for (String parent : currCommit.getParents()) {
          parents += parent.substring(0, 7);
        }
        System.out.printf("===\ncommit %s\nMerge: %s\nDate: %s\n%s\n%n"
                , curr, parents, currCommit.getTimestamp(), currCommit.getMessage());
      }
      if (currCommit.getParents() == null) {
        break;
      }
      curr = currCommit.getParents().get(0);
    }
  }

  /**
   * Like log, except displays information about all commits ever made. The order of the commits
   * does not matter. Hint: there is a useful method in gitlet.Utils that will help you iterate over
   * files within a directory.
   */
  public static void globalLogCommand() {
    List<String> commits = Utils.plainFilenamesIn(COMMIT_DIR);
    if (commits == null) {
      return;
    }

    for (String commitFile : commits) {
      Commit commit = Utils.readObject(Utils.join(COMMIT_DIR, commitFile), Commit.class);
      String commitId = commitFile.split(".txt")[0];
      if (commit.getParents() == null || commit.getParents().size() == 1) {
        System.out.printf("===\ncommit %s\nDate: %s\n%s\n%n"
                , commitId, commit.getTimestamp(), commit.getMessage());
      } else {
        String parents = "";
        for (String parent : commit.getParents()) {
          parents += parent.substring(0, 7);
        }
        System.out.printf("===\ncommit %s\nMerge: %s\nDate: %s\n%s\n%n"
                , commitId, parents, commit.getTimestamp(), commit.getMessage());
      }
    }
  }

  /**
   * Prints out the ids of all commits that have the given commit message, one per line. If there
   * are multiple such commits, it prints the ids out on separate lines. The commit message is a
   * single operand; to indicate a multiword message, put the operand in quotation marks, as for the
   * commit command below. If no such commit exists, prints the error message "Found no commit with
   * that message."
   *
   * @param message given commit message
   */
  public static void findCommand(String message) {
    List<String> commits = Utils.plainFilenamesIn(COMMIT_DIR);
    if (commits == null) {
      return;
    }

    boolean isFound = false;
    for (String commitFile : commits) {
      Commit commit = Utils.readObject(Utils.join(COMMIT_DIR, commitFile), Commit.class);
      String commitId = commitFile.split(".txt")[0];
      if (commit.getMessage().contains(message)) {
        System.out.println(commitId);
        isFound = true;
      }
    }

    if (!isFound) {
      System.out.println("Found no commit with that message.");
    }
  }

  /**
   * Displays what branches currently exist, and marks the current branch with a *. Also displays
   * what files have been staged for addition or removal.
   */
  public static void statusCommand() {
    // display branches in order
    System.out.println("=== Branches ===");
    System.out.println("*" + Utils.readContentsAsString(HEAD_FILE).split(".txt")[0]);
    List<String> branches = Utils.plainFilenamesIn(BRANCH_DIR);
    if (branches != null) {
      for (String branchFilename : branches) {
        String branch = branchFilename.split(".txt")[0];
        if (!branch.equals(Utils.readContentsAsString(HEAD_FILE).split(".txt")[0]) &&
                !branch.equals("HEAD")) {
          System.out.println(branch);
        }
      }
    }
    System.out.println();

    // display staged files and removed files in order
    Stage stagingArea = getCurrentStage();
    System.out.println("=== Staged Files ===");
    for (String file : stagingArea.getAddedFileNamesInOrder()) {
      System.out.println(file);
    }
    System.out.println();

    System.out.println("=== Removed Files ===");
    for (String file : stagingArea.getRemovedFileNamesInOrder()) {
      System.out.println(file);
    }
    System.out.println();

    /**
     * A file in the working directory is “modified but not staged” if it is
     * Tracked in the current commit, changed in the working directory, but not staged; or
     * Staged for addition, but with different contents than in the working directory; or
     * Staged for addition, but deleted in the working directory; or
     * Not staged for removal, but tracked in the current commit and deleted from the working
     * directory.
     * The final category (“Untracked Files”) is for files present in the working directory but
     * neither staged for addition nor tracked.
     */
    List<String> untracked = getUntrackedFiles();
    Map<String, String> unstagedModification = getUnstagedModification();

    System.out.println("=== Modifications Not Staged For Commit ===");
    for (String filename : unstagedModification.keySet()) {
      System.out.printf("%s(%s)\n", filename, unstagedModification.get(filename));
    }
    System.out.println();
    System.out.println("=== Untracked Files ===");
    for (String filename : untracked) {
      System.out.println(filename);
    }
    System.out.println();
  }

  /**
   * Returns names of files present in the working directory but neither staged for addition nor
   * tracked in natural order.
   *
   * @return names of files present in the working directory but neither staged for addition nor
   * tracked.
   */
  private static List<String> getUntrackedFiles() {
    Stage stagingArea = getCurrentStage();
    Map<String, String> cwdFiles = getCwdFiles();
    Map<String, String> stageFiles = stagingArea.getAddedFiles();
    Map<String, String> trackedFiles = getCurrentHead().getFiles();
    List<String> untracked = new ArrayList<>();

    for (String filename : cwdFiles.keySet()) {
      if (!stageFiles.containsKey(filename) && !trackedFiles.containsKey(filename)) {
        untracked.add(filename);
      }
    }

    Collections.sort(untracked);
    return untracked;
  }

  private static Map<String, String> getUnstagedModification() {
    Stage stagingArea = getCurrentStage();
    Map<String, String> cwdFiles = getCwdFiles();
    Map<String, String> stageFiles = stagingArea.getAddedFiles();
    List<String> stageRemovedFiles = stagingArea.getRemovedFiles();
    Map<String, String> trackedFiles = getCurrentHead().getFiles();
    Map<String, String> unstagedModification = new TreeMap<>();

    for (String filename : cwdFiles.keySet()) {
      if (trackedFiles.containsKey(filename) // current file is tracked
              && !trackedFiles.get(filename).equals(cwdFiles.get(filename)) // changes were made to current file
              && !stageFiles.containsKey(filename)  // not staged
              || stageFiles.containsKey(filename)
              && !stageFiles.get(filename).equals(cwdFiles.get(filename))) { // OR staged with different content
        unstagedModification.put(filename, "modified");
      }
    }

    for (String filename : stageFiles.keySet()) {
      if (!cwdFiles.containsKey(filename)) {
        unstagedModification.put(filename, "deleted");
      }
    }
    for (String filename : trackedFiles.keySet()) {
      if (!stageRemovedFiles.contains(filename) && !cwdFiles.containsKey(filename)) {
        unstagedModification.put(filename, "deleted");
      }
    }

    return unstagedModification;
  }

  private static Map<String, String> getCwdFiles() {
    Map<String, String> fileMap = new HashMap<>();
    List<String> files = Utils.plainFilenamesIn(CWD);
    if (files != null) {
      for (String file : files) {
        fileMap.put(file, Utils.sha1(Utils.readContentsAsString(new File(file))));
      }
    }
    return fileMap;
  }

  /**
   * Takes the version of the file as it exists in the given commit (if not given, then head commit)
   * and puts it in the working directory, overwriting the version of the file that’s already there
   * if there is one. The new version of the file is not staged.
   *
   * @param commitid restore file from given commitid
   * @param filename restore given file by filename
   */
  public static void checkoutFileCommand(String commitid, String filename) {
    // use the head commit if commitid is not provided
    if (commitid.equals("")) {
      String head = Utils.readContentsAsString(HEAD_FILE);
      commitid = Utils.readContentsAsString(Utils.join(BRANCH_DIR, head + ".txt"));
    }
    // read file from commit
    overwriteFilesFromCommit(commitid, filename);
  }

  private static void validateCommitId(String commitId) {
    if (!Utils.join(COMMIT_DIR, commitId + ".txt").exists()) {
      System.out.println("No commit with that id exists.");
      System.exit(0);
    }
  }

  /**
   * Overwrite files in CWD from given commit. If filename is given, only update that file.
   * Otherwise, update all files from the given commit.
   *
   * @param commitId the hash code of the commit that you want to retrieve
   * @param filename name of the file to be overwritten. Pass an empty string if all files from the
   *                 commit need to be overwritten
   */
  private static void overwriteFilesFromCommit(String commitId, String filename) {
    validateCommitId(commitId);
    Commit commit = Utils.readObject(Utils.join(COMMIT_DIR, commitId + ".txt"), Commit.class);
    if (!filename.equals("") && !commit.getFiles().containsKey(filename)) {
      System.out.println("File does not exist in that commit.");
      System.exit(0);
    }
    List<String> files = filename.equals("")
            ? new ArrayList<>(commit.getFiles().keySet())
            : List.of(filename);

    for (String file : files) {
      String blobId = commit.getFiles().get(file);
      File f = Utils.join(CWD, file);
      Utils.writeContents(f, Utils.readContents(Utils.join(BLOB_DIR, blobId + ".txt")));
    }
  }

  /**
   * Takes all files in the commit at the head of the given branch, and puts them in the working
   * directory, overwriting the versions of the files that are already there if they exist. Also, at
   * the end of this command, the given branch will now be considered the current branch (HEAD). Any
   * files that are tracked in the current branch but are not present in the checked-out branch are
   * deleted. The staging area is cleared, unless the checked-out branch is the current branch.
   * Not allowed to check out to current branch.
   * Not allowed to check out to other branches if current staging area is not clean.
   *
   * @param branch the name of branch to be checked out
   */
  public static void checkoutBranchCommand(String branch) {
    // no such branch
    List<String> branches = Utils.plainFilenamesIn(BRANCH_DIR);
    if (branches != null && !branches.contains(branch + ".txt")) {
      System.out.println("No such branch exists.");
      System.exit(0);
    }
    // given branch is current branch
    if (Utils.readContentsAsString(HEAD_FILE).equals(branch)) {
      System.out.println("No need to checkout the current branch.");
      System.exit(0);
    }
    // staging area is not clean (there is untracked file)
    if (!getCurrentStage().isClean() || !getUntrackedFiles().isEmpty() || !getUnstagedModification().isEmpty()) {
      System.out.println("There is an untracked file in the way; delete it, or add and commit it first.");
      System.exit(0);
    }

    // read from commit and overwrite CWD
    String checkoutCommitId = Utils.readContentsAsString(Utils.join(BRANCH_DIR, branch + ".txt"));
    overwriteFilesFromCommit(checkoutCommitId, "");
    // delete files from CWD that don't exist in commit
    deleteFilesNotExistInCommit(checkoutCommitId);

    // change the HEAD to current branch
    Utils.writeContents(HEAD_FILE, branch);
    // make new staging area / clear the staging area
    Utils.writeObject(STAGE_FILE, new Stage());
  }

  /**
   * Delete files from CWD that don't exist in the given commit.
   *
   * @param commitId hashcode of the commit to retrieve
   */
  private static void deleteFilesNotExistInCommit(String commitId) {
    Commit commit = Utils.readObject(Utils.join(COMMIT_DIR, commitId + ".txt"), Commit.class);

    List<String> CWDFiles = Utils.plainFilenamesIn(CWD);
    if (CWDFiles != null) {
      for (String filename : CWDFiles) {
        if (!commit.getFiles().containsKey(filename)) {
          Utils.restrictedDelete(Utils.join(CWD, filename));
        }
      }
    }
  }

  /**
   * Creates a new branch with the given name, and points it at the current head commit. A branch is
   * nothing more than a name for a reference (a SHA-1 identifier) to a commit node. This command
   * does NOT immediately switch to the newly created branch (just as in real Git).
   *
   * @param branch name of the branch to be created
   */
  public static void branchCommand(String branch) {
    String currentBranch = Utils.readContentsAsString(HEAD_FILE);
    String headCommitId = Utils.readContentsAsString(Utils.join(BRANCH_DIR, currentBranch + ".txt"));
    File newBranch = Utils.join(BRANCH_DIR, branch + ".txt");
    Utils.writeContents(newBranch, headCommitId);
  }

  /**
   * Deletes the branch with the given name. This only means to delete the pointer associated with
   * the branch; it does not mean to delete all commits that were created under the branch, or
   * anything like that.
   *
   * @param branch the name of a branch to be deleted
   */
  public static void rmBranchCommand(String branch) {
    if (Utils.readContentsAsString(HEAD_FILE).equals(branch)) {
      System.out.println("Cannot remove the current branch.");
      System.exit(0);
    }

    if (!Utils.join(BRANCH_DIR, branch + ".txt").delete()) {
      System.out.println("A branch with that name does not exist.");
    }
  }

  /**
   * Checks out all the files tracked by the given commit. Removes tracked files that are not
   * present in that commit. Also moves the current branch’s head to that commit node. See the intro
   * for an example of what happens to the head pointer after using reset. The [commit id] may be
   * abbreviated as for checkout. The staging area is cleared. The command is essentially checkout
   * of an arbitrary commit that also changes the current branch head.
   * Not allowed to reset if any working file is untracked in the current branch and would be
   * overwritten by the reset.
   *
   * @param commidId the hashcode of the commit that you want to retrieve
   */
  public static void resetCommand(String commidId) {
    if (!getCurrentStage().isClean() || !getUntrackedFiles().isEmpty() || !getUnstagedModification().isEmpty()) {
      System.out.println("There is an untracked file in the way; delete it, or add and commit it first.");
      System.exit(0);
    }

    overwriteFilesFromCommit(commidId, "");
    deleteFilesNotExistInCommit(commidId);

    // moves the current branch’s head to that commit node
    String currentBranch = Utils.readContentsAsString(HEAD_FILE);
    Utils.writeContents(Utils.join(BRANCH_DIR, currentBranch + ".txt"), commidId);

    // make new staging area / clear the staging area
    Utils.writeObject(STAGE_FILE, new Stage());
  }
}
