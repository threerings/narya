//
// $Id: TrackedStreamableObject.java,v 1.2 2004/08/27 02:12:36 mdb Exp $
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

package com.threerings.io;

import com.samskivert.util.StringUtil;

import com.threerings.util.TrackedObject;

/**
 * A simple serializable object implements the {@link Streamable}
 * interface and provides a default {@link #toString} implementation which
 * outputs all public members.
 */
public class TrackedStreamableObject extends TrackedObject
    implements Streamable
{
    /**
     * Generates a string representation of this instance.
     */
    public String toString ()
    {
        StringBuffer buf = new StringBuffer("[");
        toString(buf);
        return buf.append("]").toString();
    }

    /**
     * Handles the toString-ification of all public members. Derived
     * classes can override and include non-public members if desired.
     */
    protected void toString (StringBuffer buf)
    {
        StringUtil.fieldsToString(buf, this);
    }
}
