// used ideas on how to iterate through a HashMap
// @source https://stackoverflow.com/questions/1066589/iterate-through-a-hashmap
package gitlet;

import java.io.File;

import static gitlet.Utils.*;

import java.util.*;

/** Represents a gitlet repository.
 *
 *  does at a high level.
 *
 *  @author
 */
public class Repository {
    /**
     *
     *
     * List all instance variables of the Repository class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided two examples for you.
     */

    /** The current working directory. */
    public static final File CWD = new File(System.getProperty("user.dir"));
    /** The .gitlet directory. */
    public static final File GITLET_DIR = join(CWD, ".gitlet");
    /** The blobs directory hold all blob files in it*/
    public static final File BLOB_DIR = join(GITLET_DIR, "blob");
    /** The commit directory holds all of the commits that were ever made*/
    public static final File COMMIT_DIR = join(GITLET_DIR, "log");
    /** Tracks the current branch*/
    public static final File HEAD_FILE = join(GITLET_DIR, "headCommit");
    //private static String headCommit;
    /** Staging directory holds all files in the staging directory*/
    public static final File STAGING_DIR = join(GITLET_DIR, "staging");
    /** Directory that holds all the branches*/
    public static final File BRANCHES_DIR = join(GITLET_DIR, "branches");
    /** The master variable points to the current branch of focus*/
    public static final File MASTER_FILE = join(BRANCHES_DIR, "master");
    /** Map that holds all current files that the commit is pointing to */
    // private static HashMap stagingFiles;
    /** Directory that holds all the pointer files in it */
    private static final File POINTER_DIR = join(GITLET_DIR, "tree");

    /** Repository initializes the repository file which creates both the main GITLET
     *  as well as the Blob directory. Additionally,
     *  if these directories already exist, they aren't created again
     */
    public static void init() {
        if (!GITLET_DIR.isDirectory()) {
            GITLET_DIR.mkdir();
            BLOB_DIR.mkdir();
            STAGING_DIR.mkdir();
            COMMIT_DIR.mkdir();
            POINTER_DIR.mkdir();
            BRANCHES_DIR.mkdir();
            //Creates a new empty hashmap and serialize it
            HashMap stagingHashMap = new HashMap<String, String>();
            String hashEmpty = Utils.sha1(serialize(stagingHashMap));
            File emptyFile = join(POINTER_DIR, hashEmpty);
            writeObject(emptyFile, stagingHashMap);
            Commit initial = new Commit("initial commit", null, hashEmpty, null);
            String initHash = Utils.sha1(serialize(initial));
            // stagingHashMap.put("tree", null);
            File commitsMapFile = join(STAGING_DIR, "CommitsMap");
            File addStagingFile = join(STAGING_DIR, "StagingAdd");
            File removeStagingFile = join(STAGING_DIR, "StagingRemove");
            File initialCommit = join(COMMIT_DIR, initHash);
            writeObject(initialCommit, initial);
            writeObject(commitsMapFile, stagingHashMap);
            writeObject(addStagingFile, new HashMap<String, String>());
            writeObject(removeStagingFile, new HashMap<String, String>());
            writeContents(HEAD_FILE, "master");
            writeContents(MASTER_FILE, initHash);
            return;
        }
        System.out.println("A Gitlet version-control system "
                + "already exists in the current directory.");
    }

