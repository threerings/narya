//
// $Id: JNLPDownloader.java,v 1.17 2004/06/16 09:25:52 mdb Exp $

package com.threerings.resource;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import com.sun.javaws.cache.Patcher;
import com.sun.javaws.jardiff.JarDiffPatcher;

import com.samskivert.io.StreamUtil;
import com.samskivert.util.FileUtil;
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

        String dpath = _desc.destFile.getPath();

        // determine the path of our version file before we version the
        // path of the destination file
        _vfile = new File(FileUtil.resuffix(_desc.destFile, ".jar", ".vers"));

        // if we're using a version, adjust our destination file path
        // based on said version
        if (!StringUtil.blank(_desc.version)) {
            _desc.destFile = new File(
                ResourceManager.versionPath(dpath, _desc.version, ".jar"));
        }

        // determine which version we already have, if any
        if (_vfile.exists()) {
            try {
                BufferedReader vin = new BufferedReader(new FileReader(_vfile));
                _cvers = vin.readLine();

                // make sure the version referenced by that file still
                // exists; if not ignore our "current version"
                _curFile = new File(ResourceManager.versionPath(
                                        dpath, _cvers, ".jar"));
                if (!_curFile.exists()) {
                    _cvers = null;
                }

            } catch (IOException ioe) {
                Log.warning("Error reading version file [path=" + _vfile +
                            ", error=" + ioe + "].");
            }
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

        // if we're versioning, we know we need an update; force the
        // content length to be at least 1 so that we behave marginally
        // sensibly (ticking off each file as we download it) if our
        // server doesn't provide content-length for some assinine reason
        int cl = Math.max(ucon.getContentLength(), 1);
        info.totalSize += cl;
        Log.info(getResourceURL() + " requires " + cl + " byte update.");
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
            _patchFile = new File(
                FileUtil.resuffix(_curFile, ".jar", ".diff"));
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
            // now apply the patch
            Log.info("Applying patch [old=" + _curFile +
                     ", patch=" + _patchFile +
                     ", new=" + _desc.destFile + "].");
            Patcher.PatchDelegate delegate = new Patcher.PatchDelegate() {
                public void patching (int value) {
                    // System.out.println("Patching " + _desc.destFile + ": " +
                    // value);
                }
            };

            JarDiffPatcher patcher = new JarDiffPatcher();
            BufferedOutputStream out = null;
            try {
                out = new BufferedOutputStream(
                    new FileOutputStream(_desc.destFile));
                patcher.applyPatch(delegate, _curFile.getPath(),
                                   _patchFile.getPath(), out);
                out.close();

            } catch (IOException ioe) {
                Log.warning("Failure applying patch [rfile=" + _desc.destFile +
                            ", error=" + ioe + "]. Cleaning up and failing.");
                StreamUtil.close(out);
                cleanUpAndFail(ioe);
            }

            // clean up the old jar and the patch file
            if (!_curFile.delete()) {
                Log.warning("Failed to delete old bundle " + _curFile + ".");
            }
            if (!_patchFile.delete()) {
                Log.warning("Failed to delete patch file " + _patchFile + ".");
            }

            // delete any old unversioned version of the .jar file
            File unverDest = new File(ResourceManager.unversionPath(
                                          _desc.destFile.getPath(), ".jar"));
            if (unverDest.exists()) {
                if (!unverDest.delete()) {
                    Log.warning("Failed to delete old unversioned bundle '" +
                                unverDest + "'.");
                }
            }
        }

        // attempt to delete any old stale bundles as well
        if (_desc.version != null) {
            try {
                String cpath = _desc.destFile.getPath();
                String pcpath = ResourceManager.unversionPath(cpath, ".jar");
                File pdir = _desc.destFile.getParentFile();
                File[] files = pdir.listFiles();
                for (int ii = 0; ii < files.length; ii++) {
                    String path = files[ii].getPath();
                    if (path.equals(cpath) || !path.endsWith(".jar")) {
                        continue;
                    }
                    String ppath = ResourceManager.unversionPath(path, ".jar");
                    if (!pcpath.equals(ppath)) {
                        continue;
                    }
                    if (!files[ii].delete()) {
                        Log.warning("Unable to delete stale bundle '" +
                                    files[ii].getPath() + "'.");
                    } else {
                        Log.info("Deleted stale bundle '" + files[ii] + "'.");
                    }
                }
            } catch (Exception e) {
                Log.warning("Failure deleting stale bundles.");
                Log.logStackTrace(e);
            }
        }

        PrintWriter pout = new PrintWriter(
            new BufferedWriter(new FileWriter(_vfile)));
        pout.println(_desc.version);
        pout.close();
        // Log.info("Updated version to " + _desc.version + ".");
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
        if (_curFile != null) {
            Log.warning("Failed to delete " + _desc.destFile +
                        " in cleanUpAndFail().");
        }
        if (_patchFile != null) {
            _patchFile.delete();
        }
        if (_vfile != null) {
            if (!_vfile.delete()) {
                Log.warning("Failed to delete " + _vfile +
                            " in cleanUpAndFail().");
            }
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

    /** The existing version of our bundle file. */
    protected File _curFile;

    /** The mime-type of a jardiff patch file. */
    protected static final String JARDIFF_TYPE =
        "application/x-java-archive-diff";

    /** The mime-type indicating a jnlp-servlet error. Why they don't just
     * use an HTTP error response code, I dare not attempt to imagine. */
    protected static final String JNLP_ERROR_TYPE =
        "application/x-java-jnlp-error";
}
