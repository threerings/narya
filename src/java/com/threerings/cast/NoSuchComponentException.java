//
// $Id: NoSuchComponentException.java,v 1.1 2001/10/26 01:17:21 shaper Exp $

package com.threerings.cast;

/**
 * Thrown when an attempt is made to retrieve a non-existent character
 * component from the component repository.
 */
public class NoSuchComponentException extends Exception
{
    public NoSuchComponentException (int cid)
    {
        super("No such component [cid=" + cid + "]");
        _cid = cid;
    }

    public int getId ()
    {
        return _cid;
    }

    protected int _cid;
}
