//
// $Id$
//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2004 Three Rings Design, Inc., All Rights Reserved
// http://www.threerings.net/code/narya/
//
// This library is free software; you can redistribute it and/or modify it
// under the terms of the GNU Lesser General Public License as published
// by the Free Software Foundation; either version 2.1 of the License, or
// (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA

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