    /**Adds a new file to the Staging area if the file exists
     * If it does not exist, return an error message stating it doesn't.*/
    public static void add(String filename) {
        File thisFile = join(CWD, filename);
        File hashmapFile = join(STAGING_DIR, "CommitsMap");
        File addHashmapFile = join(STAGING_DIR, "StagingAdd");
        File removeHashmapFile = join(STAGING_DIR, "StagingRemove");
        String branchPointer = readContentsAsString(HEAD_FILE);
        HashMap headCommitHash = pointerFinder(branchPointer);
        HashMap addedStagingFiles = readObject(addHashmapFile, HashMap.class);
        HashMap stagingFiles = readObject(hashmapFile, HashMap.class);
        HashMap removedStagingFiles = readObject(removeHashmapFile, HashMap.class);
        //Checks to see if the file exists or not in the CWD.
        if (!thisFile.isFile()) {
            System.out.println("File does not exist.");
            return;
        }
        String thisFileHash = sha1(readContents(thisFile));
        File fileCheck = join(BLOB_DIR, thisFileHash);
        File addStaging = join(STAGING_DIR, filename);
        byte[] fileContents = readContents(thisFile);
        String fileSHA = sha1(fileContents);
        // Checks to see if the file has already been committed before,
        // and removes it from the remove staging hashmap
        if (headCommitHash.containsKey(filename) && headCommitHash.get(filename).equals(fileSHA)) {
            addStaging.delete();
            removedStagingFiles.remove(filename);
            stagingFiles.put(filename, fileSHA);
            writeObject(hashmapFile, stagingFiles);
            writeObject(removeHashmapFile, removedStagingFiles);
            writeObject(addHashmapFile, addedStagingFiles);
            return;
        }
        // Checks to see if the file has been added into the BLOB directory before
        if (!fileCheck.isFile()) {
            if (!addStaging.isFile() || !sha1(readContents(addStaging)).equals(thisFileHash)) {
                addStaging.delete();
                writeContents(addStaging, fileContents);
            }
        }
        stagingFiles.put(filename, thisFileHash);
        addedStagingFiles.put(filename, thisFileHash);
        removedStagingFiles.remove(filename);
        writeObject(hashmapFile, stagingFiles);
        writeObject(removeHashmapFile, removedStagingFiles);
        writeObject(addHashmapFile, addedStagingFiles);
    }

    /** commit creates a snapshot of the current Staging directory and creates and adds a commit
     * file to the logs directory*/
    public static void commit(String message, String mergeParent) {
        if (message.equals("")) {
            System.out.println("Please enter a commit message.");
            return;
        }
        File hashmapFile = join(STAGING_DIR, "CommitsMap");
        HashMap stagingFiles = readObject(hashmapFile, HashMap.class);
        File addHashmapFile = join(STAGING_DIR, "StagingAdd");
        HashMap addStaging = readObject(addHashmapFile, HashMap.class);
        File removeHashmapFile = join(STAGING_DIR, "StagingRemove");
        HashMap removeStaging = readObject(removeHashmapFile, HashMap.class);
        if (addStaging.size() > 0 || removeStaging.size() > 0) {
            String treeHashed = sha1(serialize(stagingFiles));
            //
            String head = readContentsAsString(HEAD_FILE);
            File currBranchFile = join(BRANCHES_DIR, head);
            String currentBranchPointer = readContentsAsString(currBranchFile);
            //
            Commit newCommit;
            if (mergeParent != null) {
                newCommit = new Commit(message, currentBranchPointer, treeHashed, mergeParent);
            } else {
                newCommit = new Commit(message, currentBranchPointer, treeHashed, null);
            }
            String commitHash = sha1(serialize(newCommit));
            File newCommitFile = join(COMMIT_DIR, commitHash);
            File newPointerFile = join(POINTER_DIR, treeHashed);
            writeObject(newCommitFile, newCommit);
            moveStagedFiles();
            writeContents(currBranchFile, commitHash);
            writeObject(newPointerFile, stagingFiles);
            HashMap newStage = new HashMap<String, String>();
            writeObject(removeHashmapFile, newStage);
            writeObject(addHashmapFile, newStage);
            // stagingFiles.put("tree", treeHashed);
            writeObject(hashmapFile, stagingFiles);
        } else {
            System.out.println("No changes added to the commit.");
        }
    }

