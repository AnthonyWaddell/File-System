import java.util.*;
import java.io.*;
//-----------------------------------------------------------------------------
// File system class, with braces perfectly balanced. As all things should be.
//-----------------------------------------------------------------------------

public class FileSystem 
{
    private SuperBlock superblock;
    private Directory directory;
    private FileTable filetable;
    private final int FALSE = -1;
    
    // For seek function    
    private final int SEEK_SET = 0;
    private final int SEEK_CUR = 1;
    private final int SEEK_END = 2;
	
	//-------------------------------------------------------------------------
	// FileSystem constructor provided with slides
	//-------------------------------------------------------------------------
    public FileSystem( int diskblocks ) 
    {
        superblock = new SuperBlock( diskblocks );
        
        directory = new Directory( superblock.totalInodes );
        
        filetable = new FileTable( directory );
        
        FileTableEntry dirEnt = open( "/", "r" );
        int dirSize = fsize( dirEnt );
        if( dirSize > 0 ) {
            byte[] dirData = new byte[dirSize];
            read( dirEnt, dirData );
            directory.bytes2directory( dirData );
        }
        close( dirEnt );
    }
    
	//-------------------------------------------------------------------------
	// Sync file system to disk by writing directory to disk in bytes. 
	//-------------------------------------------------------------------------
    public void sync()
    {
	byte[] databuf = directory.directory2bytes();
	// Need write() function~~
        
    }
    
	//-------------------------------------------------------------------------
	// Basically just starts everything over. Superblock handles most of the
	// reformat work. 
	//-------------------------------------------------------------------------
    public boolean format(int files) 
    {
        boolean ret_val = true;
        // Validity check
        if(files < 0)
        {
            ret_val = false;
            return ret_val;
        }
        // Let this wipe everything
        superblock.format(files);
        // New directory
        directory = new Directory(superblock.totalInodes);
        // Now store the new directory in FileTable
        filetable = new FileTable(directory);
        return ret_val;
    }
    
    //-------------------------------------------------------------------------
    // Open a file
    //-------------------------------------------------------------------------
    public FileTableEntry open(String filename, String mode)
    {
        FileTableEntry f_entry = filetable.falloc(filename, mode);
        return f_entry;
    }

    //-------------------------------------------------------------------------
    // Close file relevant to FileTableEntry ftEnt. 
    //-------------------------------------------------------------------------
    public synchronized boolean close(FileTableEntry ftEnt)
    {
		boolean ret_val = true;
		// One less thread accessing file
        ftEnt.count--;
		// If no other thread is accessing this entry
		if(ftEnt.count == 0)
		{
			// Remove the entry from the file table
			ret_val = filetable.ffree(ftEnt);
			return ret_val;
		}
		return true;
    }
    //-------------------------------------------------------------------------
    // Get the size of file as contained in the file table entry
    //-------------------------------------------------------------------------
    public synchronized int fsize( FileTableEntry ftEnt ) 
    {
        return ftEnt.inode.length;
    }
    
    //-------------------------------------------------------------------------
    // Reads if passed in the right mode. If block is valid, will read from 
	// block. Reads in loops based on the remaining size of the buffer. 
    //-------------------------------------------------------------------------
    public int read(FileTableEntry ftEnt, byte[] buffer) 
    {
        int entry_size = fsize(ftEnt);
        int buf_length = buffer.length;
        int readCount = 0;
		int ret_val = 0;
        int blockCount;
		int remainingBlocks;

        // Entry mode should only be "r" for read
        if(ftEnt.mode.toLowerCase() == "w" || ftEnt.mode.toLowerCase() == "a" || ftEnt.mode == "w+")
        {
			// Return -1 for error
            return FALSE;
        }
		synchronized(ftEnt)
		{
			// Start reading 
			while(ftEnt.seekPtr < entry_size && buf_length > 0)
			{
				// Figure out block to read from
				int readBlock = ftEnt.inode.getIdFromSeekPointer(ftEnt.seekPtr);
				// Validity check, exit check
				if(readBlock == -1)
				{
					break;
				}
				
				// First, Spin up a buffer to read into
				byte[] readbuf = new byte[Disk.blockSize];
				SysLib.rawread(readBlock, readbuf);
				
				// Next, get the offset into the data block
				int beginning = (ftEnt.seekPtr % Disk.blockSize);
				blockCount = (Disk.blockSize - readCount);
				remainingBlocks = (fsize(ftEnt) - ftEnt.seekPtr);
				
				// Amount read should be less than the size of the file, the size of the block
				// we are reading into, and what's left in buffer space
				readCount = blockCount < remainingBlocks ? blockCount : remainingBlocks;
				if(buf_length < readCount)
				{
					readCount = buf_length;
				}
				System.arraycopy(readbuf, beginning, buffer, ret_val, readCount);
				
				// Shift where we are reading, how far we have read, and how much room we have left
				// by the amount we have read
				ftEnt.seekPtr += readCount;
				ret_val += readCount;
				entry_size -= readCount;
			
			}
			//when finished reading
			return ret_val;
		}
    }
    
