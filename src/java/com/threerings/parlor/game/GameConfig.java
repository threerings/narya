//
// $Id: GameConfig.java,v 1.8 2002/03/26 22:58:31 mdb Exp $

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
 *
 * <p> A game that has specific configuration needs would extend this
 * class (or an appropriate subclass) adding it's configuration
 * information and overriding {@link #writeTo} and {@link #readFrom} to
 * provide code to serialize and unserialize the additional fields.
 */
public abstract class GameConfig extends PlaceConfig
{
    /** Indicates whether or not this game is rated. */
    public boolean rated = false;

    /**
     * Returns true if this game config object is equal to the supplied
     * object (meaning it is also a game config object and its
     * configuration settings are the same as ours).
     */
    public boolean equals (Object other)
    {
        if (other instanceof GameConfig) {
            GameConfig go = (GameConfig)other;
            return go.rated == rated;

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
        return (rated ? 1 : 0);
    }
}
