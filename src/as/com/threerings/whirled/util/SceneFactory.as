//
// $Id: SceneFactory.java 3099 2004-08-27 02:21:06Z mdb $
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

package com.threerings.whirled.util {

import com.threerings.crowd.data.PlaceConfig;
import com.threerings.whirled.data.Scene;
import com.threerings.whirled.data.SceneModel;

/**
 * This is used by the Whirled services to obtain a {@link Scene}
 * implementation given a scene model and associated data.
 */
public interface SceneFactory
{
    /**
     * Creates a {@link Scene} implementation given the supplied scene
     * model and place config.
     */
    function createScene (model :SceneModel, config :PlaceConfig) :Scene;
}
}
