//
// $Id: ToolkitLoader.java,v 1.3 2003/01/08 04:09:02 mdb Exp $

package com.threerings.media.image;

import java.awt.Component;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.Toolkit;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.StreamUtils;

/**
 * Loads images using the default AWT toolkit (for compatibility with
 * pre-1.4 JVMs).
 */
public class ToolkitLoader implements ImageLoader
{
    public ToolkitLoader (Component context)
    {
        // do something more informative than NPE if we're misconfigured
        if (context == null) {
            String errmsg = "Toolkit image loader constructed without a " +
                "context via which we can create a MediaTracker. If the " +
                "image manager may fall back to using the Toolkit image " +
                "loader (like it just did), then you have to provide a " +
                "component instance for it at construct time. Thanks and " +
                "have a nice day.";
            throw new RuntimeException(errmsg);
        }

        _toolkit = context.getToolkit();
        _tracker = new MediaTracker(context);
    }

    // documentation inherited
    public Image loadImage (InputStream source)
        throws IOException
    {
        byte[] data = StreamUtils.streamAsBytes(source, READ_BUFFER_SIZE);
        Image image = _toolkit.createImage(data);

        // we now have to wait for the image to finish decoding (this all
        // apparently happens on another thread. yay!)
        _tracker.addImage(image, 0);

        // if something goes horribly awry, we don't want to go into an
        // infinite loop, so we bound the number of times we'll allow
        // ourselves to be interrupted and loop
        boolean done = false;
        for (int i = 0; (i < 1000) && !done; i++) {
            try {
                _tracker.waitForAll();
                done = true;
            } catch (InterruptedException e) {
                // loop back around
            }
        }

        return image;
    }

    /** Our AWT toolkit which we use to create images. */
    protected Toolkit _toolkit;

    /** Used to wait for images to load. */
    protected MediaTracker _tracker;

    /** The size of the read buffer used to load the image data. */
    protected static final int READ_BUFFER_SIZE = 4096;
}
