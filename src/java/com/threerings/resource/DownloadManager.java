//
// $Id: DownloadManager.java,v 1.1 2002/07/19 20:12:23 shaper Exp $

package com.threerings.resource;

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
         */
        public void downloadProgress (int percent);

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
        List descriptors, DownloadObserver obs, boolean fragile)
    {
        // let the observer know that we're about to resolve all files to
        // be downloaded
        obs.resolvingDownloads();

        // check the size and last-modified information for each file to
        // ascertain whether our local copy needs to be refreshed
        ArrayList fetch = new ArrayList();
        long totalSize = 0;
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
                    (desc.destLastModified != desc.lastModified)) {
                    // increment the total file size to be fetched
                    totalSize += desc.fileSize;
                    // add the file to the list of files to be fetched
                    fetch.add(desc);
                    Log.debug("File deemed stale " +
                              "[url=" + desc.sourceURL + "].");

                } else {
                    Log.debug("File deemed up-to-date " +
                              "[url=" + desc.sourceURL + "].");
                }

            } catch (IOException ioe) {
                obs.downloadFailed(null, ioe);
                if (fragile) {
                    return;
                }
            }
        }

        // download all stale files
        size = fetch.size();
        long currentSize = 0;
        boolean complete = false;
        for (int ii = 0; ii < size; ii++) {
            DownloadDescriptor desc = (DownloadDescriptor)fetch.get(ii);
            try {
                complete = processDownload(desc, obs, currentSize, totalSize);
                currentSize += desc.fileSize;

            } catch (IOException ioe) {
                obs.downloadFailed(desc, ioe);
                if (fragile) {
                    return;
                }
            }
        }

        // make sure to always let the observer know that we've wrapped up
        // by reporting 100% completion
        if (!complete) {
            obs.downloadProgress(100);
        }
    }

    /**
     * Processes a single download descriptor.  Returns whether the
     * download observer was notified of a 100% complete progress update.
     */
    protected boolean processDownload (
        DownloadDescriptor desc, DownloadObserver obs, long currentSize,
        long totalSize)
        throws IOException
    {
        // download the resource bundle from the specified URL
        URLConnection ucon = desc.sourceURL.openConnection();

        // prepare to read the data from the URL into the cache file
        Log.info("Downloading file [url=" + desc.sourceURL + "].");
        InputStream in = ucon.getInputStream();
        FileOutputStream out = new FileOutputStream(desc.destFile);
        int read;
        boolean complete = false;

        // read in the file data
        while ((read = in.read(_buffer)) != -1) {
            // write it out to our local copy
            out.write(_buffer, 0, read);

            // report our progress to the download observer as a
            // percentage of the total file data to be transferred
            currentSize += read;
            int pctdone = (int)((currentSize / (float)totalSize) * 100f);
            complete = (pctdone >= 100);
            obs.downloadProgress(pctdone);
        }

        // close the streams
        in.close();
        out.close();

        // if we have a last modified time, we want to adjust our cache
        // file accordingly
        if (desc.lastModified != 0) {
            desc.destFile.setLastModified(desc.lastModified);
        }

        return complete;
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

    /** The downloading thread. */
    protected Thread _dlthread;

    /** The queue of download requests. */
    protected Queue _dlqueue = new Queue();

    /** The data buffer used when reading file data. */
    protected byte[] _buffer;

    /** The data buffer size for reading file data. */
    protected static final int BUFFER_SIZE = 2048;
}
