//
// $Id: JNLPDownloader.java,v 1.3 2003/08/05 07:03:34 mdb Exp $

package com.threerings.resource;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;

import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import com.sun.javaws.cache.Patcher;
import com.sun.javaws.jardiff.JarDiffPatcher;

import com.samskivert.util.StringUtil;

import com.threerings.resource.DownloadManager.DownloadDescriptor;
import com.threerings.resource.DownloadManager.DownloadObserver;

/**
 * Does something extraordinary.
 */
public class JNLPDownloader extends Downloader
{
    // documentation inherited
    public void init (DownloadDescriptor ddesc)
    {
        super.init(ddesc);

        // determine which version we already have, if any
        _vfile = new File(mungePath(_desc.destFile, ".vers"));
        try {
            BufferedReader vin = new BufferedReader(new FileReader(_vfile));
            _cvers = vin.readLine();

        } catch (FileNotFoundException fnfe) {
            // TEMP: assume version 0.22 which is the last version
            // published prior to our moving to versioned diffs
            if (_desc.destFile.exists()) {
                // Log.info("Assuming v0.22 for " + _vfile + ".");
                _cvers = "0.22";
            }

        } catch (IOException ioe) {
            Log.warning("Error reading version file [path=" + _vfile +
                        ", error=" + ioe + "].");
        }
    }

    // documentation inherited
    public boolean checkUpdate (ProgressInfo info)
        throws IOException
    {
        // if we're doing versioning and we have the appropriate version,
        // no need to update
        if (_desc.version != null && _desc.version.equals(_cvers)) {
            return false;
        }

        // read the file information via an HTTP HEAD request
        HttpURLConnection ucon = (HttpURLConnection)
            getResourceURL().openConnection();
        ucon.setRequestMethod("HEAD");
        ucon.connect();

        // make sure we got a satisfactory response code
        if (ucon.getResponseCode() != HttpURLConnection.HTTP_OK ||
            JNLP_ERROR_TYPE.equals(ucon.getContentType())) {
            String errmsg = "Unable to check up-to-date for " +
                getResourceURL() + ": " + ucon.getResponseCode();
            throw new IOException(errmsg);
        }

        // if we're versioning, we know we need an update
        info.totalSize += ucon.getContentLength();
        Log.info(getResourceURL() + " requires " + ucon.getContentLength() +
                 " byte update.");
        return true;
    }

    // documentation inherited
    public void processDownload (DownloadManager dmgr, DownloadObserver obs,
                                 ProgressInfo pinfo, byte[] buffer)
        throws IOException
    {
        // download the resource bundle from the specified URL
        URL rsrcURL = getResourceURL();
        HttpURLConnection ucon = (HttpURLConnection)rsrcURL.openConnection();
        ucon.connect();

        // make sure we got a satisfactory response code
        if (ucon.getResponseCode() != HttpURLConnection.HTTP_OK ||
            JNLP_ERROR_TYPE.equals(ucon.getContentType())) {
            String errmsg = "Unable to download update for " +
                getResourceURL() + ": " + ucon.getResponseCode();
            throw new IOException(errmsg);
        }

        // determine whether or not this is a patch
        if (ucon.getContentType().equals(JARDIFF_TYPE)) {
            Log.info("Downloading patch [url=" + rsrcURL + "].");
            _patchFile = new File(mungePath(_desc.destFile, ".diff"));
            downloadContent(dmgr, obs, pinfo, buffer, ucon, _patchFile);

        } else {
            Log.info("Downloading whole jar [url=" + rsrcURL + "].");
            downloadContent(dmgr, obs, pinfo, buffer, ucon, _desc.destFile);
        }
    }

