//
// $Id: IMImageProvider.java,v 1.1 2003/01/13 22:49:46 mdb Exp $

package com.threerings.media.tile;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;

import com.threerings.media.image.Colorization;
import com.threerings.media.image.ImageDataProvider;
import com.threerings.media.image.ImageManager;
import com.threerings.media.image.Mirage;

/**
 * Provides images to a tileset given a reference to the image manager and
 * an image data provider.
 */
public class IMImageProvider implements ImageProvider
{
    public IMImageProvider (ImageManager imgr, ImageDataProvider dprov)
    {
        _imgr = imgr;
        _dprov = dprov;
    }

    public IMImageProvider (ImageManager imgr, String rset)
    {
        _imgr = imgr;
        _rset = rset;
    }

    // documentation inherited from interface
    public BufferedImage getTileSetImage (String path, Colorization[] zations)
    {
        return _imgr.getImage(getImageKey(path), zations);
    }

    // documentation inherited from interface
    public Mirage getTileImage (String path, Rectangle bounds,
                                Colorization[] zations)
    {
        return _imgr.getMirage(getImageKey(path), bounds, zations);
    }

    protected final ImageManager.ImageKey getImageKey (String path)
    {
        return (_dprov == null) ?
            _imgr.getImageKey(_rset, path) :
            _imgr.getImageKey(_dprov, path);
    }

    protected ImageManager _imgr;
    protected ImageDataProvider _dprov;
    protected String _rset;
}