    /**Moves staged files to the BLOB_DIR*/
    private static void moveStagedFiles() {
        List<String> fileList = plainFilenamesIn(STAGING_DIR);
        int size = fileList.size();
        for (int i = 0; i < size; i += 1) {
            String fileName = fileList.get(i);
            if (!fileName.equals("CommitsMap") || !fileName.equals("StagingAdd")
                    || !fileName.equals("StagingRemove")) {
                File fileMoving = join(STAGING_DIR, fileName);
                byte[] fileStored = readContents(fileMoving);
                String fileSHA = sha1(fileStored);
                File blobFile = join(BLOB_DIR, sha1(fileStored));
                writeContents(blobFile, fileStored);
                fileMoving.delete();
            }
        }
    }

    /**Checkout checks out the file in the current headCommit and places it in the CWD*/
    public static void checkout(String filename) {
        String head = readContentsAsString(HEAD_FILE);
        File currBranchFile = join(BRANCHES_DIR, head);
        String currentBranchPointer = readContentsAsString(currBranchFile);
        checkoutHelper(filename, currentBranchPointer);
    }

    /**checkout checks out the given file in the given commit*/
    public static void checkout(String filename, String commitID) {
        checkoutHelper(filename, commitID);
    }

    /**branchCheckout checks out all files in the given branch*/
    public static void branchCheckout(String branchName) {
        File branchFile = join(BRANCHES_DIR, branchName);
        // checks if the branch exists
        if (!branchFile.isFile()) {
            System.out.println("No such branch exists.");
            return;
        }
        String currHead = readContentsAsString(HEAD_FILE);
        //checks if the branch given is the current branch that the head points to
        if (currHead.equals(branchName)) {
            System.out.println("No need to checkout the current branch");
            return;
        }
        //check if there is a CWD file that is untracked by the files (ex: not in Staging or Commit)
        // This part gives the hashmap for the head of the branch we want to check out
        String commitPointerString = readContentsAsString(branchFile);
        File commitPointerFile = join(COMMIT_DIR, commitPointerString);
        Commit changeCommit = readObject(commitPointerFile, Commit.class);
        String pointerHashMap = changeCommit.returnPointer();
        HashMap changeHashMap = readObject(join(POINTER_DIR, pointerHashMap), HashMap.class);
        // This part gives the hashmap for the head of the current branch
        File currentBranchFile = join(BRANCHES_DIR, currHead);
        String currentCommitString = readContentsAsString(currentBranchFile);
        File currentCommitFile = join(COMMIT_DIR, currentCommitString);
        Commit currentCommit = readObject(currentCommitFile, Commit.class);
        String currentPointerString = currentCommit.returnPointer();
        File currentPointerFile = join(POINTER_DIR, currentPointerString);
        HashMap currentHashMap = readObject(currentPointerFile, HashMap.class);
        //
        List<String> fileNamesCWD = plainFilenamesIn(CWD);
        for (String fileName: fileNamesCWD) {
            if (!currentHashMap.containsKey(fileName) && changeHashMap.containsKey(fileName)) {
                System.out.println("there is an untracked file in the way; "
                        + "delete it, or add and commit it first.");
                return;
            }
        }
        Set<String> list = currentHashMap.keySet(); // This is done so set actually works.
        for (String fileName: list) {
            join(CWD, fileName).delete();
        }
        Set<HashMap.Entry<String, String>> kvPairs = changeHashMap.entrySet();
        for (HashMap.Entry<String, String> filePairs: kvPairs) {
            String name = filePairs.getKey();
            checkout(name, commitPointerString);
        }
        writeContents(HEAD_FILE, branchName);
        deleteStaged();
        HashMap newMap = new HashMap<String, String>();
        writeObject(join(STAGING_DIR, "StagingAdd"), newMap);
        writeObject(join(STAGING_DIR, "StagingRemove"), newMap);
        writeObject(join(STAGING_DIR, "CommitsMap"), changeHashMap);
    }

