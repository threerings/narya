//
// $Id: GameConfig.java,v 1.2 2001/10/01 06:19:15 mdb Exp $

package com.threerings.parlor.data;

import java.io.IOException;
import java.io.DataInputStream;
import java.io.DataOutputStream;

import com.threerings.cocktail.cher.io.Streamable;

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
 * #getControllerClass} and {@link getManagerClassName}, returning the
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
public abstract class GameConfig implements Streamable
{
    /** Indicates whether or not this game is rated. */
    public boolean rated = false;

    /**
     * Returns the class that should be used to create a controller for
     * this game. The controller class must derive from {@link
     * com.threerings.parlor.client.GameController}.
     */
    public abstract Class getControllerClass ();

    /**
     * Returns the name of the class that should be used to create a
     * manager for this game. The manager class must derive from {@link
     * com.threerings.parlor.server.GameManager}. <em>Note:</em> this
     * method differs from {@link #getControllerClass} because we want to
     * avoid compile time linkage of the game config object (which is used
     * on the client) to server code. This allows a code optimizer (DashO
     * Pro, for example) to remove the server code from the client,
     * knowing that it is never used.
     */
    public abstract String getManagerClassName ();

    // documentation inherited
    public void writeTo (DataOutputStream out)
        throws IOException
    {
        out.writeBoolean(rated);
    }

    // documentation inherited
    public void readFrom (DataInputStream in)
        throws IOException
    {
        rated = in.readBoolean();
    }

    /**
     * Generates a string representation of this object by calling the
     * overridable {@link #toString(StringBuffer)} which builds up the
     * string in a manner friendly to derived classes.
     */
    public String toString ()
    {
        StringBuffer buf = new StringBuffer();
        buf.append("[");
        toString(buf);
        buf.append("]");
        return buf.toString();
    }

    /**
     * An extensible mechanism for generating a string representation of
     * this object. Derived classes should override this method, calling
     * super and then appending their own data to the supplied string
     * buffer. The regular {@link #toString} function will call this
     * derived function to generate its string.
     */
    protected void toString (StringBuffer buf)
    {
        buf.append("type=").append(getClass().getName());
        buf.append("rated=").append(rated);
    }
}
