//
// $Id: Handler.java,v 1.3 2003/05/06 18:48:18 ray Exp $

package com.threerings.resource;

import java.io.IOException;
import java.io.InputStream;

import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.net.URLStreamHandlerFactory;

/**
 * This class is not used directly, except by a registering ResourceManager
 * so that we can load data from the resource manager using URLs of the form
 * <code>resource://&lt;resourceSet&gt;/&lt;path&gt;</code>.
 */
public class Handler extends URLStreamHandler
{
    /**
     * Register this class to handle "resource" urls
     * ("resource://<i>resourceSet</i>/<i>path</i>") with the specified
     * ResourceManager.
     */
    public static void registerHandler (ResourceManager rmgr)
    {
        _rmgr = rmgr;

        // There are two ways to do this.
        // Method 1, which is the only one that seems to work under
        // Java Web Start, is to register a factory. This is kind of
        // scary because there can only be one factory.
        URL.setURLStreamHandlerFactory(new URLStreamHandlerFactory() {
            public URLStreamHandler createURLStreamHandler (String protocol)
            {
                if (protocol.equalsIgnoreCase("resource")) {
                    return new Handler();
                }
                return null;
            }
        });


        // Method 2 seems like a better idea but doesn't work under
        // Java Web Start. We add on a property that registers this
        // very class as the handler for the resource property.
        // It would be instantiated with Class.forName().
        // (And I did check, it's not dasho that is preventing this
        // from working under JWS, it's something else.)
        /*
        // dug up from java.net.URL
        String HANDLER_PROP = "java.protocol.handler.pkgs";

        String prop = System.getProperty(HANDLER_PROP, "");
        if (!"".equals(prop)) {
            prop += "|";
        }
        prop += "com.threerings";
        System.setProperty(HANDLER_PROP, prop);
        */
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
}
