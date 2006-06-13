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

package com.threerings.media.util;

import java.awt.Rectangle;

/**
 * Delegates all calls to a delegate pathable. One would derive from this
 * class and override just the methods in which that one desired to
 * intervene.
 */
public class DelegatingPathable implements Pathable
{
    public DelegatingPathable (Pathable delegate)
    {
        _delegate = delegate;
    }

    // documentation inherited from interface
    public int getX ()
    {
        return _delegate.getX();
    }

    // documentation inherited from interface
    public int getY ()
    {
        return _delegate.getY();
    }

    // documentation inherited from interface
    public Rectangle getBounds ()
    {
        return _delegate.getBounds();
    }

    // documentation inherited from interface
    public void setLocation (int x, int y)
    {
        _delegate.setLocation(x, y);
    }

    // documentation inherited from interface
    public void setOrientation (int orient)
    {
        _delegate.setOrientation(orient);
    }

    // documentation inherited from interface
    public int getOrientation ()
    {
        return _delegate.getOrientation();
    }

    // documentation inherited from interface
    public void pathBeginning ()
    {
        _delegate.pathBeginning();
    }

    // documentation inherited from interface
    public void pathCompleted (long timestamp)
    {
        _delegate.pathCompleted(timestamp);
    }

    protected Pathable _delegate;
}
