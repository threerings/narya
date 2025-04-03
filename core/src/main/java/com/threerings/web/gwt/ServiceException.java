//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2025 Three Rings Design, Inc., All Rights Reserved
// https://github.com/threerings/narya/blob/master/LICENSE

package com.threerings.web.gwt;

/**
 * An exception thrown by a remote service when it wishes to communicate a
 * particular error message to a user.
 */
public class ServiceException extends Exception
{
    /**
     * Creates a service exception with the supplied translation message.
     */
    public ServiceException (String message)
    {
        _message = message;
    }

    /**
     * Default constructor for use when unserializing.
     */
    public ServiceException ()
    {
    }

    @Override // from Exception
    public String getMessage ()
    {
        // we have to return our own message because GWT won't serialize anything in our parent
        // class without a bunch of annoying fiddling
        return _message;
    }

    protected String _message;
}
