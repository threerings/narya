//
// $Id$

package com.threerings.io;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import java.io.IOException;
import java.io.OutputStream;

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
     * Notes that the receiver has received the mappings for a group of classes, and thus that from
     * now on, only the class codes need be sent.
     */
    public void noteMappingsReceived (Collection<Class<?>> sclasses)
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

    @Override // documentation inherited
    protected ClassMapping createClassMapping (short code, Class<?> sclass, Streamer streamer)
    {
        // the negative class code indicates that we must rewrite the metadata for the first
        // instance in each go-round; when we are notified that the other side has cached the
        // mapping, we can simply write the (positive) code
        return new ClassMapping((short)(-code), sclass, streamer);
    }

    @Override // documentation inherited
    protected void writeNewClassMapping (ClassMapping cmap)
        throws IOException
    {
        writeClassMapping(cmap.code, cmap.sclass);
    }

    @Override // documentation inherited
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

    @Override // documentation inherited
    protected void writeClassMapping (int code, Class<?> sclass)
        throws IOException
    {
        super.writeClassMapping(code, sclass);

        // note that we've written the mapping
        _mappedClasses.add(sclass);
    }

    /** The set of classes for which we have written mappings. */
    protected Set<Class<?>> _mappedClasses = new HashSet<Class<?>>();
}
