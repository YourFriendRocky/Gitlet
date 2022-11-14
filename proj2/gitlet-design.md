# Gitlet Design Document

**Name**: Joshua Lin

## Classes and Data Structures

### Class 1
Repository

#### Fields

1. public static final File CWD = new File(System.getProperty("user.dir"));
   This obtains the current working directory that is taken now for
   all other parts of the class to use. All classes with the same package
   can use this field.
2. public static final File GITLET_DIR = join(CWD, ".gitlet");
   This is the new hidden .gitlet directory file that all the commit
   files will be located in. This directory will store all iterations of 
   the files that are committed.
3. public static final File BLOB_DIR = join(GITLET_DIR, "blob");
   This directory is a directory that keeps the "blobs" or files that have been
   committed. The blob directory keeps copies of every single file made ever.
   
4. public static final File COMMIT_DIR = join(GITLET_DIR, "log");
   This directory keeps track of all commits by holding every commit file in it.
   The commits will always exist and no commits will be deleted once a commit is made.
   Commit files are just files with a commit class stored in it and their names are always
   the HashCode of their commit file.
5. public static final File HEAD_FILE = join(GITLET_DIR, "headCommit");
   HEAD_FILE keeps track of which branch the user is currently on. To do
   this, it is a file that holds only the name of the branch.
6. public static final File STAGING_DIR = join(GITLET_DIR, "staging");
   STAGING_DIR keeps track of the directory that holds the files that are staged
   for removal. These files are then added to the BLOB_DIR after a commit is
   made.
   
7. public static final File BRANCHES_DIR = join(GITLET_DIR, "branches");
   BRANCHES_DIR keeps track of all the branches made. This is done as it holds
   files that hold pointers to the head commit of the branch.
   
8. public static final File MASTER_FILE = join(BRANCHES_DIR, "master");
   The master file keeps track of the master branch. This is done for ease
   when checking the programming when debugging.
   
9. private static final File POINTER_DIR = join(GITLET_DIR, "tree");
   The pointer director contains files containing a HashMap of all the
   files that a certain commit has. Every commit will have a pointer pointing
   to one file in this directory.

### Class 2
Commit
#### Fields

1. private String message;
   This is the commit message that will help user identify which
   iteration of the file it is as the HashIDs are pretty much 
   unreadable
2. private String timestamp; 
   This is a string that represents what time the commit was created
3. private String parent;
   This is a variable that stores the parent commit, so
   the class can more easily refer to which commit this
   commit instance derives from.
4. private SimpleDateFormat dateFormat = new SimpleDateFormat("EEE MMM d HH:mm:ss yyyy Z");
   This is a variable in DateFormat that stores a format template
   for how the date should be formatted.
   
5. private String filePointer;
   This is a variable that stores the HashCode of the HashMap that contains 
   the names of all the files that associate with this commit.
   
6. private String mergeParent;
   In cases where the commit is a result of a merge, mergeParent
   is a variable that stores the pointer to the second commit that 
   is merged with the parent commit.
   
### Class 3
Main
### Fields
1. The main function of this class is to act as a sort of
   starting location that allows the user to use some of the method
   functions from the repository class.


## Algorithms
1. Repository.java
- init() creates both the GITLET and Blob 
directories while also adding a initial commit into
  the GITLET directory.
-  add adds the file to a staging directory if the file does exists.
The file will not be added if the exact file is already in the staging area
   or if the exact file has already been committed by the parent commit.
   
- commit commits all staged changes. If new files are added, commit
   copies those files into the BLOB_DIR, if files are removed, commit 
  makes sure to remove those files from the HASHMAP and sets the new commit
  to that hashmap.
  
- moveStagedFiles helper function for commits that copies all files in staging
   into the blob directory. The names of the files are the hashcode of its contents
  
- checkout(filename) checks out the file in the head commit and
   places it in the CWD if it exists. If it doesn't exist
  in the current commit, it gives an error.
  
- checkout(filename, commitID) checks out the file in the given commit id and
   places it in the CWD if it exists. Errors if commitID doesn't exist and if
  the file does not exist in the given commit.
  
- branchCheckout checks out all files in a given branch and places the files
   in the CWD. Errors if: the branch doesn't exist, the branch is the current branch
  , and if there are untracked files that would be deleted.
  
- deleteStaged deletes all files in the staging area.

- checkoutHelper is a helper function for the checkout functions that
   do most of the work described in those functions.
  
- shortID is a helper function that checks if the IDs are shortened. If they
   are, matched the shortened id to the closest real id and returns that.
  
- log prints out all commits from the headCommit of the current branch down 
   to the current commit. prints the commit id, the message of the commit, the date the commit was made, and the parent commits if the
  commit was a result of a merge.
  
- global log prints out all commits with the rules of the log function.

- find finds all commit with a certain message. Iterates over the entire
   Commit directory and prints the commitID of all commits that share that 
  message. Errors if no commits with the message are found
  
- rm removes a file from the staging directory hashmap if it is there, or
   places the file in the remove staging hashmap if it exists in the 
  headcommit of the current branch. Errors if the file is already removed or if
  the file has not been committed in the previous head commit.
  
