//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2025 Three Rings Design, Inc., All Rights Reserved
// https://github.com/threerings/narya/blob/master/LICENSE

package com.threerings.io;

/**
 * Used by the {@link ObjectOutputStream} and {@link ObjectInputStream} to
 * map classes to codes during a session.
 */
class ClassMapping
{
    public short code;
    public Class<?> sclass;
    public Streamer streamer;

    public ClassMapping (short code, Class<?> sclass, Streamer streamer)
    {
        this.code = code;
        this.sclass = sclass;
        this.streamer = streamer;
    }

    @Override
    public String toString ()
    {
        return "[code=" + code + ", class=" + sclass.getName() + "]";
    }
}
