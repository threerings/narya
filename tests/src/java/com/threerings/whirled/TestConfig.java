//
// $Id: TestConfig.java,v 1.2 2001/10/11 04:07:54 mdb Exp $

package com.threerings.whirled.test;

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
