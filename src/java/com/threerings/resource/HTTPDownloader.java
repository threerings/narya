//
// $Id: HTTPDownloader.java,v 1.1 2003/08/08 17:45:00 mdb Exp $

package com.threerings.resource;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;

import com.threerings.resource.DownloadManager.DownloadObserver;

/**
 * Downloads resources via HTTP using on last modification timestamps to
 * determine whether updates are needed.
 */
public class HTTPDownloader extends Downloader
{
    // documentation inherited
    public boolean checkUpdate (ProgressInfo info)
        throws IOException
    {
        // read the file information via an HTTP HEAD request
        HttpURLConnection ucon = (HttpURLConnection)
            _desc.sourceURL.openConnection();
        ucon.setRequestMethod("HEAD");
        ucon.connect();

        // make sure we got a satisfactory response code
        if (ucon.getResponseCode() != HttpURLConnection.HTTP_OK) {
            String errmsg = "Unable to check up-to-date for " +
                _desc.sourceURL + ": " + ucon.getResponseCode();
            throw new IOException(errmsg);
        }

        // read size and last modified information from the HEAD response
        long fileSize = ucon.getContentLength();
        _desc.lastModified = ucon.getLastModified();

        if (compareWithLocal(fileSize, _desc.lastModified)) {
            // increment the total file size to be fetched
            info.totalSize += fileSize;
            Log.debug("Resource deemed stale [url=" + _desc.sourceURL + "].");
            return true;
        } else {
            Log.debug("Resource up-to-date [url=" + _desc.sourceURL + "].");
            return false;
        }
    }

    // documentation inherited
    public void processDownload (DownloadManager dmgr, DownloadObserver obs,
                                 ProgressInfo pinfo, byte[] buffer)
        throws IOException
    {
        // download the resource bundle from the specified URL
        HttpURLConnection ucon = (HttpURLConnection)
            _desc.sourceURL.openConnection();
        ucon.connect();

        // make sure we got a satisfactory response code
        if (ucon.getResponseCode() != HttpURLConnection.HTTP_OK) {
            String errmsg = "Unable to download update for " +
                _desc.sourceURL + ": " + ucon.getResponseCode();
            throw new IOException(errmsg);
        }

        Log.info("Downloading updated jar [url=" + _desc.sourceURL + "].");
        downloadContent(dmgr, obs, pinfo, buffer, ucon, _desc.destFile);
    }
}
