//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2025 Three Rings Design, Inc., All Rights Reserved
// https://github.com/threerings/narya/blob/master/LICENSE

package com.threerings.io;

import com.samskivert.util.StringUtil;

import com.threerings.util.ActionScript;

/**
 * A simple serializable object implements the {@link Streamable}
 * interface and provides a default {@link Object#toString} implementation which
 * outputs all public members.
 */
public class SimpleStreamableObject implements Streamable
{
    @Override
    public String toString ()
    {
        StringBuilder buf = new StringBuilder("[");
        toString(buf);
        return buf.append("]").toString();
    }

    /**
     * Handles the toString-ification of all public members. Derived
     * classes can override and include non-public members if desired.
     */
    @ActionScript(name="toStringBuilder")
    protected void toString (StringBuilder buf)
    {
        StringUtil.fieldsToString(buf, this);
    }
}
