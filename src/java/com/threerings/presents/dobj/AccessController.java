//
// $Id: AccessController.java,v 1.2 2004/08/27 02:20:20 mdb Exp $
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

package com.threerings.presents.dobj;

/**
 * Used to validate distributed object subscription requests and event
 * dispatches.
 *
 * @see DObject#setAccessController
 */
public interface AccessController
{
    /**
     * Should return true if the supplied subscriber is allowed to
     * subscribe to the specified object.
     */
    public boolean allowSubscribe (DObject object, Subscriber subscriber);

    /**
     * Should return true if the supplied event is legal for dispatch on
     * the specified distributed object.
     */
    public boolean allowDispatch (DObject object, DEvent event);
}
