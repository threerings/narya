package com.threerings.presents.client {

import com.threerings.util.Name;
import com.threerings.presents.net.Credentials;

public class TestClient extends Client
{
    public function TestClient ()
    {
        super(new Credentials(new Name("Ray")));
        setServer("tasman.sea.earth.threerings.net", DEFAULT_SERVER_PORT);
        logon();
    }
}
}
