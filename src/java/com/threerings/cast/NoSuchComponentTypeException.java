//
// $Id: NoSuchComponentTypeException.java,v 1.1 2001/10/26 01:17:21 shaper Exp $

package com.threerings.cast;

/**
 * Thrown when an attempt is made to reference a non-existent
 * component type in the component repository.
 */
public class NoSuchComponentTypeException extends Exception
{
    public NoSuchComponentTypeException (int ctid)
    {
        super("No such component type [ctid=" + ctid + "]");
        _ctid = ctid;
    }

    public int getTypeId ()
    {
        return _ctid;
    }

    protected int _ctid;
}
