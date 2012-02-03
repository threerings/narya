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

package com.threerings.io;

import java.util.Collection;
import java.util.Set;

import java.io.IOException;
import java.io.OutputStream;

import com.google.common.collect.Sets;

/**
 * Extends {@link ObjectOutputStream} for use in unreliable channels, where we must transmit class
 * mappings with every object until we are explicitly notified that the receiver has cached the
 * mappings.
 */
public class UnreliableObjectOutputStream extends ObjectOutputStream
{
    /**
     * Constructs an object output stream which will write its data to the supplied target stream.
     */
    public UnreliableObjectOutputStream (OutputStream target)
    {
        super(target);
    }

    /**
     * Sets the reference to the set that will hold the classes for which mappings have been
     * written.
     */
    public void setMappedClasses (Set<Class<?>> mappedClasses)
    {
        _mappedClasses = mappedClasses;
    }

    /**
     * Returns a reference to the set of classes for which mappings have been written.
     */
    public Set<Class<?>> getMappedClasses ()
    {
        return _mappedClasses;
    }

    /**
     * Sets the reference to the set that will hold the pooled strings for which mappings have been
     * written.
     */
    public void setMappedInterns (Set<String> mappedInterns)
    {
        _mappedInterns = mappedInterns;
    }

    /**
     * Returns a reference to the set of pooled strings for which mappings have been written.
     */
    public Set<String> getMappedInterns ()
    {
        return _mappedInterns;
    }

    /**
     * Notes that the receiver has received the mappings for a group of classes and thus that from
     * now on, only the codes need be sent.
     */
    public void noteClassMappingsReceived (Collection<Class<?>> sclasses)
    {
        // sanity check
        if (_classmap == null) {
            throw new RuntimeException("Missing class map");
        }

        // make each class's code positive to signify that we no longer need to send metadata
        for (Class<?> sclass : sclasses) {
            ClassMapping cmap = _classmap.get(sclass);
            if (cmap == null) {
                throw new RuntimeException("No class mapping for " + sclass.getName());
            }
            cmap.code = (short)Math.abs(cmap.code);
        }
    }

    /**
     * Notes that the receiver has received the mappings for a group of interns and thus that from
     * now on, only the codes need be sent.
     */
    public void noteInternMappingsReceived (Collection<String> sinterns)
    {
        // sanity check
        if (_internmap == null) {
            throw new RuntimeException("Missing intern map");
        }

        // make each intern's code positive to signify that we no longer need to send metadata
        for (String sintern : sinterns) {
            Short code = _internmap.get(sintern);
            if (code == null) {
                throw new RuntimeException("No intern mapping for " + sintern);
            }
            short scode = code;
            if (scode < 0) {
                _internmap.put(sintern, (short)-code);
            }
        }
    }

    @Override
    protected Short createInternMapping (short code)
    {
        // the negative intern code indicates that we must rewrite the metadata for the first
        // instance in each go-round; when we are notified that the other side has cached the
        // mapping, we can simply write the (positive) code
        return (short)-code;
    }

    @Override
    protected void writeNewInternMapping (short code, String value)
        throws IOException
    {
        writeInternMapping(code, value);
    }

    @Override
    protected void writeExistingInternMapping (short code, String value)
        throws IOException
    {
        // if the other side has received the mapping, we need only send the reference
        if (code > 0) {
            writeShort(code);

        // likewise if we've written the value once this go-round
        } else if (_mappedInterns.contains(value)) {
            writeShort(-code);

        // otherwise, we must retransmit the mapping
        } else {
            writeInternMapping(code, value);
        }
    }

    @Override
    protected void writeInternMapping (int code, String value)
        throws IOException
    {
        super.writeInternMapping(code, value);

        // note that we've written the mapping
        _mappedInterns.add(value);
    }

    @Override
    protected ClassMapping createClassMapping (short code, Class<?> sclass, Streamer streamer)
    {
        // the negative class code indicates that we must rewrite the metadata for the first
        // instance in each go-round; when we are notified that the other side has cached the
        // mapping, we can simply write the (positive) code
        return new ClassMapping((short)(-code), sclass, streamer);
    }

    @Override
    protected void writeNewClassMapping (ClassMapping cmap)
        throws IOException
    {
        writeClassMapping(cmap.code, cmap.sclass);
    }

    @Override
    protected void writeExistingClassMapping (ClassMapping cmap)
        throws IOException
    {
        // if the other side has received the mapping, we need only send the reference
        if (cmap.code > 0) {
            writeShort(cmap.code);

        // likewise if we've written the class once this go-round
        } else if (_mappedClasses.contains(cmap.sclass)) {
            writeShort(-cmap.code);

        // otherwise, we must retransmit the mapping
        } else {
            writeClassMapping(cmap.code, cmap.sclass);
        }
    }

    @Override
    protected void writeClassMapping (int code, Class<?> sclass)
        throws IOException
    {
        super.writeClassMapping(code, sclass);

        // note that we've written the mapping
        _mappedClasses.add(sclass);
    }

    /** The set of classes for which we have written mappings. */
    protected Set<Class<?>> _mappedClasses = Sets.newHashSet();

    /** The set of pooled strings for which we have written mappings. */
    protected Set<String> _mappedInterns = Sets.newHashSet();
}
