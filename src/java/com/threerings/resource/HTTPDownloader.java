//
// $Id: HTTPDownloader.java,v 1.4 2004/06/17 03:02:55 mdb Exp $

package com.threerings.resource;

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
        _contentLength = ucon.getContentLength();
        _desc.lastModified = ucon.getLastModified();

        if (compareWithLocal(_contentLength, _desc.lastModified)) {
            // increment the total file size to be fetched
            info.totalSize += _contentLength;
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

        // if we have a last modified time, we want to adjust our cache
        // file accordingly
        if (_desc.lastModified != 0) {
            if (!_desc.destFile.setLastModified(_desc.lastModified)) {
                Log.warning("Failed to set last-modified date " +
                            "[file=" + _desc.destFile + "].");
            }
        }
    }
}
