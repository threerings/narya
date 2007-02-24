//
// $Id$
//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2007 Three Rings Design, Inc., All Rights Reserved
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

package com.threerings.presents.client;

/**
 * The client adapter makes life easier for client observer classes that
 * only care about one or two of the client observer callbacks. They can
 * either extend client adapter or create an anonymous class that extends
 * it and overrides just the callbacks they care about.
 *
 * <p> Note that the client adapter defaults to always ratifying a call to
 * {@link #clientWillLogoff} by returning true.
 */
public class ClientAdapter implements ClientObserver
{
    // documentation inherited
    public void clientWillLogon (Client client)
    {
    }

    // documentation inherited
    public void clientDidLogon (Client client)
    {
    }

    // documentation inherited
    public void clientFailedToLogon (Client client, Exception cause)
    {
    }

    // documentation inherited from interface
    public void clientObjectDidChange (Client client)
    {
    }

    // documentation inherited
    public void clientConnectionFailed (Client client, Exception cause)
    {
    }

    // documentation inherited
    public boolean clientWillLogoff (Client client)
    {
        return true;
    }

    // documentation inherited
    public void clientDidLogoff (Client client)
    {
    }

    // documentation inherited
    public void clientDidClear (Client client)
    {
    }
}
