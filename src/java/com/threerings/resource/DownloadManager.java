//
// $Id: DownloadManager.java,v 1.7 2003/05/27 07:51:49 mdb Exp $

package com.threerings.resource;

import java.awt.EventQueue;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

import java.util.ArrayList;
import java.util.List;

import com.samskivert.util.Queue;
import com.samskivert.util.StringUtil;

/**
 * Manages the asynchronous downloading of files that are usually located
 * on an HTTP server but that may also be located on the local filesystem.
 */
public class DownloadManager
{
    /**
     * Provides facilities for notifying an observer of file download
     * progress.
     */
    public interface DownloadObserver
    {
        /**
         * If this method returns true the download observer callbacks
         * will be called on the AWT thread, allowing the observer to do
         * things like safely update user interfaces, etc. If false, it
         * will be called on the download thread.
         */
        public boolean notifyOnAWTThread ();

        /**
         * Called when the download manager is about to check all
         * downloads to see whether they are in need of an update.
         */
        public void resolvingDownloads ();

        /**
         * Called to inform the observer of ongoing progress toward
         * completion of the overall downloading task.  The caller is
         * guaranteed to get at least one call reporting 100% completion.
         *
         * @param percent the percent completion, in terms of total file
         * size, of the download request.
         * @param remaining the estimated download time remaining in
         * seconds, or <code>-1</code> if the time can not yet be
         * determined.
         */
        public void downloadProgress (int percent, long remaining);

        /**
         * Called after the download has completed on the download manager
         * thread, immediately after reporting download progress of 100%.
         */
        public void postDownloadHook ();

        /**
         * Called on the preferred notification thread after the download
         * is complete and the post-download hook has run to completion.
         */
        public void downloadComplete ();

        /**
         * Called if a failure occurs while checking for an update or
         * downloading a file.
         *
         * @param desc the file that was being downloaded when the error
         * occurred, or <code>null</code> if the failure occurred while
         * resolving downloads.
         * @param e the exception detailing the failure.
         */
        public void downloadFailed (DownloadDescriptor desc, Exception e);
    }

    /**
     * Describes a single file to be downloaded.
     */
    public static class DownloadDescriptor
    {
        /** The URL from which the file is to be downloaded. */
        public URL sourceURL;

        /** The destination file to which the file is to be written. */
        public File destFile;

        /** The last-modified timestamp of the source file. */
        public long lastModified;

        /** The size in bytes of the source files. */
        public long fileSize;

        /** The last-modified timestamp of the destination file. */
        public long destLastModified;

        /** The size in bytes of the destination file. */
        public long destFileSize;

        /**
         * Constructs a download descriptor to retrieve the given file
         * from the given URL.
         */
        public DownloadDescriptor (URL url, File file)
        {
            this.sourceURL = url;
            this.destFile = file;
        }

        /** Returns a string representation of this instance. */
        public String toString ()
        {
            return StringUtil.fieldsToString(this);
        }
    }

    /**
     * Downloads the supplied list of descriptors, notifying the given
     * download observer of download progress and status.
     *
     * @param descriptors the list of files to be downloaded.
     * @param fragile if true, reports failure and ceases any further
     * downloading if an error occurs; else will continue attempting to
     * download the remainder of the descriptors.
     * @param downloadObs the observer to notify of progress, success and
     * failure.
     */
    public void download (
        List descriptors, boolean fragile, DownloadObserver downloadObs)
    {
        // add the download request to the download queue
        DownloadRecord dlrec = new DownloadRecord();
        dlrec.descriptors = descriptors;
        dlrec.obs = downloadObs;
        dlrec.fragile = fragile;
        _dlqueue.append(dlrec);

        synchronized (this) {
            // if we've not yet got our downloading thread...
            if (_dlthread == null) {
                // create the thread
                _dlthread = new Thread() {
                    public void run () {
                        processDownloads();
                    }
                };
                _dlthread.setDaemon(true);
                // and start it going
                _dlthread.start();
            }
        }
    }