    /**deleteStaged is a helper function that deletes all staged files.*/
    private static void deleteStaged() {
        List<String> fileList = plainFilenamesIn(STAGING_DIR);
        for (String file: fileList) {
            if (!file.equals("StagingAdd") && !file.equals("StagingRemove")
                    && !file.equals("CommitsMap")) {
                join(STAGING_DIR, file).delete();
            }
        }
    }

    /**checkoutHelper goes through iterates through a the trees linked with
     * the commit to find the file.*/
    private static void checkoutHelper(String filename, String commitID) {
        String newCommitID = shortID(commitID);
        if (newCommitID != null) {
            File commitFile = join(COMMIT_DIR, newCommitID);
            Commit thisCommit = readObject(commitFile, Commit.class);
            String treeString = thisCommit.returnPointer();
            if (treeString != null) {
                File currentFile = join(POINTER_DIR, treeString);
                HashMap currentHashMap = readObject(currentFile, HashMap.class);
                if (currentHashMap.containsKey(filename)) {
                    String fileString = (String) currentHashMap.get(filename);
                    byte[] contents = readContents(join(BLOB_DIR, fileString));
                    File cwdFile = join(CWD, filename);
                    writeContents(cwdFile, contents);
                    return;
                }
            }
            System.out.println("File does not exist in that commit.");
        } else {
            System.out.println("No commit with that id exists.");
        }
    }

    /** shortID is a helper function that helps check for the commitID given a shortened UID*/
    private static String shortID(String shortID) {
        int length = shortID.length();
        List<String> files = plainFilenamesIn(COMMIT_DIR);
        for (String file: files) {
            String shortFile = file.substring(0, length);
            if (shortID.equals(shortFile)) {
                return file;
            }
        }
        return null;
    }

    /** log prints out all files from headCommit down */
    public static void log() {
        String head = readContentsAsString(HEAD_FILE);
        File currBranchFile = join(BRANCHES_DIR, head);
        String currentCommitID = readContentsAsString(currBranchFile);
        while (currentCommitID != null) {
            File currentCommitFile = join(COMMIT_DIR, currentCommitID);
            Commit currentCommit = readObject(currentCommitFile, Commit.class);
            logHelper(currentCommit, currentCommitID);
            currentCommitID = currentCommit.getParent();
        }
    }

    /** global-log prints out all information from all Commits ever made*/
    public static void globalLog() {
        List<String> commits = plainFilenamesIn(COMMIT_DIR);
        for (int i = 0; i < commits.size(); i += 1) {
            String commitID = commits.get(i);
            File commitFile = join(COMMIT_DIR, commitID);
            Commit currentCommit = readObject(commitFile, Commit.class);
            logHelper(currentCommit, commitID);
        }
    }

    /** log helper helps with the printing*/
    private static void logHelper(Commit currentCommit, String commitID) {
        System.out.println("===");
        System.out.println("commit " + commitID);
        if (currentCommit.getMergeParent() != null) {
            String mergeParent = currentCommit.getMergeParent();
            String parent = currentCommit.getParent();
            System.out.println("Merge: " + parent.substring(0, 7) + " "
                    + mergeParent.substring(0, 7));
        }
        System.out.println("Date: " + currentCommit.getTimestamp());
        System.out.println(currentCommit.getMessage());
        System.out.println();
    }

    /** find finds all commits with a certain object message*/
    public static void find(String message) {
        boolean found = false;
        List<String> commits = plainFilenamesIn(COMMIT_DIR);
        for (int i = 0; i < commits.size(); i += 1) {
            String commitID = commits.get(i);
            File commitFile = join(COMMIT_DIR, commitID);
            Commit currentCommit = readObject(commitFile, Commit.class);
            if (currentCommit.getMessage().equals(message)) {
                System.out.println(commitID);
                found = true;
            }
        }
        if (!found) {
            System.out.println("Found no commit with that message.");
        }
    }

