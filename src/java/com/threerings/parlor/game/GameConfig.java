//
// $Id: GameConfig.java,v 1.1 2001/10/01 02:56:35 mdb Exp $

package com.threerings.parlor.data;

import java.io.IOException;
import java.io.DataInputStream;
import java.io.DataOutputStream;

import com.threerings.cocktail.cher.io.Streamable;

/**
 * The game config class encapsulates the configuration information for a
 * particular type of game. The hierarchy of game config objects mimics
 * the hierarchy of game managers and the client provides a game config
 * object to the game manager at game creation time which is used to set
 * the game's configuration options.
 *
 * <p> A game that has specific configuration needs would extend this
 * class (or an appropriate subclass) adding it's configuration
 * information, and also overriding {@link #writeTo} and {@link #readFrom}
 * to provide code to effect the necessary serialization and
 * unserialization of the added fields.
 *
 * <p> The implementation of the client-side of the game can either use
 * the code provided for obtaining configuration parameters from the user,
 * or provide its own specialized configuration interface. It need only
 * produce a game config object when the configuration process is complete
 * and provide that to the server along with the game creation request.
 */
public class GameConfig implements Streamable
{
    // documentation inherited
    public void writeTo (DataOutputStream out)
        throws IOException
    {
    }

    // documentation inherited
    public void readFrom (DataInputStream in)
        throws IOException
    {
    }
}
