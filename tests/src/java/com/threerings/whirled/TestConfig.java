//
// $Id: TestConfig.java,v 1.1 2001/10/05 23:59:37 mdb Exp $

package com.threerings.whirled.test;

import com.threerings.cocktail.party.data.PlaceConfig;
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
