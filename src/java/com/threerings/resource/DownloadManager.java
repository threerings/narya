//
// $Id: DownloadManager.java,v 1.12 2003/08/09 00:31:14 mdb Exp $

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
         * Called on the download thread when the patching of jar files
         * has begun.
         */
        public void patching ();

        /**
         * Called after the download and patching has completed on the
         * download manager thread.
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

        /** The version of the file to be downloaded. */
        public String version;

        /** The last-modified timestamp of the source. */
        public long lastModified;

        /** The last-modified timestamp of the destination file. */
        public long destLastModified;

        /** The size in bytes of the destination file. */
        public long destFileSize;

        /**
         * Constructs a download descriptor to retrieve the specified
         * version of the given file from the given URL.
         */
        public DownloadDescriptor (URL url, File file, String version)
        {
            this.sourceURL = url;
            this.destFile = file;
            this.version = version;
        }

        /**
         * Creates the appropriate type of downloader for this descriptor.
         */
        public Downloader createDownloader ()
            throws IOException
        {
            String protocol = sourceURL.getProtocol();
            if (protocol.equals("file")) {
                return new FileDownloader();
            } else if (protocol.equals("http")) {
                return VERSIONING ? (Downloader)new JNLPDownloader() :
                    (Downloader)new HTTPDownloader();
            } else {
                throw new IOException(
                    "Unknown source file protocol " +
                    "[protocol=" + protocol + ", desc=" + this + "].");
            }
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
                Downloader loader = desc.createDownloader();
                loader.init(desc);
                if (loader.checkUpdate(pinfo)) {
                    fetch.add(loader);
                }

            } catch (final IOException ioe) {
                notifyFailed(obs, null, ioe);
                if (fragile) {
                    return;
                }
            }
        }

        if (pinfo.totalSize > 0) {
            Log.info("Initiating download of " + pinfo.totalSize + " bytes.");
        }

        // download all stale files
        size = fetch.size();
        pinfo.start = System.currentTimeMillis();
        for (int ii = 0; ii < size; ii++) {
            Downloader loader = (Downloader)fetch.get(ii);
            try {
                loader.processDownload(this, obs, pinfo, _buffer);
            } catch (IOException ioe) {
                notifyFailed(obs, loader.getDescriptor(), ioe);
                if (fragile) {
                    return;
                }
            }
        }

        // now go through and do the post-download phase
        DownloadDescriptor fdesc = null;
        for (int ii = 0; ii < size; ii++) {
            Downloader loader = (Downloader)fetch.get(ii);
            try {
                loader.postDownload(this, obs, pinfo);
            } catch (IOException ioe) {
                // we want to try to apply as many of the patches as we
                // can, so we don't fail entirely here, just keep track of
                // the last failure and report that when we're done
                fdesc = loader.getDescriptor();
                Log.warning("Downloader failed in postDownload hook " +
                            "[desc=" + fdesc + "].");
                Log.logStackTrace(ioe);
            }
        }

        // if we had any failure, go ahead and report it now
        if (fdesc != null) {
            PatchException pe = new PatchException(
                "Failed to patch one or more updated bundles.");
            notifyFailed(obs, fdesc, pe);

        } else {
            // make sure to always let the observer know that we've
            // wrapped up by reporting 100% completion
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

    /** Indicates whether or not we're using versioned resources. */
    protected static boolean VERSIONING = false;
    static {
        try {
            VERSIONING = "true".equalsIgnoreCase(
                System.getProperty("versioned_rsrcs"));
        } catch (Throwable t) {
            // no versioning, no problem
        }
    }
}
