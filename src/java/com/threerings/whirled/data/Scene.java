//
// $Id: Scene.java,v 1.2 2001/08/16 18:05:17 shaper Exp $

package com.threerings.whirled.data;

/**
 * The base scene interface. This encapsulates the minimum information
 * needed about a scene in the Whirled system.
 */
public interface Scene
{
    public int getId ();

    public int getVersion ();

    public int[] getPortalIds ();
}
