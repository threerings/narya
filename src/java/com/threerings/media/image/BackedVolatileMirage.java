//
// $Id: BackedVolatileMirage.java,v 1.1 2003/01/13 22:49:46 mdb Exp $

package com.threerings.media.image;

import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;

import com.threerings.media.Log;

/**
 * Provides a volatile mirage that is backed by a buffered image that is
 * not obtained from the image manager but is instead provided at
 * construct time and completely circumvents the image manager's cache. As
 * such, this should not be used unless you know what you're doing.
 */
public class BackedVolatileMirage extends VolatileMirage
{
    /**
     * Creates a mirage with the supplied regeneration informoation and
     * prepared image.
     */
    public BackedVolatileMirage (ImageManager imgr, BufferedImage source)
    {
        super(imgr, new Rectangle(0, 0, source.getWidth(), source.getHeight()));
        _source = source;

        // create our volatile image for the first time
        createVolatileImage();
    }

    // documentation inherited
    protected void refreshVolatileImage ()
    {
        Graphics gfx = null;
        try {
            gfx = _image.getGraphics();
            gfx.drawImage(_source, -_bounds.x, -_bounds.y, null);

        } catch (Exception e) {
            Log.warning("Failure refreshing mirage " + this + ".");
            Log.logStackTrace(e);

        } finally {
            gfx.dispose();
        }
    }

    // documentation inherited
    protected void toString (StringBuffer buf)
    {
        super.toString(buf);
        buf.append(", src=").append(_source);
    }

    protected BufferedImage _source;
}
