public class FileSystem {
    private SuperBlock superblock;
    private Directory directory;
    private FileTable filetable;
    
    public FileSystem( int diskblocks ) {
        superblock = new SuperBlock( diskblocks );
        
        directory = new Directory( superblock.getMax_INodes() );
        
        filetable= new FileTable( directory );
        
        FileTableEntry dirEnt = filetable.falloc( "/", "r" );
        int dirSize = fsize( dirEnt );
        if( dirSize > 0 ) {
            byte[] dirData = new byte[dirSize];
            read( dirEnt, dirData );
            directory.bytes2directory( dirData );
            close( dirEnt );
        }
    }
    
    void sync(){
        
    }
    
    boolean format( int files ) {
        
    }
    
    int fsize( FileTableEntry ftEnt ) {
        return ftEnt.inode.length;
    }
    
    int read( FileTableEntry ftEnt ) {
        
    }
    
    int write( FileTableEntry ftEnt, byte[] buffer) {
        
    }
    
    private boolean deallocAllBlocks( FileTableEntry ftEnt ) {
        
    }
    
    int delete( String filename ) {
        short fileINumber = directory.namei(filename);
       
       // If the file is open, then:
       if(fileINumber > -1) {
           // Indicate this action can't occur due to the file being open.
           return -1;
       }
       
       directory.ifree(fileINumber);
       return 0;
    }
    
    private final int SEEK_SET = 0;
    private final int SEEK_CUR = 1;
    private final int SEEK_END = 2;
    
    int seek( FileTableEntry ftEnt, int offset, int whence ) {
        //ftEnt.
    }
}