    /**
     * Called by the download thread to process all download requests in
     * the download queue.
     */
    protected void processDownloads ()
    {
        // create the data buffer to be used when reading files
        _buffer = new byte[BUFFER_SIZE];

        DownloadRecord dlrec;
        while (true) {
            synchronized (this) {
                // kill the download thread if we have no remaining
                // download requests
                if (_dlqueue.size() == 0) {
                    _dlthread = null;
                    // free up the data buffer
                    _buffer = null;
                    return;
                }

                // pop download off the queue
                dlrec = (DownloadRecord)_dlqueue.getNonBlocking();
            }

            // handle the download request
            processDownloadRequest(dlrec.descriptors, dlrec.obs, dlrec.fragile);
        }
    }

    /**
     * Processes a single download request.
     */
    protected void processDownloadRequest (
        List descriptors, final DownloadObserver obs, boolean fragile)
    {
        // let the observer know that we're about to resolve all files to
        // be downloaded
        if (obs.notifyOnAWTThread()) {
            EventQueue.invokeLater(new Runnable() {
                public void run () {
                    obs.resolvingDownloads();
                }
            });
        } else {
            obs.resolvingDownloads();
        }

        // check the size and last-modified information for each file to
        // ascertain whether our local copy needs to be refreshed
        ArrayList fetch = new ArrayList();
        ProgressInfo pinfo = new ProgressInfo();
        int size = descriptors.size();
        for (int ii = 0; ii < size; ii++) {
            DownloadDescriptor desc = (DownloadDescriptor)descriptors.get(ii);

            try {
                // get the source file information
                desc.fileSize = 0;
                desc.lastModified = 0;
                String protocol = desc.sourceURL.getProtocol();
                if (protocol.equals("file")) {
                    // read the file information directly from the file system
                    File tfile = new File(desc.sourceURL.getPath());
                    desc.fileSize = tfile.length();
                    desc.lastModified = tfile.lastModified();

                } else if (protocol.equals("http")) {
                    // read the file information via an HTTP HEAD request
                    HttpURLConnection ucon = (HttpURLConnection)
                        desc.sourceURL.openConnection();
                    ucon.setRequestMethod("HEAD");
                    ucon.connect();
                    desc.fileSize = ucon.getContentLength();
                    desc.lastModified = ucon.getLastModified();

                } else {
                    throw new IOException(
                        "Unknown source file protocol " +
                        "[protocol=" + protocol + ", desc=" + desc + "].");
                }

                // determine whether we actually need to fetch the file by
                // checking to see whether we have a local copy at all,
                // and if so, whether its file size or last-modified time
                // differs from the source file
                desc.destFileSize = desc.destFile.length();
                desc.destLastModified = desc.destFile.lastModified();
                if ((!desc.destFile.exists()) ||
                    (desc.destFileSize != desc.fileSize) ||
                    (desc.destLastModified < (desc.lastModified - DELTA)) ||
                    (desc.destLastModified > (desc.lastModified + DELTA))) {
                    // increment the total file size to be fetched
                    pinfo.totalSize += desc.fileSize;
                    // add the file to the list of files to be fetched
                    fetch.add(desc);
                    Log.debug("File deemed stale " +
                              "[url=" + desc.sourceURL + "].");

                } else {
                    Log.debug("File deemed up-to-date " +
                              "[url=" + desc.sourceURL + "].");
                }

            } catch (final IOException ioe) {
                notifyFailed(obs, null, ioe);
                if (fragile) {
                    return;
                }
            }
        }

        // download all stale files
        size = fetch.size();
        pinfo.start = System.currentTimeMillis();
        for (int ii = 0; ii < size; ii++) {
            DownloadDescriptor desc = (DownloadDescriptor)fetch.get(ii);
            try {
                processDownload(desc, obs, pinfo);
            } catch (IOException ioe) {
                notifyFailed(obs, desc, ioe);
                if (fragile) {
                    return;
                }
            }
        }

        // make sure to always let the observer know that we've wrapped up
        // by reporting 100% completion
        if (!pinfo.complete) {
            notifyProgress(obs, 100, 0L);
        }
    }

