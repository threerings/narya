//
// $Id: DefaultSceneConfig.java 4026 2006-04-18 01:32:41Z mdb $
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

package com.threerings.whirled.data {

import com.threerings.crowd.data.PlaceConfig;
import com.threerings.whirled.client.SceneController;

/**
 * The default scene config simply causes the default scene manager and
 * controller to be created. A user of the Whirled services would most
 * likely extend the default scene config.
 *
 * <p> Note that this place config won't even work on the client side
 * because it instantiates a {@link SceneController} which is an abstract
 * class. It is used only for testing the server side and as a placeholder
 * in case standard scene configuration information is one day needed.
 */
public class DefaultSceneConfig extends PlaceConfig
{
    // documentation inherited
    public function getManagerClassName () :String
    {
        return "com.threerings.whirled.server.SceneManager";
    }
}
}
