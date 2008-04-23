//
// $Id: StreamableHashSet.java 4893 2007-12-03 21:07:34Z dhoover $
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

package com.threerings.util;

import java.io.IOException;
import java.util.AbstractSet;
import java.util.Arrays;
import java.util.ConcurrentModificationException;
import java.util.EnumSet;
import java.util.Iterator;

import com.threerings.io.ObjectInputStream;
import com.threerings.io.ObjectOutputStream;
import com.threerings.io.Streamable;

/**
 * An {@link EnumSet} equivalent (not a subclass, because EnumSet's implementation is private)
 * that can be streamed.
 *
 * @see Streamable
 */
public class StreamableEnumSet<E extends Enum<E>> extends AbstractSet<E>
    implements Cloneable, Streamable
{
    /**
     * Creates a new, empty enum set for storing elements of the specified class.
     */
    public StreamableEnumSet (Class<E> elementType)
    {
        _elementType = elementType;
        initContents();
    }

    /**
     * No-arg constructor for deserialization.
     */
    public StreamableEnumSet ()
    {
    }

    @Override // documentation inherited
    public Iterator<E> iterator ()
    {
        return new Iterator<E>() {
            public boolean hasNext () {
                checkConcurrentModification();
                return _count < _size;
            }
            public E next () {
                checkConcurrentModification();
                do {
                    _idx += (++_bit >> 3);
                    _bit &= 0x07;
                } while ((_contents[_idx] & (1 << _bit)) == 0);
                _count++;
                return _elementType.getEnumConstants()[(_idx << 3) | _bit];
            }
            public void remove () {
                checkConcurrentModification();
                _contents[_idx] &= ~(1 << _bit);
                _size--;
                _count--;
                _omodcount = ++_modcount;
            }
            protected void checkConcurrentModification () {
                if (_modcount != _omodcount) {
                    throw new ConcurrentModificationException();
                }
            }
            protected int _idx, _bit = -1;
            protected int _count;
            protected int _omodcount = _modcount;
        };
    }

    @Override // documentation inherited
    public int size ()
    {
        return _size;
    }

    @Override // documentation inherited
    public boolean contains (Object o)
    {
        if (!_elementType.isInstance(o)) {
            return false;
        }
        int ordinal = ((Enum)o).ordinal();
        int idx = ordinal >> 3, mask = 1 << (ordinal & 0x07);
        return (_contents[idx] & mask) != 0;
    }

    @Override // documentation inherited
    public boolean add (E o)
    {
        int ordinal = _elementType.cast(o).ordinal();
        int idx = ordinal >> 3, mask = 1 << (ordinal & 0x07);
        if ((_contents[idx] & mask) == 0) {
            _contents[idx] |= mask;
            _size++;
            _modcount++;
            return true;
        }
        return false;
    }

    @Override // documentation inherited
    public boolean remove (Object o)
    {
        if (!_elementType.isInstance(o)) {
            return false;
        }
        int ordinal = ((Enum)o).ordinal();
        int idx = ordinal >> 3, mask = 1 << (ordinal & 0x07);
        if ((_contents[idx] & mask) != 0) {
            _contents[idx] &= ~mask;
            _size--;
            _modcount++;
            return true;
        }
        return false;
    }

    @Override // documentation inherited
    public void clear ()
    {
        Arrays.fill(_contents, (byte)0);
        _size = 0;
        _modcount++;
    }

    @Override // documentation inherited
    public Object clone ()
    {
        try {
            // make a deep clone of the contents
            StreamableEnumSet cset = (StreamableEnumSet)super.clone();
            cset._contents = _contents.clone();
            return cset;

        } catch (CloneNotSupportedException e) {
            return null; // won't happen
        }
    }

    /**
     * Writes our custom streamable fields.
     */
    public void writeObject (ObjectOutputStream out)
        throws IOException
    {
        if (!classDefinesElementType()) {
            out.writeUTF(_elementType.getName());
        }
        out.write(_contents);
    }

    /**
     * Reads our custom streamable fields.
     */
    public void readObject (ObjectInputStream in)
        throws IOException, ClassNotFoundException
    {
        if (!classDefinesElementType()) {
            @SuppressWarnings("unchecked") Class<E> elementType =
                (Class<E>)Class.forName(in.readUTF());
            _elementType = elementType;

        } else if (_elementType == null) {
            throw new RuntimeException("No element type defined in constructor.");
        }
        initContents();
        in.read(_contents);
        updateSize();
    }

    /**
     * Subclasses that only store elements of a single enum type (initialized in their no-arg
     * constructors) can return <code>true</code> here to avoid the overhead of streaming the
     * enum type for each instance.
     */
    protected boolean classDefinesElementType ()
    {
        return false;
    }

    /**
     * Creates the contents array.
     */
    protected void initContents ()
    {
        int constants = _elementType.getEnumConstants().length;
        _contents = new byte[(constants >> 3) + ((constants & 0x07) == 0 ? 0 : 1)];
    }

    /**
     * Computes the size from the contents.
     */
    protected void updateSize ()
    {
        _size = 0;
        for (byte b : _contents) {
            _size += Integer.bitCount(b & 0xFF);
        }
    }

    /** The element type. */
    protected Class<E> _elementType;

    /** A byte array with bits set for each element in the set. */
    protected byte[] _contents;

    /** The number of elements in the set. */
    protected int _size;

    /** The modification count (used to detect concurrent modifications). */
    protected int _modcount;
}