    /** Helper function. */
    protected void notifyProgress (final DownloadObserver obs,
                                   final int progress, final long remaining)
    {
        if (obs.notifyOnAWTThread()) {
            EventQueue.invokeLater(new Runnable() {
                public void run () {
                    obs.downloadProgress(progress, remaining);
                }
            });
        } else {
            obs.downloadProgress(progress, remaining);
        }

        if (progress == 100) {
            // if we're at 100%, run the post-download hook
            try {
                obs.postDownloadHook();
            } catch (Exception e) {
                Log.warning("Observer choked in post-download hook.");
                Log.logStackTrace(e);
            }

            // notify of total and final download completion
            if (obs.notifyOnAWTThread()) {
                EventQueue.invokeLater(new Runnable() {
                    public void run () {
                        obs.downloadComplete();
                    }
                });
            } else {
                obs.downloadComplete();
            }
        }
    }

    /** Helper function. */
    protected void notifyFailed (final DownloadObserver obs,
                                 final DownloadDescriptor desc,
                                 final Exception e)
    {
        if (obs.notifyOnAWTThread()) {
            EventQueue.invokeLater(new Runnable() {
                public void run () {
                    obs.downloadFailed(desc, e);
                }
            });
        } else {
            obs.downloadFailed(desc, e);
        }
    }

    /**
     * Processes a single download descriptor.
     */
    protected void processDownload (
        DownloadDescriptor desc, DownloadObserver obs, ProgressInfo pinfo)
        throws IOException
    {
        // download the resource bundle from the specified URL
        URLConnection ucon = desc.sourceURL.openConnection();

        // prepare to read the data from the URL into the cache file
        Log.info("Downloading file [url=" + desc.sourceURL + "].");
        InputStream in = ucon.getInputStream();
        FileOutputStream out = new FileOutputStream(desc.destFile);
        int read;

        // read in the file data
        while ((read = in.read(_buffer)) != -1) {
            // write it out to our local copy
            out.write(_buffer, 0, read);
            // report our progress to the download observer as a
            // percentage of the total file data to be transferred
            pinfo.currentSize += read;
            int pctdone = pinfo.getPercentDone();
            pinfo.complete = (pctdone >= 100);
            // if we've finished downloading everything, hold off on
            // notifying the observer until we're done working with the
            // file to ensure that any action the observer may take with
            // respect to the downloaded files can be safely undertaken
            if (!pinfo.complete) {
                // update the transfer rate to reflect the bit of data we
                // just transferred
                long now = System.currentTimeMillis();
                pinfo.updateXferRate(now);

                // notify the progress observer if it's been sufficiently
                // long since our last notification
                if ((now - pinfo.lastUpdate) >= UPDATE_DELAY) {
                    pinfo.lastUpdate = now;
                    long remaining = pinfo.getXferTimeRemaining();
                    notifyProgress(obs, pctdone, remaining);
                }
            }
        }

        // close the streams
        in.close();
        out.close();

        // if we have a last modified time, we want to adjust our cache
        // file accordingly
        if (desc.lastModified != 0) {
            if (!desc.destFile.setLastModified(desc.lastModified)) {
                Log.warning("Failed to set last-modified date " +
                            "[file=" + desc.destFile + "].");
            }
        }

        if (pinfo.complete) {
            // let the observer know we're finished now that we've
            // finished all of our work with the file
            notifyProgress(obs, 100, 0L);
        }
    }

    /**
     * A record describing a single download request.
     */
    protected static class DownloadRecord
    {
        /** The list of download descriptors to be downloaded. */
        public List descriptors;

        /** The download observer to notify of download progress. */
        public DownloadObserver obs;

        /** Whether to abort downloading if an error occurs. */
        public boolean fragile;
    }

    /**
     * A record detailing the progress of a download request.
     */
    protected class ProgressInfo
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

    /** The downloading thread. */
    protected Thread _dlthread;

    /** The queue of download requests. */
    protected Queue _dlqueue = new Queue();

    /** The data buffer used when reading file data. */
    protected byte[] _buffer;

    /** The data buffer size for reading file data. */
    protected static final int BUFFER_SIZE = 2048;

    /** The last-modified difference in milliseconds allowed between the
     * source and destination file without considering the destination
     * file to be out of date. */
    protected static final long DELTA = 5000L;

    /** The delay in milliseconds between notifying progress observers of
     * file download progress. */
    protected static final long UPDATE_DELAY = 2500L;
}
