//
// $Id: CachedVolatileMirage.java,v 1.3 2003/04/11 18:48:05 mdb Exp $

package com.threerings.media.image;

import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.Transparency;
import java.awt.image.BufferedImage;

import com.threerings.media.Log;
import com.samskivert.util.StringUtil;

/**
 * A mirage implementation which allows the image to be maintained in
 * video memory and refetched from the image manager in the event that our
 * target screen resolution changes or we are flushed from video memory
 * for some other reason.
 *
 * <p> These objects are never created directly, but always obtained from
 * the {@link ImageManager}.
 */
public class CachedVolatileMirage extends VolatileMirage
{
    /**
     * Creates a mirage with the supplied regeneration informoation and
     * prepared image.
     */
    protected CachedVolatileMirage (
        ImageManager imgr, ImageManager.ImageKey source,
        Rectangle bounds, Colorization[] zations)
    {
        super(imgr, bounds);

        _source = source;
        _zations = zations;

        // create our volatile image for the first time
        createVolatileImage();
    }

    // documentation inherited
    protected int getTransparency ()
    {
        BufferedImage source = _imgr.getImage(_source, _zations);
        return (source == null) ? Transparency.OPAQUE :
            source.getColorModel().getTransparency();
    }

    // documentation inherited
    protected void refreshVolatileImage ()
    {
        Graphics gfx = null;
        try {
            BufferedImage source = _imgr.getImage(_source, _zations);
            if (source != null) {
                gfx = _image.getGraphics();
                gfx.drawImage(source, -_bounds.x, -_bounds.y, null);
            }

        } catch (Exception e) {
            Log.warning("Failure refreshing mirage " + this + ".");
            Log.logStackTrace(e);

        } finally {
            if (gfx != null) {
                gfx.dispose();
            }
        }
    }

    // documentation inherited
    protected void toString (StringBuffer buf)
    {
        super.toString(buf);
        buf.append(", key=").append(_source);
        buf.append(", zations=").append(_zations);
    }

    /** The key that identifies the image data used to create our volatile
     * image. */
    protected ImageManager.ImageKey _source;

    /** Optional colorizations that are applied to our source image when
     * creating our mirage. */
    protected Colorization[] _zations;
}
