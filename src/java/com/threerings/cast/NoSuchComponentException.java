//
// $Id: NoSuchComponentException.java,v 1.2 2001/11/27 08:09:35 mdb Exp $

package com.threerings.cast;

/**
 * Thrown when an attempt is made to retrieve a non-existent character
 * component from the component repository.
 */
public class NoSuchComponentException extends Exception
{
    public NoSuchComponentException (int componentId)
    {
        super("No such component [componentId=" + componentId + "]");
        _componentId = componentId;
    }

    public int getComponentId ()
    {
        return _componentId;
    }

    protected int _componentId;
}
