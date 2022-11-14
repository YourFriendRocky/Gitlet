package gitlet;

/** Driver class for Gitlet, a subset of the Git version-control system.
 *  @author
 */
public class Main {

    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND1> <OPERAND2> ... 
     */
    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Please enter a command.");
            return;
        }
        String firstArg = args[0];
        switch (firstArg) {
            case "init":
                Repository.init();
                break;
            case "add":
                String filename = args[1];
                Repository.add(filename);
                break;
            case "commit":
                String message = args[1];
                Repository.commit(message, null);
                break;
            // Case when the firstArg is empty
            case "checkout":
                if (args.length == 3) {
                    if (args[1].equals("--")) {
                        Repository.checkout(args[2]);
                    } else {
                        System.out.println("Incorrect operands.");
                    }
                } else if (args.length == 4) {
                    if (args[2].equals("--")) {
                        Repository.checkout(args[3], args[1]);
                    } else {
                        System.out.println("Incorrect operands.");
                    }
                } else if (args.length == 2) {
                    Repository.branchCheckout(args[1]);
                }
                break;
            case "log":
                Repository.log();
                break;
            case "global-log":
                Repository.globalLog();
                break;
            case "find":
                String commitMessage = args[1];
                Repository.find(commitMessage);
                break;
            case "status":
                Repository.status();
                break;
            case "rm":
                String fileName = args[1];
                Repository.rm(fileName);
                break;
            case "branch":
                String branchName = args[1];
                Repository.branch(branchName);
                break;
            case "rm-branch":
                String branch = args[1];
                Repository.rmBranch(branch);
                break;
            case "reset":
                Repository.reset(args[1]);
                break;
            case "merge":
                Repository.merge(args[1]);
                break;
            //default case when the first argument is not a command
            default:
                System.out.println("No command with that name exists.");
                break;
        }
    }
}
