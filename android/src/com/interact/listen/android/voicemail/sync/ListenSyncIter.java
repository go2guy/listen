package com.interact.listen.android.voicemail.sync;

import java.util.Iterator;

public class ListenSyncIter< T extends Comparable<T> >
{
    private Iterator<T> localIter;
    private Iterator<T> serverIter;

    private T lastLocal;
    private T lastServer;
    
    public ListenSyncIter(Iterator<T> localIter, Iterator<T> serverIter)
    {
        reset(localIter, serverIter);
    }
    
    public void reset(Iterator<T> lIter, Iterator<T> sIter)
    {
        this.localIter = lIter;
        this.serverIter = sIter;
        
        this.lastLocal = null;
        this.lastServer = null;
    }

    public boolean next()
    {
        if(isMissingFromLocal())
        {
            lastServer = nextIter(serverIter);
        }
        else if(isMissingFromServer())
        {
            lastLocal = nextIter(localIter);
        }
        else // isMatched() or both null
        {
            lastServer = nextIter(serverIter);
            lastLocal = nextIter(localIter);
        }
        
        return lastLocal != null || lastServer != null;
    }
    
    public boolean nextMatch()
    {
        while(next() && lastLocal != null && lastServer != null)
        {
            if(isMatched())
            {
                return true;
            }
        }
        return false;
    }

    public T getLocal()
    {
        return lastLocal;
    }
    public T getServer()
    {
        return lastServer;
    }
    
    public boolean isMissingFromLocal()
    {
        return lastServer != null && (lastLocal == null || lastLocal.compareTo(lastServer) > 0);
    }
    
    public boolean isMissingFromServer()
    {
        return lastLocal != null && (lastServer == null || lastServer.compareTo(lastLocal) > 0);
    }

    public boolean isMatched()
    {   // isMatched() == (!isMissingFromLocal() && !isMissingFromServer())
        return lastLocal == null || lastServer == null ? false : lastLocal.compareTo(lastServer) == 0;
    }
    
    private T nextIter(Iterator<T> iter)
    {
        return iter.hasNext() ? iter.next() : null;
    }
}
