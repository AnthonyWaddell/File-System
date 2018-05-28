import java.util.Vector;

public class FileTable {
    private Vector<FileTableEntry> table;
    private Directory dir;
    
    public FileTable( Directory directory) {
        table = new Vector<FileTableEntry>();
        dir = directory;
    }
    
    public synchronized FileTableEntry falloc( String filename, String mode) {
        // Allocate an Inode number for the passed filename.
        int iNumber = dir.ialloc(filename);
        // Instantiate a new Inode instance to repersent the Inode on disk.
        Inode newNode = new Inode();
        // The FileTableEntry of the file.
        FileTableEntry newEntry;
        
        // If the Inode on disk isn't being accessed by any other threads, then:
        if(newNode.count == 0) {
            // Indicate the file is being accessed.
            newNode.flag = true;
        }
        // Update the amount of threads accessing the file.
        newNode.count++;
        
        // Write the representation of the Inode to disk version.
        newNode.toDisk(iNumber);
        // Instantiate a new FileTableEntry, passing in the new Inode, its
        //     number, and the mode of the file.
        newEntry = new FileTableEntry(newNode, iNumber, mode);
        // Add the new FileTableEntry to the file table.
        table.add(newEntry);
        
        // Return the new FileTableEntry.
        return newEntry;
    }
    
    public synchronized boolean ffree( FileTableEntry e) {
        int index = -1;

        // Update the amount of threads accessing the file.
        e.Inode.count--;
        // If there are no threads accessing the file, then:
        if(e.Inode.count == 0) {
            // Indicate the file is NOT being accessed.
            e.Inode.flag = false;
        }
        // Write the representation of the Inode to disk version.
        e.Inode.toDisk(e.iNumber);
        
        // If the given FileTableEntry is in the file table, then:
        if((index = table.indexOf(e)) > 0) {
            // Remove it from the file table.
            table.removeElementAt(index);
            // Indicate that the passed FileTableEntry was found and removed.
            return true;
        }
        // Indicate that the passed FileTableEntry was NOT found.
        return false;
    }
    
    public synchronized boolean fempty() {
        // Return whether there are any entries in the file table.
        return table.isEmpty();
    }
}
