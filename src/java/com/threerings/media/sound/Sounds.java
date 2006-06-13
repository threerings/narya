//
// $Id$
//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2004 Three Rings Design, Inc., All Rights Reserved
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

package com.threerings.media.sound;

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
