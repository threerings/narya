//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2025 Three Rings Design, Inc., All Rights Reserved
// https://github.com/threerings/narya/blob/master/LICENSE

package com.threerings.presents.dobj;

import com.threerings.io.Streamable;

/**
 * A quick and easy DSet.Entry that holds some sort of Comparable.
 *
 * Remember: this type must also be {@link Streamable}.
 */
public class SimpleEntry<T extends Comparable<?>>
    implements DSet.Entry
{
    public SimpleEntry (T key) {
        _key = key;
    }

    public T getKey () {
        return _key;
    }

    protected T _key;
}
