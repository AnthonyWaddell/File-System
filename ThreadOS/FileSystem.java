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
        
        directory = new Directory( superblock.getMax_INodes() );
        
        filetable= new FileTable( directory );
        
        FileTableEntry dirEnt = filetable.open( "/", "r" );
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
		bool ret_val = true;
		// Validity check
		if(files < 0)
		{
			ret_val = false;
			return ret_val;
		}
		// Let this wipe everything
        superblock.format(files);
		// New directory
		directory new Directory(superblock.totalInodes);
		// Now store the new directory in FileTable
		fileTable = new FileTable(directory);
		return ret_val;
    }
    
	//-------------------------------------------------------------------------
	//
	//-------------------------------------------------------------------------
	public FileTableEntry open(String filename, String mode)
	{
		FileTableEntry f_entry = filetable.falloc(filename, mode);
		return f_entry;
	}
	
	//-------------------------------------------------------------------------
	//
	//-------------------------------------------------------------------------
	public boolean close(FileTableEntry ftEnt)
	{
		
	}
	//-------------------------------------------------------------------------
	//
	//-------------------------------------------------------------------------
    public int fsize( FileTableEntry ftEnt ) 
	{
        return ftEnt.inode.length;
    }
    
	//-------------------------------------------------------------------------
	//
	//-------------------------------------------------------------------------
    public int read(FileTableEntry ftEnt, byte[] buffer) 
	{
		int entry_size = fsize(ftEnt);
		int buf_length = buffer.length;
		int readCount = 0;
		int blockCount;
		
		// Entry mode should only be "r" for read
		if(ftEnt.mode.toLowerCase() == "w" || ftEnt.mode.toLowerCase() == "a" || ftEnt.mode == "w+")
		{
			return -1
		}
		// Begining at 0, start reading 
		while(ftEnt.seekPtr < entry_size && buf_length > 0)
		{
			// Figure out block to read from
			int readBlock = ftEnt.inode.getIdFromSeekPointer(ftEnt.seekPtr);
			// Validity check
			if(readBlock == -1)
			{
				return FALSE;
			}
			// Load up a buffer to read into
			byte[] readbuf = new byte[Disk.blockSize];
			SysLib.rawread(readBlock, readbuf);
			// Get the offset into the data
			int beginning = (ftEnt.seekPtr % Disk.blockSize);
			blockCount = Disk.blockSize - readCount;
			
			
			// keep whittling away at reading blocks, will firgure rest out later
		}
		
        
    }
    
	//-------------------------------------------------------------------------
	//
	//-------------------------------------------------------------------------
    public int write( FileTableEntry ftEnt, byte[] buffer) 
	{
        
    }
    
	//-------------------------------------------------------------------------
	//
	//-------------------------------------------------------------------------
    private boolean deallocAllBlocks( FileTableEntry ftEnt ) 
	{
        
    }
    
	//-------------------------------------------------------------------------
	//
	//-------------------------------------------------------------------------
    int delete(String filename) 
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
				break;
		}
		// Pretty sure we just return this?
		return ftEnt.seekPtr;
    }
}
