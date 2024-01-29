package gitlet;

/**
 * Driver class for Gitlet, a subset of the Git version-control system.
 *
 * @author Qian
 */
public class Main {

  /**
   * Usage: java gitlet.Main ARGS, where ARGS contains
   * <COMMAND> <OPERAND1> <OPERAND2> ...
   */
  public static void main(String[] args) {
    /**
     * If a user doesn’t input any arguments, print the message Please enter a command. and exit.
     *
     * If a user inputs a command that doesn’t exist, print the message No command with that name exists. and exit.
     *
     * If a user inputs a command with the wrong number or format of operands, print the message Incorrect operands. and exit.
     *
     * If a user inputs a command that requires being in an initialized Gitlet working directory (i.e., one containing a
     * .gitlet subdirectory), but is not in such a directory, print the message Not in an initialized Gitlet directory.
     */
    // TODO: what if args is empty?
    if (args.length == 0) {
      System.out.println("Please enter a command.");
      System.exit(0);
    }


    String firstArg = args[0];
    if (!Repository.GITLET_DIR.exists() && !firstArg.equals("init")) {
      System.out.println("Not in an initialized Gitlet directory.");
      System.exit(0);
    }

    switch (firstArg) {
      case "init":
        // TODO: handle the `init` command
        validateArgsNum(args, 1, false);
        Repository.initCommand();
        break;
      case "add":
        // TODO: handle the `add [filename]` command
        validateArgsNum(args, 2, true);
        Repository.addCommand(args);
        break;
      // TODO: FILL THE REST IN
      case "commit":
        // TODO: handle the `commit [message]` command
        validateArgsNum(args, 2, false);
        Repository.commitCommand(args[1]);
        break;
      case "rm":
        validateArgsNum(args, 2, true);
        Repository.rmCommand(args);
        break;
      case "log":
        validateArgsNum(args, 1, false);
        Repository.logCommand();
        break;
      case "global-log":
        validateArgsNum(args, 1, false);
        Repository.globalLogCommand();
        break;
      case "find":
        validateArgsNum(args, 2, false);
        Repository.findCommand(args[1]);
        break;
      default:
        System.out.println("No command with that name exists.");
        System.exit(0);
    }
  }

  public static void validateArgsNum(String[] args, int num, boolean atLeast) {
    if (args.length < num || (!atLeast && args.length > num)) {
      System.out.println("Incorrect operands.");
      System.exit(0);
    }
  }
}
