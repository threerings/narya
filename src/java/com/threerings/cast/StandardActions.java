//
// $Id: StandardActions.java,v 1.1 2001/11/27 08:09:35 mdb Exp $

package com.threerings.cast;

/**
 * Actions are referenced by name and this interface defines constants for
 * two standard actions: standing and walking. Because character sprites
 * follow paths, it is helpful for them to take care of switching between
 * the standing and walking actions automatically.
 */
public interface StandardActions
{
    /** The name of the standard standing action. */
    public static final String STANDING = "standing";

    /** The name of the standard walking action. */
    public static final String WALKING = "walking";
}
