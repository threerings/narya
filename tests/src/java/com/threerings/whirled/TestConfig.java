//
// $Id: TestConfig.java,v 1.3 2001/11/08 02:07:36 mdb Exp $

package com.threerings.whirled;

import com.threerings.crowd.data.PlaceConfig;
import com.threerings.whirled.server.SceneManager;

public class TestConfig extends PlaceConfig
{
    public Class getControllerClass ()
    {
        return TestController.class;
    }

    public String getManagerClassName ()
    {
        return SceneManager.class.getName();
    }
}
