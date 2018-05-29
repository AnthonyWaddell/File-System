//-----------------------------------------------------------------------------
// The first disk block, block 0, is called the superblock. It is used to 
// describe:
//
//  1: The number of disk blocks.
//  2: The number of inodes.
//  3: The block number of the head block of the free list.
// 
// It is the OS-managed block. No other information must be recorded in and no
// user threads must be able to get access to the superblock.
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Superblock class
//-----------------------------------------------------------------------------
public class SuperBlock 
{
   public int totalBlocks; // the number of disk blocks
   public int totalInodes; // the number of inodes
   public int freeList;    // the block number of the free list's head
   private final static int MAX_INODES = 64;	// Max number of INodes/files
   private final static int MAX_BLOCKS = 1000;	// Max number of blocks
   
   //--------------------------------------------------------------------------
   // Basic constructor
   //--------------------------------------------------------------------------
    public SuperBlock(int diskSize) 
    {
	    byte[] super_block = new byte[Disk.blockSize]; // Create new block
	    SysLib.rawread(0, super_block);				   // Read from disk
	    totalBlocks = SysLib.bytes2int(super_block, 0);// Initialize # blocks
	    totalInodes = SysLib.bytes2int(super_block, 4);// Initialize # iNodes
	    freeList = SysLib.bytes2int(super_block, 8);   // Initialize freeList
	    if(totalInodes <= 0 || totalBlocks != diskSize)// Validate
	    {
		    totalBlocks = diskSize;
		    format(MAX_INODES);
	    }
	    else
	    {
			return;
	    }
    }
   
    //-------------------------------------------------------------------------
	// Sync to disk by writing back the total number of disk blocks, inodes, 
	// and the freeList
	//-------------------------------------------------------------------------
	public void sync()
	{
	   // Create byte array to write to disk with
		byte[] super_block = new byte[Disk.blockSize];
		SysLib.int2bytes(totalBlocks, super_block, 0);	// Write back total number of disk blocks
		SysLib.int2bytes(totalInodes, super_block, 4);	// Write back total number of Inodes
		SysLib.int2bytes(freeList, super_block, 8);		// Write back freeList
		SysLib.rawwrite(0, super_block);				// Write to disk
	}
   
	//-------------------------------------------------------------------------
	// If initialization goes wrong, super block will wipe the disk of data
	// and start over with reformatting with default 4 blocks/64 iNodes.
	//-------------------------------------------------------------------------
	public void format(int fileCount)
	{
		byte[] temp = null;
		int total_iNodes = fileCount;
		int free_files = (fileCount / 16) + 2; 	// Freelist head begins after 2
		for(short i = 0; i < fileCount; i++)// For the max amount of files/iNodes
		{
			Inode temp_node = new Inode();	// Creat a new Inodes
			temp_node.flag = 0;				// Set flag to 0
			temp_node.toDisk(i);			// Write to disk as the i-th node
		}
		
		for(int j = free_files; j < MAX_BLOCKS; j++)
		{
			temp = new byte[Disk.blockSize]; // Create a new empty block
			for(int k = 0; k < Disk.blockSize; k++)
			{
				temp[k] = 0;				 // Fill with nothing
			}
			SysLib.int2bytes(j + 1, temp, 0);// Point to next free block space
			SysLib.rawwrite(j, temp);		 // Write back to disk
		}
		SysLib.int2bytes(-1, temp, 0);		 // Last block points to nothing
		SysLib.rawwrite(MAX_BLOCKS - 1, temp);
		sync();								 // Now sync, create new superblock
	}
	

	
	
	
	
	
	
	
}