//
// $Id: ProgressInfo.java,v 1.1 2003/08/05 01:33:20 mdb Exp $

package com.threerings.resource;

/**
 * Used to track download progress.
 */
public class ProgressInfo
{
    /** The total file size in bytes to be transferred. */
    public long totalSize;

    /** The file size in bytes transferred thus far. */
    public long currentSize;

    /** The time at which the file transfer began. */
    public long start;

    /** The current transfer rate in bytes per second. */
    public long bytesPerSecond;

    /** The time at which the last progress update was posted to the
     * progress observer. */
    public long lastUpdate;

    /** Whether the download has completed and the progress observer
     * notified. */
    public boolean complete;

    /**
     * Returns the percent completion, based on data size transferred,
     * of the file transfer.
     */
    public int getPercentDone ()
    {
        return (int)((currentSize / (float)totalSize) * 100f);
    }

    /**
     * Updates the bytes per second transfer rate for the download
     * associated with this progress info record.
     */
    public void updateXferRate (long now)
    {
        long secs = (now - start) / 1000L;
        bytesPerSecond = (secs == 0) ? 0 : (currentSize / secs);
    }

    /**
     * Returns the estimated transfer time remaining for the download
     * associated with this progress info record, or <code>-1</code> if
     * the transfer time cannot currently be estimated.
     */
    public long getXferTimeRemaining ()
    {
        return (bytesPerSecond == 0) ? -1 :
            (totalSize - currentSize) / bytesPerSecond;
    }
}
