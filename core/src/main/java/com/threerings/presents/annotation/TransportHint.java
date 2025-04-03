//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2025 Three Rings Design, Inc., All Rights Reserved
// https://github.com/threerings/narya/blob/master/LICENSE

package com.threerings.presents.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.threerings.presents.net.Transport;

/**
 * An annotation indicating the type of transport desired for a distributed object
 * class, field, or method.
 */
@Target({ ElementType.FIELD, ElementType.METHOD, ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface TransportHint
{
    /** The type of transport to use. */
    Transport.Type type () default Transport.Type.RELIABLE_ORDERED;

    /** For ordered transport types, the channel to use. */
    int channel () default 0;
}