    /** rm removes a given file. Unstage the file if it is currently staged for addition.
     * If the file is tracked, stage it for removal and remove the
     * file from the working directory if the user has not already done so.*/
    public static void rm(String fileName) {
        File stagingCheck = join(STAGING_DIR, fileName);
        File currentHashFile = join(STAGING_DIR, "CommitsMap");
        HashMap currentStagingMap = readObject(currentHashFile, HashMap.class);
        File removeStagingFile = join(STAGING_DIR, "StagingRemove");
        HashMap currentRemoveStaging = readObject(removeStagingFile, HashMap.class);
        File addHashFile = join(STAGING_DIR, "StagingAdd");
        HashMap currentAddMap = readObject(addHashFile, HashMap.class);
        String branchPointer = readContentsAsString(HEAD_FILE);
        HashMap headCommitHash = pointerFinder(branchPointer);
        //Checks to see if the file is currently in STAGING_DIR and removes it if it is
        //Additionally checks to see if the file is currently already removed
        if (currentRemoveStaging.containsKey(fileName) || (!currentStagingMap.containsKey(fileName)
            && !currentAddMap.containsKey(fileName))) {
            System.out.println("No reason to remove the file.");
            return;
        }
        if (headCommitHash.containsKey(fileName)) {
            restrictedDelete(join(CWD, fileName));
            currentRemoveStaging.put(fileName, fileName);
        }
        currentAddMap.remove(fileName);
        stagingCheck.delete();
        currentStagingMap.remove(fileName);
        writeObject(currentHashFile, currentStagingMap);
        writeObject(removeStagingFile, currentRemoveStaging);
        writeObject(addHashFile, currentAddMap);
    }

    /** status currently displays the branches that
     * currently exist, marks them with a *, and displays files that have
     * been staged for addition or removal*/
    public static void status() {
        if (!GITLET_DIR.isDirectory()) {
            System.out.println("Not in an initialized Gitlet directory.");
            return;
        }
        //Iterating through branches TODO
        String head = readContentsAsString(HEAD_FILE);
        List<String> branchList = plainFilenamesIn(BRANCHES_DIR);
        Collections.sort(branchList);
        System.out.println("=== Branches ===");
        for (String branch: branchList) {
            if (branch.equals(head)) {
                System.out.println("*" + branch);
            } else {
                System.out.println(branch);
            }
        }
        System.out.println();
        // iterating through files staged for addition
        File addStagingFile = join(STAGING_DIR, "StagingAdd");
        HashMap addHash = readObject(addStagingFile, HashMap.class);
        TreeMap addTree = new TreeMap<String, String>();
        addTree.putAll(addHash);
        int addSize = addTree.size();
        System.out.println("=== Staged Files ===");
        for (int i = 0; i < addSize; i += 1) {
            //Cast as String cause the keys are stored as strings
            String name = (String) addTree.firstKey();
            System.out.println(name);
            addTree.remove(name);
        }
        System.out.println();
        //Iterating thorugh files staged for removal
        File removeStagingFile = join(STAGING_DIR, "StagingRemove");
        HashMap removeHash = readObject(removeStagingFile, HashMap.class);
        TreeMap removeTree = new TreeMap<String, String>();
        removeTree.putAll(removeHash);
        int removeSize = removeTree.size();
        System.out.println("=== Removed Files ===");
        for (int i = 0; i < removeSize; i += 1) {
            String name = (String) removeTree.firstKey();
            System.out.println(name);
            removeTree.remove(name);
        }
        System.out.println();
        //Modifications not staged for commit
        System.out.println("=== Modifications Not Staged For Commit ===");
        System.out.println();
        //Untracked Files
        System.out.println("=== Untracked Files ===");
        System.out.println();
    }