- status displays all branches, staged additions, staged removals, changed files,
   and untracked files in lexical order. Places a "*" behind the branch that
  is the current head branch. Errors if not in a .gitlet directory.
  
- branch creates a branch with the given name at the given headCommit. 
   errors if branchName already exists.
  
- rmBranch removes a branch with the given name. Errors if the branchName does not
   exist or if the removed branch is the current branch.
  
- reset resets the CWD to a previous commit. In other words, it checksout all
   files tracked by the given commit. Errors if there are untracked files that would
  be deleted or if the commit name doesn't exist.
  
- pointerFinder is a helper function that returns the headCommit filePointer hashmap
   given a branch name.
  
- merge merges the headCommit of two branches together. If two files
   from both the headCommits conflict with one another, they are combined together
   with a printed message at the end of the runtime of merge stating that there were 
  conflicts. Conflicts occur when the two files have different contents, and if one of
   the file was deleted while the other file is changed from the split point file.
   The split point is the closest commit that both branches share in their history.
  Errors that may occur include: having uncommitted changes, giving a branchName
  that doesn't exist, merging a branch with itself, having an untracked file that would be 
  deleted, giving a branch that is either the ancestor or the parent of the current branch, and
  all the errors that come with commit.
  
- findSplitPoint finds the split point commit given two branches. the split point
   is the closest commit that is the ancestor of the two given branches. This is found
   by placing all commits from one branch into a Set and using BFS on the second branch to
   find the closest commit that can be found in the Set.
  
- ancestorSet is a helper function to findSplitPoint that finds all ancestor commits. for
   a branch and returns them in a Set.
  
- fileSetCreator is a helper function that places all filenames from the
   hashmaps of the two branches and the split point.
  
- mergeConflict is a helper function that helps deals with conflicting files
   by putting the contents of the two files together and placing it in the staging directory
   file.
  
- checkoutAndAdd is a helper function that checks out the given file and adds it
   to the StagingAdd hashmap.
  
- pointerFromCommitString is a helper function that gives the Hashmap pointer
   for a commit given the commit string.
  
- findCommitFromBranch is a helper function that gives the commit class file 
   given a branch string.
  
- findCommitFromCommitString is a helper function that gives the commit class file
   given a commit string.
  
- mergeStringReturns merges the contents of the two files given and returns it.

   
2. Commit
- Commit() creates a commit and assigns the put in variables to the class variables
    additionally checks to see if this is the initial commit by checking if the parent == null.
  if it is, the date is the UNIX starting time and the message is "initial commit"
  
- getMessage gets the message of the commit
- getTimeStamp gets the timestamp of the commit
- getParent gets the parent string of the commit
- returnPointer gets the hashmap pointer string of the commit
- getMergeParent gets the second parent string of the commit if the commit was a result of a merge.
## Persistence
- The file structure look like
>gitlet
>
> >STAGING_DIR
> >>StagingAdd
> >>
> >>StagingRemove
> >>
> >>CommitsMap
> >>
> >> (Files staged for addition)
> 
> >BRANCHES_DIR
> >>MASTER_FILE
> >>
> >>(Branch Names)
> 
> >COMMIT_DIR
> >>(Commits)
> 
> >BLOB_DIR
> >>(Files that have been committed)
> 
> >POINTER_DIR
>>> (HashMaps that contain filenames as the key and hashcodes of the files
> as pointers)
> 
> >HEAD_FILE
> 
> CWD
1. To create start a Repository, git init is called
   which creates all the directories and files above (execpt for the ones in parentheses);
2. When a file is added, a hashcode of its contents is taken and the filename
   and hashcode is added into the StagingAdd file hashmap which is stored in
   the staging add file.
   
3. When a file is deleted, its name is stored in the StagingRemove file hashmap which
   is stored in the stagingremove file in the Staging directory
   
4. when commit is called, all files are moved from the staged directory to the
   blob directory with the name of the file being the hashcode of its contents. This 
   allows different copies of the same file to be stored in the BLOB_DIRECTOR and be accessed later on.
   Additionally, when the commit file is stored, the file is named the hashcode of the
   commit class for easier access. The current branch file is changed to now have the
   commit file name to access it easier. Additionlly, when creating a commit file, 
   we put the previous commit in the curr branch in the commitID variable which allows
   for tracking like a linked-list.
   
5. when removed is called, we check in the headcommit for the current branch for the HashMap pointer
    that contains all the files that are in the commit. If the file is not there and it is
   not just staged for addition, that's when it knows how to error. Otherwise it just
   adds the filename to the removeStaging hashmap in the removeStaging file to keep track that it was
   removed.
   
6. branches are stored in the branch directory, and each branch file
    is named the name of the branch and contains a string for the commit that is
    at the current head of the branch.
   
7. StagingAdd keeps a hashmap that keeps track of all files added
8. StagingRemove keeps a hashmap that keeps track of all removed files
9. commitsMap keeps a hashmap that keeps track of what the current commit would
    look like with the adds and removes.
   

