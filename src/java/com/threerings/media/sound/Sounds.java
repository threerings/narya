//
// $Id: Sounds.java,v 1.3 2002/11/27 00:08:36 ray Exp $

package com.threerings.media;

import java.io.File;

/**
 * A base class for sound repository classes. These would extend this
 * class and define keys for the various sounds that are mapped in the
 * properties file associated with that sound repository.
 */
public class Sounds
{
    /** The name of the sound repository configuration file. */
    public static final String PROP_NAME = "sounds";

    /**
     * Return the package path prefix of the supplied class.
     *
     * Generates the key for the sound repository configuration file in
     * the package associated with the class. For example, if a the class
     * <code>com.threerings.happy.fun.GameSounds</code> were supplied to
     * this method, it would return
     * <code>com/threerings/happy/fun/sounds/</code> which would reference
     * a <code>sounds.properties</code> file in the
     * <code>com.threerings.happy.fun</code> package.
     */
    protected static String getPackagePath (Class clazz)
    {
        return clazz.getPackage().getName().replace('.', '/') + "/";
    }
}
