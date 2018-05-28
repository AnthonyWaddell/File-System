//-----------------------------------------------------------------------------
// Upon booting ThreadOS, the file system instantiates the Directory class as 
// the root directory through its constructor, reads the file from the disk 
// that can be found through the inode 0 at 32 bytes of the disk block 1, 
// and initializes the Directory instance with the file contents. Prior to 
// shutdown, the file system must write back the Directory information onto 
// the disk. The methods bytes2directory( ) and directory2bytes will 
// initialize the Directory instance with a byte array read from the disk 
// and converts the Directory instance into a byte array that will be 
// thereafter written back to the disk.
//
// Resources used: 
//  https://docs.oracle.com/javase/7/docs/api/java/lang/String.html#String(byte[],%20int,%20int)
//-----------------------------------------------------------------------------

public class Directory 
{
    private static int maxChars = 30; 		// max characters of each file name
	private final static int NAMELENGTH = 60;
	private final static int BLOCKSIZE = 4; // The size of one block
	
    // Directory entries
    private int fsize[];        // each element stores a different file size.
    private char fnames[][];    // each element stores a different file name.
	int offset = 0;

    // directory constructor
    public Directory(int maxInumber) 
    { 
      fsize = new int[maxInumber];     // maxInumber = max files
      for (int i = 0; i < maxInumber; i++) 
	  {
		  fsize[i] = 0; 				// all file size initialized to 0
	  }		  
      fnames = new char[maxInumber][maxChars];
      String root = "/";                // entry(inode) 0 is "/"
      fsize[0] = root.length( );        // fsize[0] is the size of "/".
      root.getChars(0, fsize[0], fnames[0], 0); // fnames[0] includes "/"
    }

    // assumes data[] received directory information from disk
    // initializes the Directory instance with this data[]
    public void bytes2directory(byte data[]) 
    {
		int i_nums = fsize.length;
		int i_names = fnames.length;
		// Offset begins at 0
		offset = 0;
		for (int i = 0; i < i_nums; i++) // Store the sizes
		{
			fsize[i] = SysLib.bytes2int(data, offset);
			offset += BLOCKSIZE; // increment the offset by size of 1 block
		}
		
		// Get file names
		for(int j = 0; j < i_names; j++)
		{
			String fileName = new String(data, offset, NAMELENGTH);
			fileName.getChars(0, fsize[j], fnames[j], 0);
			offset += NAMELENGTH;
		}
    }

    // converts and return Directory information into a plain byte array
    // this byte array will be written back to disk
    // note: only meaningfull directory information should be converted
    // into bytes.
    public byte[] directory2bytes() 
    {
		int i_nums = fsize.length;
		int i_names = fnames.length;
		int bytes_per_block = (i_nums * BLOCKSIZE);
		// Offset begins at 0
		offset = 0;
		
		// Create a new array to copy to
		byte[] plainByteArray = new byte[bytes_per_block + (i_nums * (NAMELENGTH))];
		
		// Copy the byte data, use SysLib to perform int to bytye conversion
		for(int i = 0; i < i_nums; i++)
		{
			SysLib.int2bytes(fsize[i], plainByteArray, offset);
			offset += BLOCKSIZE;
		}
		
		// Now get the names
		for(int j = 0; j < i_names; j++)
		{
			// Copy the name from the begining of the name, to the length of the name;
			String s_name = new String(fnames[j], 0, fsize[j]);
			// Turn string into bytes
			byte[] byte_string = s_name.getBytes();
			// Copy the string (now in byte_form), to the byte Array
			System.arraycopy(byte_string, 0, plainByteArray, offset, byte_string.length);
			offset += NAMELENGTH;
		}
		return plainByteArray;
    }

    // filename is the one of a file to be created.
    // allocates a new inode number for this filename
    public short ialloc(String filename) 
    {
		short failure = -1;
		short ret_val = 0;
		// Look for an empty spot
		for(short i = 0; i < fsize.length; i++)
		{
			// When an empty spot is found
			if(fsize[i] == 0)
			{
				// Copy characters from name filename to fnames
				fsize[i] = filename.length();
				filename.getChars(0, fsize[i], fnames[i], 0);
				ret_val = i;
				return ret_val;
			}
		}
		return failure;
	}

	// deallocates this inumber (inode number)
    // the corresponding file will be deleted.
    public boolean ifree(short iNumber) 
    {
		boolean ret_val = false;
		// If valid
		if(iNumber > 0)
		{
			fsize[iNumber] = 0;						// release iNode
			fnames[iNumber] = new char[maxChars];	// release filename
			ret_val = true;
			return ret_val;
		}
		else
		{
			return ret_val;
		}
    }

	// returns the inumber corresponding to this filename
    public short namei(String filename) 
    {
		short ret_val = -1;
		// Look over each file 
		for(short i = 0; i < fsize.length; i++)
		{
			// If something is there
			if(fsize[i] > 0)
			{
				// Check the name of the file
				String s_name = new String(fnames[i], 0, fsize[i]);
				// If they are equivalent, return this position
				if(s_name == filename)
				{
					ret_val = i;
					return ret_val;
				}
			}
		}
		return ret_val;
    }
}