    /**Branch creates a branch with a given name that splits off from the current head file.*/
    public static void branch(String branchName) {
        //Checks to see if the branchName is already used. If it is ends the run.
        File newBranchFile = join(BRANCHES_DIR, branchName);
        if (newBranchFile.isFile()) {
            System.out.println("A branch with that name already exists.");
            return;
        }
        String currHead = readContentsAsString(HEAD_FILE);
        String currCommit = readContentsAsString(join(BRANCHES_DIR, currHead));
        writeContents(newBranchFile, currCommit);
    }

    /**Rm-branch removes a give branch without doing anything to the commits*/
    public static void rmBranch(String branchName) {
        File branchFile = join(BRANCHES_DIR, branchName);
        if (!branchFile.isFile()) {
            System.out.println("A branch with that name does not exist.");
            return;
        }
        if (readContentsAsString(HEAD_FILE).equals(branchName)) {
            System.out.println("Cannot remove the current branch.");
            return;
        }
        branchFile.delete();
    }

    /**reset resets the CWD files to a previous commit in the branch*/
    public static void reset(String commitID) {
        File commitFile = join(COMMIT_DIR, commitID);
        //checks to see if any commits with the commitID exist
        if (!commitFile.isFile()) {
            System.out.println("No commit with that id exists.");
            return;
        }
        String branchString = readContentsAsString(HEAD_FILE);
        HashMap headCommitHash = pointerFinder(branchString);
        Commit commit = readObject(commitFile, Commit.class);
        String pointerString = commit.returnPointer();
        File pointerFile = join(POINTER_DIR, pointerString);
        HashMap pointerHash = readObject(pointerFile, HashMap.class);
        //checks to see if any files are untracked and thus, would be removed from the CWD
        List<String> fileList = plainFilenamesIn(CWD);
        for (String fileName: fileList) {
            if (!headCommitHash.containsKey(fileName) && pointerHash.containsKey(fileName)) {
                System.out.println("There is an untracked file "
                        + "in the way; delete it, or add and commit it first.");
                return;
            }
        }
        //deletes all files in the CWD except for .gitlet and
        // checkouts the given commit. Then changes the head of the
        //current branch to point to that commit above.
        for (String fileName: fileList) {
            if (!fileName.equals(".gitlet")) {
                restrictedDelete(join(CWD, fileName));
            }
        }
        Set<String> fileSet = pointerHash.keySet();
        for (String fileName: fileSet) {
            checkout(fileName, commitID);
        }
        deleteStaged();
        writeContents(join(BRANCHES_DIR, branchString), commitID);
        writeObject(join(STAGING_DIR, "StagingRemove"), new HashMap<String, String>());
        writeObject(join(STAGING_DIR, "StagingAdd"), new HashMap<String, String>());
        writeObject(join(STAGING_DIR, "CommitsMap"), pointerHash);
    }

    /** Given the branch name, returns the given HashMap pointer associated
     * with the head of the branch*/
    private static HashMap<String, String> pointerFinder(String branchName) {
        File branchFile = join(BRANCHES_DIR, branchName);
        String commitString = readContentsAsString(branchFile);
        File commitFile = join(COMMIT_DIR, commitString);
        Commit commit = readObject(commitFile, Commit.class);
        String pointerString = commit.returnPointer();
        File pointerFile = join(POINTER_DIR, pointerString);
        return readObject(pointerFile, HashMap.class);
    }

