public class TCB {
    private Thread thread = null;
    private int tid = 0;
    private int pid = 0;
    private boolean terminated = false;
    private int sleepTime = 0;
    public FileTableEntry[] ftEnt = null; // added for the file system

    public TCB( Thread newThread, int myTid, int parentTid ) {
	thread = newThread;
	tid = myTid;
	pid = parentTid;
	terminated = false;

	ftEnt = new FileTableEntry[32];    // added for the file system
	for ( int i = 0; i < 32; i++ )
	{
		ftEnt[i] = null;         // all entries initialized to null
	}
        // fd[0], fd[1], and fd[2] are kept null.

	System.err.println( "threadOS: a new thread (thread=" + thread + 
			    " tid=" + tid + 
			    " pid=" + pid + ")");
    }

    public synchronized Thread getThread( ) {
	return thread;
    }

    public synchronized int getTid( ) {
	return tid;
    }

    public synchronized int getPid( ) {
	return pid;
    }

    public synchronized boolean setTerminated( ) {
	terminated = true;
	return terminated;
    }

    public synchronized boolean getTerminated( ) {
	return terminated;
    }

    // added for the file system
    public synchronized int getFd( FileTableEntry entry ) {
	if ( entry == null )
	    return -1;
	for ( int i = 3; i < 32; i++ ) {
	    if ( ftEnt[i] == null ) {
		ftEnt[i] = entry;
		return i;
	    }
	}
	return -1;
    }

    // added for the file system, returns int relevant to file decriptor table
    public synchronized FileTableEntry returnFd( int fd ) {
	if ( fd >= 3 && fd < 32 ) {
	    FileTableEntry oldEnt = ftEnt[fd];
	    ftEnt[fd] = null;
	    return oldEnt;
	}
	else
	    return null;
    }

    // added for the file systme, returns the file table entry, 0, 1, and 2 are reserved
    public synchronized FileTableEntry getFtEnt( int fd ) {
	if ( fd >= 3 && fd < 32 )
	    return ftEnt[fd];
	else
	    return null;
    }
}
