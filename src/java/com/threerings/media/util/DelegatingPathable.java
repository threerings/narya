//
// $Id: DelegatingPathable.java,v 1.1 2003/05/20 04:04:56 mdb Exp $

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
