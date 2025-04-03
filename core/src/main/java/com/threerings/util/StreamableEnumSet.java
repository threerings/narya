//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2025 Three Rings Design, Inc., All Rights Reserved
// https://github.com/threerings/narya/blob/master/LICENSE

package com.threerings.util;

import java.util.AbstractSet;
import java.util.Arrays;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.EnumSet;
import java.util.Iterator;

import java.io.IOException;

import com.threerings.io.ObjectInputStream;
import com.threerings.io.ObjectOutputStream;
import com.threerings.io.Streamable;

/**
 * An {@link EnumSet} equivalent (not a subclass, because EnumSet's implementation is private)
 * that can be streamed.
 *
 * @see Streamable
 * @param <E> the type of enum being stored in this set.
 */
public class StreamableEnumSet<E extends Enum<E>> extends AbstractSet<E>
    implements Cloneable, Streamable
{
    /**
     * Creates an empty set of the specified type.
     */
    public static <E extends Enum<E>> StreamableEnumSet<E> noneOf (Class<E> elementType)
    {
        return new StreamableEnumSet<E>(elementType);
    }

    /**
     * Creates a set containing all elements of the specified type.
     */
    public static <E extends Enum<E>> StreamableEnumSet<E> allOf (Class<E> elementType)
    {
        StreamableEnumSet<E> set = new StreamableEnumSet<E>(elementType);
        for (E constant : elementType.getEnumConstants()) {
            set.add(constant);
        }
        return set;
    }

    /**
     * Creates a set containing all elements in the collection provided (which must have at least
     * one element, unless it is a <code>StreamableEnumSet</code>).
     */
    public static <E extends Enum<E>> StreamableEnumSet<E> copyOf (Collection<E> s)
    {
        if (s instanceof StreamableEnumSet<?>) {
            StreamableEnumSet<E> set = (StreamableEnumSet<E>)s;
            return copyOf(set);
        }
        if (s.isEmpty()) {
            throw new IllegalArgumentException("Collection must have at least one element.");
        }
        StreamableEnumSet<E> set = new StreamableEnumSet<E>(
            s.iterator().next().getDeclaringClass());
        set.addAll(s);
        return set;
    }

    /**
     * Creates a set containing all elements in the set provided.
     */
    public static <E extends Enum<E>> StreamableEnumSet<E> copyOf (StreamableEnumSet<E> s)
    {
        return s.clone();
    }

    /**
     * Creates a set containing all elements <em>not</em> in the set provided.
     */
    public static <E extends Enum<E>> StreamableEnumSet<E> complementOf (StreamableEnumSet<E> s)
    {
        Class<E> elementType = s._elementType;
        StreamableEnumSet<E> set = new StreamableEnumSet<E>(elementType);
        for (E constant : elementType.getEnumConstants()) {
            if (!s.contains(constant)) {
                set.add(constant);
            }
        }
        return set;
    }

    /**
     * Creates a set consisting of the specified elements.
     */
    public static <E extends Enum<E>> StreamableEnumSet<E> of (E first, E... rest)
    {
        StreamableEnumSet<E> set = new StreamableEnumSet<E>(first.getDeclaringClass());
        set.add(first);
        for (E e : rest) {
            set.add(e);
        }
        return set;
    }

    /**
     * Creates a set that includes all enum constants in the specified (inclusive) range.
     */
    public static <E extends Enum<E>> StreamableEnumSet<E> range (E from, E to)
    {
        Class<E> elementType = from.getDeclaringClass();
        StreamableEnumSet<E> set = new StreamableEnumSet<E>(elementType);
        E[] constants = elementType.getEnumConstants();
        for (int ii = from.ordinal(), last = to.ordinal(); ii <= last; ii++) {
            set.add(constants[ii]);
        }
        return set;
    }

    /**
     * Creates a new, empty enum set for storing elements of the specified class.
     */
    public StreamableEnumSet (Class<E> elementType)
    {
        _elementType = elementType;
        initContents();
    }

    @Override
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

    @Override
    public int size ()
    {
        return _size;
    }

    @Override
    public boolean contains (Object o)
    {
        if (!_elementType.isInstance(o)) {
            return false;
        }
        int ordinal = ((Enum<?>)o).ordinal();
        int idx = ordinal >> 3, mask = 1 << (ordinal & 0x07);
        return (_contents[idx] & mask) != 0;
    }

    @Override
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

    @Override
    public boolean remove (Object o)
    {
        if (!_elementType.isInstance(o)) {
            return false;
        }
        int ordinal = ((Enum<?>)o).ordinal();
        int idx = ordinal >> 3, mask = 1 << (ordinal & 0x07);
        if ((_contents[idx] & mask) != 0) {
            _contents[idx] &= ~mask;
            _size--;
            _modcount++;
            return true;
        }
        return false;
    }

    @Override
    public void clear ()
    {
        Arrays.fill(_contents, (byte)0);
        _size = 0;
        _modcount++;
    }

    @Override
    public StreamableEnumSet<E> clone ()
    {
        try {
            // make a deep clone of the contents
            @SuppressWarnings("unchecked")
            StreamableEnumSet<E> cset = (StreamableEnumSet<E>)super.clone();
            cset._contents = _contents.clone();
            return cset;

        } catch (CloneNotSupportedException e) {
            throw new AssertionError(e); // won't happen
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

        // count set bits to initialize size
        for (byte b : _contents) {
            _size += Integer.bitCount(b & 0xFF);
        }
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

    /** The element type. */
    protected Class<E> _elementType;

    /** A byte array with bits set for each element in the set. */
    protected byte[] _contents;

    /** The number of elements in the set. */
    protected int _size;

    /** The modification count (used to detect concurrent modifications). */
    protected int _modcount;
}
