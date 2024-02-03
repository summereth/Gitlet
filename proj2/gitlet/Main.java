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
        validateArgsNum(args, 1, 1);
        Repository.initCommand();
        break;
      case "add":
        // TODO: handle the `add [filename]` command
        validateArgsNum(args, 2, Integer.MAX_VALUE);
        Repository.addCommand(args);
        break;
      // TODO: FILL THE REST IN
      case "commit":
        // TODO: handle the `commit [message]` command
        validateArgsNum(args, 2, 2);
        Repository.commitCommand(args[1]);
        break;
      case "rm":
        validateArgsNum(args, 2, Integer.MAX_VALUE);
        Repository.rmCommand(args);
        break;
      case "log":
        validateArgsNum(args, 1, 1);
        Repository.logCommand();
        break;
      case "global-log":
        validateArgsNum(args, 1, 1);
        Repository.globalLogCommand();
        break;
      case "find":
        validateArgsNum(args, 2, 2);
        Repository.findCommand(args[1]);
        break;
      case "status":
        validateArgsNum(args, 1, 1);
        Repository.statusCommand();
        break;
      case "checkout":
        validateArgsNum(args, 2, 4);
        if (args.length == 2) {
          Repository.checkoutBranchCommand(args[1]);
        } else if (args.length == 3) {
          Repository.checkoutFileCommand("", args[2]);
        } else {
          Repository.checkoutFileCommand(args[1], args[3]);
        }
        break;
      case "branch":
        validateArgsNum(args, 2, 2);
        Repository.branchCommand(args[1]);
        break;
      case "rm-branch":
        validateArgsNum(args, 2, 2);
        Repository.rmBranchCommand(args[1]);
        break;
      case "reset":
        validateArgsNum(args, 2, 2);
        Repository.resetCommand(args[1]);
        break;
      case "merge":
        validateArgsNum(args, 2, 2);
        Repository.mergeCommand(args[1]);
        break;
      default:
        System.out.println("No command with that name exists.");
        System.exit(0);
    }
  }

  public static void validateArgsNum(String[] args, int min, int max) {
    if (args.length < min || args.length > max) {
      System.out.println("Incorrect operands.");
      System.exit(0);
    }
  }
}
