//
// $Id: PlaceConfig.java,v 1.2 2001/10/11 04:07:51 mdb Exp $

package com.threerings.crowd.data;

import java.io.IOException;
import java.io.DataInputStream;
import java.io.DataOutputStream;

import com.threerings.presents.io.Streamable;

/**
 * The place config class encapsulates the configuration information for a
 * particular type of place. The hierarchy of place config objects mimics
 * the hierarchy of place managers and controllers. Both the place manager
 * and place controller are provided with the place config object when the
 * place is created.
 *
 * <p> The place config object is also the mechanism used to instantiate
 * the appropriate place manager and controller. Every place must have an
 * associated place config derived class that overrides {@link
 * #getControllerClass} and {@link #getManagerClassName}, returning the
 * appropriate place controller and manager class for that place.
 *
 * <p> A place that has specific configuration needs would extend this
 * class (or an appropriate subclass) adding it's configuration
 * information and overriding {@link #writeTo} and {@link #readFrom} to
 * provide code to serialize and unserialize the additional fields.
 */
public abstract class PlaceConfig implements Streamable
{
    /** The oid of the place object for which we represent the
     * configuration information. */
    public int placeOid;

    /**
     * Returns the class that should be used to create a controller for
     * this place. The controller class must derive from {@link
     * com.threerings.crowd.client.PlaceController}.
     */
    public abstract Class getControllerClass ();

    /**
     * Returns the name of the class that should be used to create a
     * manager for this place. The manager class must derive from {@link
     * com.threerings.crowd.server.PlaceManager}. <em>Note:</em>
     * this method differs from {@link #getControllerClass} because we
     * want to avoid compile time linkage of the place config object
     * (which is used on the client) to server code. This allows a code
     * optimizer (DashO Pro, for example) to remove the server code from
     * the client, knowing that it is never used.
     */
    public abstract String getManagerClassName ();

    // documentation inherited
    public void writeTo (DataOutputStream out)
        throws IOException
    {
        out.writeInt(placeOid);
    }

    // documentation inherited
    public void readFrom (DataInputStream in)
        throws IOException
    {
        placeOid = in.readInt();
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
    }
}
