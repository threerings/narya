//
// $Id: ScenedBodyObject.java,v 1.1 2002/09/20 00:54:06 mdb Exp $

package com.threerings.whirled.data;

/**
 * A system that uses the whirled services must provide a body object
 * extension that implements this interface.
 */
public interface ScenedBodyObject
{
    /**
     * Returns the scene id currently occupied by this body.
     */
    public int getSceneId ();

    /**
     * Sets the scene id currently occupied by this body.
     */
    public void setSceneId (int sceneId);
}