    /** merge merges two branches together, the head and
     * the given branch.*/
    public static void merge(String branchName) {
        HashMap addHashMap = readObject(join(STAGING_DIR, "StagingAdd"), HashMap.class);
        HashMap removeHashMap = readObject(join(STAGING_DIR, "StagingRemove"), HashMap.class);
        File changeBranchFile = join(BRANCHES_DIR, branchName);
        if (addHashMap.size() > 0 || removeHashMap.size() > 0) {
            System.out.println("You have uncommitted changes.");
            return;
        }
        if (!changeBranchFile.isFile()) {
            System.out.println("A branch with that name does not exist.");
            return;
        }
        String headBranchName = readContentsAsString(HEAD_FILE);
        if (headBranchName.equals(branchName)) {
            System.out.println("Cannot merge a branch with itself.");
            return;
        }
        HashMap<String, String> headHashMap = pointerFinder(headBranchName);
        HashMap<String, String> givenHashMap = pointerFinder(branchName);
        List<String> cwdFileList = plainFilenamesIn(CWD);
        for (String fileName: cwdFileList) {
            if (!headHashMap.containsKey(fileName) && givenHashMap.containsKey(fileName)) {
                System.out.println("There is an untracked file in the way; delete it, "
                        + "or add and commit it first.");
                return;
            }
        }
        String splitPoint = findSplitPoint(headBranchName, branchName);
        HashMap<String, String> splitHashMap = pointerFromCommitString(splitPoint);
        if (splitPoint.equals(findCommitStringFromBranch(branchName))) {
            System.out.println("Given branch is an ancestor of the current branch.");
            return;
        }
        if (splitPoint.equals(findCommitStringFromBranch(headBranchName))) {
            branchCheckout(branchName);
            System.out.println("Current branch fast-forwarded.");
            return;
        }
        HashSet<String> fileSet = fileSetCreator(splitHashMap, headHashMap, givenHashMap);
        boolean mergeConflict = false;
        String givenCommitID = findCommitStringFromBranch(branchName);
        for (String key : fileSet) {
            String splitValue = splitHashMap.get(key);
            String headValue = headHashMap.get(key);
            String givenValue = givenHashMap.get(key);
            if (splitValue != null && headValue != null && givenValue != null) {
                if ((splitValue.equals(headValue) && splitValue.equals(givenValue))
                        || headValue.equals(givenValue) || splitValue.equals(givenValue)) {
                    boolean a;
                    continue;
                } else if (splitValue.equals(headValue)) {
                    checkoutAndAdd(key, givenCommitID);
                } else {
                    mergeConflict = mergeConflict(headValue, givenValue, key);
                }
            } else if (splitValue == null && headValue != null && givenValue != null) {
                if (!headValue.equals(givenValue)) {
                    mergeConflict = mergeConflict(headValue, givenValue, key);
                }
            } else if (splitValue != null && headValue == null && givenValue != null) {
                if (!splitValue.equals(givenValue)) {
                    mergeConflict = mergeConflict(headValue, givenValue, key);
                }
            } else if (splitValue != null && headValue != null && givenValue == null) {
                if (splitValue.equals(headValue)) {
                    rm(key);
                } else {
                    mergeConflict = mergeConflict(headValue, givenValue, key);
                }
            } else if (givenValue != null) {
                checkoutAndAdd(key, givenCommitID);
            }
        }
        File newBranchFile = join(BRANCHES_DIR, branchName);
        commit("Merged " + branchName + " into " + headBranchName + ".",
                readContentsAsString(newBranchFile));
        if (mergeConflict) {
            System.out.println("Encountered a merge conflict");
        }
    }

    /**helper function that helps to find the split point of two branches*/
    private static String findSplitPoint(String branch1, String branch2) {
        String commitString1 = readContentsAsString(join(BRANCHES_DIR, branch1));
        String commitString2 = readContentsAsString(join(BRANCHES_DIR, branch2));
        Set commitString1Storage = ancestorSet(commitString1);
        ArrayDeque<String> queue = new ArrayDeque<String>();
        queue.addLast(commitString2);
        // BFS implementation to get the closest split point
        while (queue.size() > 0) {
            if (commitString1Storage.contains(queue.peekFirst())) {
                return queue.peekFirst();
            }
            String thisCommitString = queue.removeFirst();
            Commit thisCommit = findCommitFromCommitString(thisCommitString);
            if (thisCommit.getMergeParent() != null) {
                queue.addLast(thisCommit.getMergeParent());
            }
            queue.addLast(thisCommit.getParent());
        }
        return "should not need to return this";
    }

