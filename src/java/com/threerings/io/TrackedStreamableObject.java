//
// $Id: TrackedStreamableObject.java,v 1.1 2004/08/13 23:44:16 mdb Exp $

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
