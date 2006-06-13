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

package com.threerings.whirled.data;

import com.threerings.crowd.data.PlaceConfig;

/**
 * This interface makes available basic scene information. At this basic
 * level, not much information is available, but extensions to this
 * interface begin to create a more comprehensive picture of a scene in a
 * system built from the Whirled services.
 */
public interface Scene
{
    /**
     * Returns the unique identifier for this scene.
     */
    public int getId ();

    /**
     * Returns the human readable name of this scene.
     */
    public String getName ();

    /**
     * Returns the version number of this scene.
     */
    public int getVersion ();

    /**
     * Returns the place config that can be used to determine which place
     * controller instance should be used to display this scene as well as
     * to obtain runtime configuration information.
     */
    public PlaceConfig getPlaceConfig ();

    /**
     * Sets this scene's unique identifier.
     */
    public void setId (int sceneId);

    /**
     * Sets the human readable name of this scene.
     */
    public void setName (String name);

    /**
     * Sets this scene's version number.
     */
    public void setVersion (int version);

    /**
     * Called to inform the scene that an update has been received while
     * the scene was resolved and active. The update should be applied to
     * the underlying scene model and any derivative data should be
     * appropriately updated.
     */
    public void updateReceived (SceneUpdate update);

    /**
     * Returns the scene model from which this scene was created.
     */
    public SceneModel getSceneModel ();
}