    /** Helper functions that puts all commitStrings into a set*/
    private static Set ancestorSet(String commitString) {
        HashSet<String> thisSet = new HashSet<String>();
        String commitName = commitString;
        while (commitName != null) {
            //Checks to see if we got to split point of a merge
            //meaning we already iterated through everything
            if (thisSet.contains(commitName)) {
                break;
            }
            thisSet.add(commitName);
            Commit thisCommit = findCommitFromCommitString(commitName);
            //Checks to see if there is a merge pointer. If there is, look into it
            if (thisCommit.getMergeParent() != null) {
                thisSet.addAll(ancestorSet(thisCommit.getMergeParent()));
            }
            commitName = thisCommit.getParent();
        }
        return thisSet;
    }

    /**fileSetCreator helps create a set of all keys of 3 hashmaps*/
    private static HashSet<String> fileSetCreator(HashMap splitHashMap,
                                              HashMap headHashMap, HashMap givenHashMap) {
        HashSet<String> fileSet = new HashSet<>();
        Set<String> splitSet = splitHashMap.keySet();
        Set<String> headSet = headHashMap.keySet();
        Set<String> givenSet = givenHashMap.keySet();
        fileSet.addAll(splitSet);
        fileSet.addAll(headSet);
        fileSet.addAll(givenSet);
        return fileSet;
    }

    /**Helper function that helps with conflicts in merge*/
    private static boolean mergeConflict(String headValue, String givenValue, String key) {
        String write = mergeStringReturn(headValue, givenValue);
        File currentFile = join(CWD, key);
        writeContents(currentFile, write);
        add(key);
        return true;
    }

    /**Helper function that checks out a file and adds it to the staging area*/
    private static void checkoutAndAdd(String key, String givenCommitID) {
        checkout(key, givenCommitID);
        add(key);
    }

    /**Given the commit string, find the HashMap associated with it*/
    private static HashMap<String, String> pointerFromCommitString(String commitString) {
        String splitPoint = commitString;
        Commit splitCommit = findCommitFromCommitString(splitPoint);
        String splitPointer = splitCommit.returnPointer();
        File splitPointerFile = join(POINTER_DIR, splitPointer);
        HashMap<String, String> splitHashMap = readObject(splitPointerFile, HashMap.class);
        return splitHashMap;
    }

    /** helper function that gives the commit for a given branch*/
    private static Commit findCommitFromBranch(String branch) {
        File branchFile = join(BRANCHES_DIR, branch);
        String commitString = readContentsAsString(branchFile);
        File commitFile = join(COMMIT_DIR, commitString);
        return readObject(commitFile, Commit.class);
    }

    /** helper function that gives the commit string when given a branch*/
    private static String findCommitStringFromBranch(String branch) {
        File branchFile = join(BRANCHES_DIR, branch);
        return readContentsAsString(branchFile);
    }

    /** helper function that gives the commit for a given commitString*/
    private static Commit findCommitFromCommitString(String commitString) {
        File commitFile = join(COMMIT_DIR, commitString);
        return readObject(commitFile, Commit.class);
    }

    /** mergeStringReturn gives the file string that results from a successful merge*/
    private static String mergeStringReturn(String headValue, String givenValue) {
        String headString;
        String givenString;
        if (headValue != null) {
            File headFile = join(BLOB_DIR, headValue);
            headString = readContentsAsString(headFile);
        } else {
            headString = "";
        }
        if (givenValue != null) {
            File givenFile = join(BLOB_DIR, givenValue);
            givenString = readContentsAsString(givenFile);
        } else {
            givenString = "";
        }
        String write = "<<<<<<< HEAD\n" + headString
                + "=======\n" + givenString + ">>>>>>>\n";
        return write;
    }
}
