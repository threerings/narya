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

package com.threerings.parlor.game;

import com.threerings.crowd.data.PlaceConfig;
import com.threerings.util.Name;

/**
 * The game config class encapsulates the configuration information for a
 * particular type of game. The hierarchy of game config objects mimics
 * the hierarchy of game managers and controllers. Both the game manager
 * and game controller are provided with the game config object when the
 * game is created.
 *
 * <p> The game config object is also the mechanism used to instantiate
 * the appropriate game manager and controller. Every game must have an
 * associated game config derived class that overrides {@link
 * #getControllerClass} and {@link #getManagerClassName}, returning the
 * appropriate game controller and manager class for that game. Thus the
 * entire chain of events that causes a particular game to be created is
 * the construction of the appropriate game config instance which is
 * provided to the server as part of an invitation or via some other
 * matchmaking mechanism.
 */
public abstract class GameConfig extends PlaceConfig implements Cloneable
{ 
    /** The usernames of the players involved in this game, or an empty
     * array if such information is not needed by this particular game. */
    public Name[] players = new Name[0];

    /** Indicates whether or not this game is rated. */
    public boolean rated = true;

    /**
     * Returns the game rating type.
     */
    public abstract byte getRatingTypeId ();
    
    /**
     * Returns a translatable label describing this game.
     */
    public abstract String getGameName ();
    
    /**
     * Returns the message bundle identifier for the bundle that should be
     * used to translate the translatable strings used to describe the
     * game config parameters.
     */
    public abstract String getBundleName ();
    
    /**
     * Returns the class that should be used to create a user interface
     * that can be used to configure this instance prior to starting the
     * game. The configurator class must derive from {@link
     * GameConfigurator}.
     */
    public abstract Class getConfiguratorClass ();

    /**
     * Returns true if this game config object is equal to the supplied
     * object (meaning it is also a game config object and its
     * configuration settings are the same as ours).
     */
    public boolean equals (Object other)
    {
        // make sure they're of the same class
        if (other.getClass() == this.getClass()) {
            GameConfig that = (GameConfig) other;
            return this.rated == that.rated;

        } else {
            return false;
        }
    }

    /**
     * Computes a hashcode for this game config object that supports our
     * {@link #equals} implementation. Objects that are equal should have
     * the same hashcode.
     */
    public int hashCode ()
    {
        // look ma, it's so sophisticated!
        return getClass().hashCode() + (rated ? 1 : 0);
    }
    
    // documentation inherited
    public Object clone ()
    {
        try {
            return super.clone();
        } catch (CloneNotSupportedException cnse) {
            throw new RuntimeException("clone() failed: " + cnse);
        }
    }
}
