//
// $Id: GameConfig.java,v 1.16 2004/02/25 14:44:54 mdb Exp $

package com.threerings.parlor.game;

import com.threerings.crowd.data.PlaceConfig;

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
public abstract class GameConfig extends PlaceConfig
{
    /** The usernames of the players involved in this game, or an empty
     * array if such information is not needed by this particular game. */
    public String[] players = new String[0];

    /** Indicates whether or not this game is rated. */
    public boolean rated = true;

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
}
