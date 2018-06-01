public class FileSystem {
    private SuperBlock superblock;
    private Directory directory;
    private FileTable filetable;
    
    public FileSystem( int diskblocks ) {
        superblock = new SuperBlock( diskBlocks );
        
        directory = new Directory( superblock.InodeBlocks );
        
        filetable= new FileTable( directory );
        
        FileTableEntry dirEnt = open( "/", "r" );
        int dirSize = fsize( dirEnt );
        if( dirSize > 0 ) {
            byte[] dirData = new byte(dirSize);
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
        
    }
    
    int read( FileTableEntry ftEnt ) {
        
    }
    
    int write( FileTableEntry ftEnt, byte[] buffer) {
        
    }
    
    private boolean deallocAllBlocks( FileTableEntry ftEnt ) {
        
    }
    
    boolean delete( String filename ) {
        
    }
    
    private final int SEEK_SET = 0;
    private final int SEEK_CUR = 1;
    private final int SEEK_END = 2;
    
    int seek( FileTableEntry ftEnt, int offset, int whence ) {
        
    }
}