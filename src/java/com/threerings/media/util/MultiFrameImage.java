//
// $Id: MultiFrameImage.java,v 1.5 2004/08/27 02:12:47 mdb Exp $
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

package com.threerings.media.util;

import java.awt.Graphics2D;

/**
 * The multi-frame image interface provides encapsulated access to a set
 * of images that are used to create a multi-frame animation.
 */
public interface MultiFrameImage
{
    /**
     * Returns the number of frames in this multi-frame image.
     */
    public int getFrameCount ();

    /**
     * Returns the width of the specified frame image.
     */
    public int getWidth (int index);

    /**
     * Returns the height of the specified frame image.
     */
    public int getHeight (int index);

    /**
     * Renders the specified frame into the specified graphics object at
     * the specified coordinates.
     */
    public void paintFrame (Graphics2D g, int index, int x, int y);

    /**
     * Returns true if the specified frame contains a non-transparent
     * pixel at the specified coordinates.
     */
    public boolean hitTest (int index, int x, int y);
}
