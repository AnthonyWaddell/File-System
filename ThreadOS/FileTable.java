import java.util.Vector;

public class FileTable {
    private Vector<FileTableEntry> table;
    private Directory dir;
    
    public FileTable( Directory directory) {
        table = new Vector<FileTableEntry>();
        dir = directory;
    }
    
    public synchronized FileTableEntry falloc( String filename, String mode) {
        int iNumber = dir.ialloc(filename);
        Inode newNode = new Inode();
        FileTableEntry newEntry;
        
        if(newNode.count == 0) {
            newNode.flag = true;
        }
        newNode.count++;
        
        newNode.toDisk(iNumber);
        newEntry = new FileTableEntry(newNode, iNumber, mode);
        table.add(newEntry);
        
        return newEntry;
    }
    
    public synchronized boolean ffree( FileTabelEntry e) {
        e.Inode.count--;
        if(e.Inode.count == 0) {
            e.Inode.flag = false;
        }
        e.Inode.toDisk(e.iNumber);
        
        int index = -1;
        if((index = table.indexOf(e)) > 0) {
            table.removeElementAt(index);
            return true;
        }
        return false;
    }
    
    public synchronized boolean fempty() {
        return table.isEmpty();
    }
}
