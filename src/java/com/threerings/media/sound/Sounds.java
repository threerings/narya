//
// $Id: Sounds.java,v 1.1 2002/11/22 21:54:49 mdb Exp $

package com.threerings.media;

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
     * Generates the key for the sound repository configuration file in
     * the package associated with the class. For example, if a the class
     * <code>com.threerings.happy.fun.GameSounds</code> were supplied to
     * this method, it would return
     * <code>com.threerings.happy.fun.sounds</code> which would reference
     * a <code>sounds.properties</code> file in the
     * <code>com.threerings.happy.fun</code> package.
     */
    protected static String makeKey (Class clazz)
    {
        String cname = clazz.getName();
        int didx = cname.lastIndexOf(".");
        return (didx != -1) ? cname.substring(0, didx) : "";
    }
}