    // documentation inherited
    public void postDownload (DownloadManager dmgr, DownloadObserver obs,
                              ProgressInfo pinfo)
        throws IOException
    {
        if (_patchFile != null) {
            // move the old jar out of the way
            File oldDest = new File(_desc.destFile.getPath() + ".old");
            if (!_desc.destFile.renameTo(oldDest)) {
                Log.warning("Unable to move " + _desc.destFile + " to " +
                            oldDest + ". Cleaning up and failing.");
                // attempt to blow everything away before choking so that
                // next time we'll download afresh
                cleanUpAndFail(null);
            }

            // now apply the patch
            Log.info("Applying patch [old=" + oldDest + ", path=" + _patchFile +
                     ", new=" + _desc.destFile + "].");
            Patcher.PatchDelegate delegate = new Patcher.PatchDelegate() {
                public void patching (int value) {
                    // System.out.println("Patching " + _desc.destFile + ": " +
                    // value);
                }
            };
            JarDiffPatcher patcher = new JarDiffPatcher();

            try {
                BufferedOutputStream out = new BufferedOutputStream(
                    new FileOutputStream(_desc.destFile));
                patcher.applyPatch(delegate, oldDest.getPath(),
                                   _patchFile.getPath(), out);
                out.close();

            } catch (IOException ioe) {
                Log.warning("Failure applying patch [rfile=" + _desc.destFile +
                            ", error=" + ioe + "]. Cleaning up and failing.");
                oldDest.delete();
                cleanUpAndFail(ioe);
            }

            // clean up the old jar and the patch file
            oldDest.delete();
            _patchFile.delete();
        }

        PrintWriter pout = new PrintWriter(
            new BufferedWriter(new FileWriter(_vfile)));
        pout.println(_desc.version);
        pout.close();
        // Log.info("Updated version to " + _desc.version + ".");
    }

    /**
     * Replaces <code>.jar</code> with the supplied new extention if the
     * supplied file path ends in <code>.jar</code>. Otherwise the new
     * extension is appended to the whole existing file path.
     */
    protected String mungePath (File file, String newext)
    {
        String path = file.getPath();
        if (path.endsWith(".jar")) {
            path = path.substring(0, path.length()-4);
        }
        return path + newext;
    }

    /**
     * Constructs the resource URL using the source URL supplied in the
     * download descriptor and any version information that is
     * appropriate.
     */
    protected URL getResourceURL ()
    {
        if (_desc.version == null) {
            return _desc.sourceURL;
        }
        URL rsrcURL = _desc.sourceURL;
        String vargs = _desc.sourceURL.getPath() +
            "?version-id=" + _desc.version;
        if (_cvers != null) {
            vargs += "&current-version-id=" + _cvers;
        }
        try {
            rsrcURL = new URL(rsrcURL, vargs);
        } catch (MalformedURLException mue) {
            Log.warning("Error creating versioned resource URL " +
                        "[url=" + rsrcURL + ", vargs=" + vargs + "].");
        }
        return rsrcURL;
    }

    /**
     * Attempts to wipe out everything relating to this resource so that
     * the next time we attempt to run the client we can download it
     * completely afresh.
     */
    protected void cleanUpAndFail (IOException cause)
        throws IOException
    {
        _desc.destFile.delete();
        if (_patchFile != null) {
            _patchFile.delete();
        }
        if (_vfile != null) {
            _vfile.delete();
        }

        IOException failure = new IOException(
            "Failed to patch " + _desc.destFile + ".");
        if (cause != null) {
            failure.initCause(cause);
        }
        throw failure;
    }

    /** A file that contains the current resource version. */
    protected File _vfile;

    /** The file that contains our patch if we have one. */
    protected File _patchFile;

    /** The current version of the resource if we have one. */
    protected String _cvers;

    /** The mime-type of a jardiff patch file. */
    protected static final String JARDIFF_TYPE =
        "application/x-java-archive-diff";

    /** The mime-type indicating a jnlp-servlet error. Why they don't just
     * use an HTTP error response code, I dare not attempt to imagine. */
    protected static final String JNLP_ERROR_TYPE =
        "application/x-java-jnlp-error";
}
