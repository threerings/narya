//
// $Id: GameConfig.java,v 1.7 2001/10/11 21:08:21 mdb Exp $

package com.threerings.parlor.game;

import java.io.IOException;
import java.io.DataInputStream;
import java.io.DataOutputStream;

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

    // documentation inherited
    public void writeTo (DataOutputStream out)
        throws IOException
    {
        super.writeTo(out);
        out.writeBoolean(rated);
    }

    // documentation inherited
    public void readFrom (DataInputStream in)
        throws IOException
    {
        super.readFrom(in);
        rated = in.readBoolean();
    }

    // documentation inherited
    protected void toString (StringBuffer buf)
    {
        super.toString(buf);
        buf.append(", rated=").append(rated);
    }
}
