//
// $Id: SpotSceneController.java,v 1.1 2001/12/14 02:44:10 mdb Exp $

package com.threerings.whirled.spot.client;

import com.threerings.whirled.client.SceneController;

/**
 * The base spot scene controller class. It is expected that users of the
 * Whirled Spot services will extend this controller class when creating
 * specialized controllers for their scenes. Presently there are no basic
 * scene services provided by this controller, but its existence affords
 * the addition of such services should they become necessary in the
 * future.
 */
public abstract class SpotSceneController extends SceneController
{
}
