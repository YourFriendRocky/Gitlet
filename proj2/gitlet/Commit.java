package gitlet;



import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/** Represents a gitlet commit object.
 *
 *  does at a high level.
 *
 *  @author
 */
public class Commit implements Serializable {
    /**
     *
     *
     * List all instance variables of the Commit class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided one example for `message`.
     */

    /** The message of this Commit. */
    private String message;
    /** This is the the time when the commit was created*/
    private String timestamp;
    /** This is the parent Commit that this commit derives from*/
    private String parent;
    /** This is just the format of the date Used throughout the code*/
    //(ex: Mon Mar 1 13:40:45 2021 -0800)
    private SimpleDateFormat dateFormat = new SimpleDateFormat("EEE MMM d HH:mm:ss yyyy Z");
    /** This string points to the HashMap containing all the file pointers.*/
    private String filePointer;
    /** This string points to the HashMap containing all the files staged to be removed.*/
    private String mergeParent;


    /** Initializes a commit object and assigns a parent and a message to it
     * Additionally check the parent set of file pointers and copies it*/

    public Commit(String message, String parent, String filePointer, String mergeParent) {
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC")); //Sets the timezone to UTC time
        if (message.equals("")) {
            System.out.println("Please enter a commit message.");
            return;
        }
        this.message = message;
        this.parent = parent;
        this.filePointer = filePointer;
        this.mergeParent = mergeParent;
        if (parent == null) {
            //Date Unix = new Date(0); (In case I need this)
            timestamp = dateFormat.format(new Date(0));
        } else {
            //Date currTime = new Date(); (In case I need this)
            timestamp = dateFormat.format(new Date());
        }
    }

    //getMessage returns the message of the commit
    public String getMessage() {
        return message;
    }

    //getTimestamp returns the timestamp of the commit
    public String getTimestamp() {
        return timestamp;
    }

    //getParent returns the parent Commit of this commit
    public String getParent() {
        return parent;
    }

    //returnPointer returns the file pointer
    public String returnPointer() {
        return filePointer;
    }

    //returnRemovePointer returns the file remove pointer
    public String getMergeParent() {
        return mergeParent;
    }

}
