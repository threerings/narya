//
// $Id: SceneController.java,v 1.1 2001/11/12 20:56:55 mdb Exp $

package com.threerings.whirled.client;

import com.threerings.crowd.client.PlaceController;

/**
 * The base scene controller class. It is expected that users of the
 * Whirled services will extend this controller class when creating
 * specialized controllers for their scenes. Presently there are no basic
 * scene services provided by this controller, but its existence affords
 * the addition of such services should they become necessary in the
 * future.
 */
public abstract class SceneController extends PlaceController
{
}
