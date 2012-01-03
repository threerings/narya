//
// $Id$
//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2012 Three Rings Design, Inc., All Rights Reserved
// http://code.google.com/p/narya/
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
