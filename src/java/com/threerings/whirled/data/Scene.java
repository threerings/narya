//
// $Id: Scene.java,v 1.1 2001/08/11 04:09:50 mdb Exp $

package com.threerings.whirled.data;

/**
 * The base scene interface. This encapsulates the minimum information
 * needed about a scene in the Whirled system.
 */
public interface Scene
{
    public int getId ();

    public int getVersion ();

    public int[] getExitIds ();
}
