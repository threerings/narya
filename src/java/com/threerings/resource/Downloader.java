//
// $Id: Downloader.java,v 1.4 2004/06/16 09:44:23 mdb Exp $

package com.threerings.resource;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLConnection;

import com.threerings.resource.DownloadManager.DownloadDescriptor;
import com.threerings.resource.DownloadManager.DownloadObserver;

/**
 * Does something extraordinary.
 */
public abstract class Downloader
{
    /**
     * Provides this downloader with a reference to the download
     * descriptor it needs to do its business.
     */
    public void init (DownloadDescriptor ddesc)
    {
        _desc = ddesc;
        _desc.destFileSize = _desc.destFile.length();
        _desc.destLastModified = _desc.destFile.lastModified();
    }

    /**
     * Returns a reference to the descriptor we are downloading.
     */
    public DownloadDescriptor getDescriptor ()
    {
        return _desc;
    }

    /**
     * Requests that this downloader determine whether it needs to be
     * updated and if so, it should add its total download requirement to
     * {@link ProgressInfo#totalSize} the supplied {@link ProgressInfo}
     * record.
     *
     * @return true if this downloader needs be updated, false if not.
     */
    public abstract boolean checkUpdate (ProgressInfo info)
        throws IOException;

    /**
     * Compares the supplied size and last modified time with the size and
     * last modified time of our local copy of the file.
     */
    protected boolean compareWithLocal (long fileSize, long lastModified)
    {
        // check to see whether we have a local copy at all, and if so,
        // whether its file size or last-modified time differs from the
        // source file modulo a delta to account for inexactness on the
        // part of the file-system (thanks Microsoft)
        return ((!_desc.destFile.exists()) ||
                (_desc.destFileSize != fileSize) ||
                (_desc.destLastModified < (lastModified - DELTA)) ||
                (_desc.destLastModified > (lastModified + DELTA)));
    }

    /**
     * Processes a single download descriptor.
     */
    public void processDownload (DownloadManager dmgr, DownloadObserver obs,
                                 ProgressInfo pinfo, byte[] buffer)
        throws IOException
    {
        // download the resource bundle from the specified URL
        URLConnection ucon = _desc.sourceURL.openConnection();

        // prepare to read the data from the URL into the cache file
        Log.info("Downloading file [url=" + _desc.sourceURL + "].");
        downloadContent(dmgr, obs, pinfo, buffer, ucon, _desc.destFile);

        // if we have a last modified time, we want to adjust our cache
        // file accordingly
        if (_desc.lastModified != 0) {
            if (!_desc.destFile.setLastModified(_desc.lastModified)) {
                Log.warning("Failed to set last-modified date " +
                            "[file=" + _desc.destFile + "].");
            }
        }
    }

    /**
     * Called after the download phase has completed to allow patching or
     * other post-download activities.
     */
    public void postDownload (DownloadManager dmgr, DownloadObserver obs,
                              ProgressInfo pinfo)
        throws IOException
    {
        // nothing to do by default
    }

    /**
     * Downloads the content from the supplied URL connection into the
     * specified destination file, informing the supplied download manager
     * and observer of our progress along the way.
     */
    protected void downloadContent (DownloadManager dmgr, DownloadObserver obs,
                                    ProgressInfo pinfo, byte[] buffer,
                                    URLConnection ucon, File destFile)
        throws IOException
    {
        InputStream in = ucon.getInputStream();
        FileOutputStream out = new FileOutputStream(destFile);
        int read;

        // TODO: look to see if we have a download info file containing
        // info on potentially partially downloaded data; if so, use a
        // "Range: bytes=HAVE-" header.

        // if we were unable to determine our content length, record a
        // single "byte" of progress to indicate that we've started to
        // download this file
        if (_contentLength <= 0) {
            pinfo.currentSize += 1;
        }

        // read in the file data
        while ((read = in.read(buffer)) != -1) {
            // write it out to our local copy
            out.write(buffer, 0, read);

            // if we know we added something to the total download size,
            // then report our progress to the download observer as a
            // percentage of the total file data to be transferred
            if (_contentLength > 0) {
                pinfo.currentSize += read;
            }

            // update our percent completion; if we're totally done, hold
            // off on notifying the observer until the download manager
            // finishes fiddling to ensure that any action the observer
            // may take on the downloaded files can be safely undertaken
            int pctdone = pinfo.getPercentDone();
            pinfo.complete = (pctdone >= 100);
            if (pinfo.complete) {
                continue;
            }

            // update the transfer rate to reflect the bit of data we just
            // transferred
            long now = System.currentTimeMillis();
            pinfo.updateXferRate(now);

            // notify the progress observer if it's been sufficiently long
            // since our last notification
            if ((now - pinfo.lastUpdate) >= UPDATE_DELAY) {
                pinfo.lastUpdate = now;
                long remaining = pinfo.getXferTimeRemaining();
                dmgr.notifyProgress(obs, pctdone, remaining);
            }
        }

        // close the streams
        in.close();
        out.close();
    }

    /** The descriptor that we're downloading. */
    protected DownloadDescriptor _desc;

    /** We need this to cope gracefully with a missing content-length. */
    protected long _contentLength;

    /** The last-modified difference in milliseconds allowed between the
     * source and destination file without considering the destination
     * file to be out of date. */
    protected static final long DELTA = 5000L;

    /** The delay in milliseconds between notifying progress observers of
     * file download progress. */
    protected static final long UPDATE_DELAY = 2500L;
}