    //-------------------------------------------------------------------------
    // Writes from the buffer to the file entry. Starts with seekptr and as 
	// bytes are written will update the seekptr of the file table entry.
    //-------------------------------------------------------------------------
    public int write( FileTableEntry ftEnt, byte[] buffer) 
    {
		int entry_size = fsize(ftEnt);
		int write_bytes = 0;
		int buf_length = buffer.length;
		
		// Fail if mode is read pls
        if(ftEnt.mode.toLowerCase() == "r")
		{
			return FALSE;
		}
		
		// Lock this up pls
		synchronized(ftEnt)
		{
			
		}
	    return 0; // broken return statement
    }
    
    //-------------------------------------------------------------------------
    //
    //-------------------------------------------------------------------------
    private boolean deallocAllBlocks(FileTableEntry ftEnt) 
    {
		boolean ret_val = true;
		// Get the number of direct blocks
		//int ft_direct_size = ftEnt.inode.directSize;
		// Allocate data buffer for data pointed to by indirect block
		byte[] databuf = new byte[Disk.blockSize];
		short blockCounter = 0;
		
		// Loop over direct blocks
		for(int i = 0; i < ftEnt.inode.directSize; i++)
		{
			// If they contain anything
			if(ftEnt.inode.direct[i] != FALSE)
			{
				// Return that block to free list
				superblock.returnBlock(i);
				// And set the inode direct block back to -1
				ftEnt.inode.direct[i] = FALSE;
			}
		}
		
		// Now check the indirect block
		if(ftEnt.inode.indirect >= 0)
		{
			// If it is being used, read the data into a buffer
			SysLib.rawread(ftEnt.inode.indirect, databuf);
			// and set indirect back to -1
			ftEnt.inode.indirect = FALSE;
		}
		// While there is data to return
		do
		{
			// Get data as short
			blockCounter = SysLib.bytes2short(databuf, 0);
			if(blockCounter != FALSE)
			{
				// Return that block to free list
				superblock.returnBlock(blockCounter);
			}
			
		}while(blockCounter != FALSE);
		
		// When finished, write back to disk
		ftEnt.inode.toDisk((short)ftEnt.iNumber);
		
        return ret_val;
    }
    
    //-------------------------------------------------------------------------
    // Once we get write handled, probably just all open on a new ftEnt, 
	// have directory call ifree on that ftEnt's inumber and close the file. boom
    //-------------------------------------------------------------------------
    public synchronized int delete(String filename) 
    {
        short fileINumber = directory.namei(filename);
       
        // If the file is open, then:
        if(fileINumber > -1) {
           // Indicate this action can't occur due to the file being open.
           return -1;
        }
       
        directory.ifree(fileINumber);
        return 0;
    }
    
    //-------------------------------------------------------------------------
    //  For navigating inside file
    //-------------------------------------------------------------------------
    int seek(FileTableEntry ftEnt, int offset, int whence) 
    {
        // Get the original file size
        int file_size = fsize(ftEnt);

        switch(whence)
        {
            // If beginning of file
            case SEEK_SET:
                ftEnt.seekPtr = offset;
                break;
            // If current position in file
            case SEEK_CUR:
                ftEnt.seekPtr += offset;
                break;
            // If end of file
            case SEEK_END:
                ftEnt.seekPtr = (file_size + offset);
                break;
            default:
                SysLib.cerr("Error: invalid seek state\n");
                return FALSE;
        }
        // Pretty sure we just return this?
        return ftEnt.seekPtr;
    }
}
