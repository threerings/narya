//
// $Id: FileDownloader.java,v 1.2 2004/06/16 09:44:23 mdb Exp $

package com.threerings.resource;

import java.io.File;
import java.io.IOException;

/**
 * "Downloads" a file from a location on the filesystem.
 */
public class FileDownloader extends Downloader
{
    // documentation inherited
    public boolean checkUpdate (ProgressInfo info)
        throws IOException
    {
        // read the file information directly from the file system
        File tfile = new File(_desc.sourceURL.getPath());
        _contentLength = tfile.length();
        _desc.lastModified = tfile.lastModified();

        if (compareWithLocal(_contentLength, _desc.lastModified)) {
            // increment the total file size to be fetched
            info.totalSize += _contentLength;
            Log.debug("File deemed stale [url=" + _desc.sourceURL + "].");
            return true;
        } else {
            Log.debug("File deemed up-to-date [url=" + _desc.sourceURL + "].");
            return false;
        }
    }
}
