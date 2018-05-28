//-----------------------------------------------------------------------------
// Each inode describes one file. Our inode is a simplified version of the Unix
// inode. It includes 12 pointers of the index block. The first 11 of these 
// pointers point to direct blocks. The last pointer points to an indirect 
// block. In addition, each inode must include (1) the length of the 
// corresponding file, (2) the number of file (structure) table entries that 
// point to this inode, and (3) the flag to indicate if it is unused (= 0), 
// used(= 1), or in some other status (= 2, 3, 4, ...). 16 inodes can be 
// stored in one block.
//-----------------------------------------------------------------------------

public class Inode 
{
   private final static int iNodeSize = 32;       // fix to 32 bytes
   private final static int directSize = 11;      // # direct pointers
   private final static int shortSize = 2;
   private final static int intSize = 4;

   public int length;                             // file size in bytes
   public short count;                            // # file-table entries pointing to this
   public short flag;                             // 0 = unused, 1 = used, ...
   public short direct[] = new short[directSize]; // direct pointers
   public short indirect;                         // a indirect pointer

   // a default constructor
   Inode() 
   {                                     
      length = 0;
      count = 0;
      flag = 1;
      for (int i = 0; i < directSize; i++)
	  {
		  direct[i] = -1;
	  }
      indirect = -1;
   }

   //--------------------------------------------------------------------------
   // Given an inode number, termed inumber, this constructor reads the
   // corresponding disk block, locates the corresponding inode information in 
   // that block, and initializes a new inode with this information.
   //--------------------------------------------------------------------------
   Inode(short iNumber) 
   {
	   // 16 INodes can be stored in one block
	   int blockNumber = iNumber / 16; 
	   byte[] iData = new byte[Disk.blockSzie];
	   // iNode data requires 32 bytes total,
	   // offset should begin where node begins on block
	   int offset = (iNumber % 16) * iNodeSize;
	   
	   // Read data from disk
	   SysLib.rawread(blockNumber, iData); 
	   length = SysLlib.bytes2int(iData, offset); // 4 bytes for length
	   offset += intSize;
	   count = SysLib.bytes2short(iData, offset); // 2 bytes for count
	   offset += shortSize;
	   flags = SysLib.bytes2short(iData, offset); // 2 bytes for flags
	   offset += shortSize;
	   
	   // First 11 pointers point to index block
	   for(int i = 0; i < directSize; i++)
	   {
		   direct[i] = SysLib.bytes2short(iData, offset) // 22 bytes for direct
		   offset += shortSize;
	   }
	   // Last pointer points to an indirect block
	   indirect = SysLib.bytes2short(iData, offset);	 // 2 bytes for indirect, 32 bytes total.
   }

    // save to disk as the i-th inode
   int toDisk(short iNumber) 
   {                 
       // 16 INodes can be stored in one block
	   int blockNumber = (iNumber / 16); 
	   blockNumber += 1;
	   byte[] iData = new byte[Disk.blockSzie];
	   // iNode data requires 32 bytes total,
	   // offset should begin where node begins on block
	   int offset = (iNumber % 16) * iNodeSize;
	   
	   SysLib.int2bytes(length, iData, offset);
	   offset += intSize;
	   SysLib.short2bytes(count, iData, offset);
	   offset += shortSize;
	   SysLib.short2bytes(flag, iData, offset);
	   
	   // First 11 pointers point to index block
	   for(int i = 0; i < directSize; i++)
	   {
		   SysLib.short2bytes(direct[i], iData, offset);
		   offset += shortSize;
	   }
	   // Last pointer points to an indirect block
	   SysLib.short2bytes(indirect, iData, offset);
	   
	   // Write back to disk
	   if(!SysLib.rawwrite(blockNumber, iData))
	   {
		   return -1;
	   }
	   else return 0;
   }
}