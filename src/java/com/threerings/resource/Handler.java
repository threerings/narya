//
// $Id: Handler.java,v 1.1 2003/04/24 07:23:47 ray Exp $

package com.threerings.resource;

import java.io.IOException;
import java.io.InputStream;

import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;

/**
 * This class is not used directly, except by a registering ResourceManager
 * so that we can load data from the resource manager using URLs of the form
 * <code>resource://&lt;bundle&gt;/&lt;path&gt;</code>.
 */
public class Handler extends URLStreamHandler
{
    /**
     * Register this class to handle "resource" urls
     * ("resource://<i>bundlename</i>/<i>path</i>") with the specified
     * ResourceManager.
     */
    public static void registerHandler (ResourceManager rmgr)
    {
        _rmgr = rmgr;

        String prop = System.getProperty(HANDLER_PROP, "");
        if (!"".equals(prop)) {
            prop += "|";
        }
        prop += "com.threerings";
        System.setProperty(HANDLER_PROP, prop);
    }

    // documentation inherited
    protected URLConnection openConnection (URL url)
        throws IOException
    {
        return new URLConnection(url) {
            // documentation inherited
            public void connect ()
                throws IOException
            {
                try {
                    // the host is the bundle name
                    _stream = _rmgr.getResource(this.url.getHost(),
                        // and we need to remove the leading '/' from path
                        this.url.getPath().substring(1));
                    this.connected = true;

                } catch (IOException ioe) {
                    Log.warning("Could not find resource [url=" + this.url +
                        ", error=" + ioe.getMessage() + "].");
                    throw ioe; // rethrow
                }
            }

            // documentation inherited
            public InputStream getInputStream ()
                throws IOException
            {
                if (!this.connected) {
                    connect();
                }
                return _stream;
            }

            protected InputStream _stream;
        };
    }

    /** Our singleton resource manager. */
    protected static ResourceManager _rmgr;

    /** Dug up from java.net.URL */
    protected static final String HANDLER_PROP = "java.protocol.handler.pkgs";
}
