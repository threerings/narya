//
// $Id: DefaultSceneConfig.java,v 1.1 2001/11/12 20:56:55 mdb Exp $

package com.threerings.whirled.data;

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
    public Class getControllerClass ()
    {
        return SceneController.class;
    }

    // documentation inherited
    public String getManagerClassName ()
    {
        return "com.threerings.whirled.server.SceneManager";
    }
}
