//
// $Id: SpotSceneController.java 3099 2004-08-27 02:21:06Z mdb $
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

package com.threerings.whirled.spot.client {

import com.threerings.whirled.client.SceneController;

/**
 * The base spot scene controller class. It is expected that users of the
 * Whirled Spot services will extend this controller class when creating
 * specialized controllers for their scenes. Presently there are no basic
 * scene services provided by this controller, but its existence affords
 * the addition of such services should they become necessary in the
 * future.
 */
public /*abstract*/ class SpotSceneController extends SceneController
{
}
}
