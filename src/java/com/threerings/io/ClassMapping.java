//
// $Id: ClassMapping.java,v 1.1 2002/07/23 05:42:34 mdb Exp $

package com.threerings.io;

/**
 * Used by the {@link ObjectOutputStream} and {@link ObjectInputStream} to
 * map classes to codes during a session.
 */
class ClassMapping
{
    public short code;
    public Class sclass;
    public Streamer streamer;

    public ClassMapping (short code, Class sclass, Streamer streamer)
    {
        this.code = code;
        this.sclass = sclass;
        this.streamer = streamer;
    }

    public String toString ()
    {
        return "[code=" + code + ", class=" + sclass.getName() + "]";
    }
}
