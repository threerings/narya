//
// $Id: NoSuchComponentException.java,v 1.3 2002/02/19 22:09:50 mdb Exp $

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

    public NoSuchComponentException (
        String componentClass, String componentName)
    {
        super("No such component [class=" + componentClass +
              ", name=" + componentName + "]");
        _componentId = -1;
    }

    public int getComponentId ()
    {
        return _componentId;
    }

    protected int _componentId;
}
