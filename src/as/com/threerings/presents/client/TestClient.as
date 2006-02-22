package com.threerings.presents.client {

import com.threerings.util.Name;
import com.threerings.presents.net.UsernamePasswordCreds;

public class TestClient extends Client
{
    public function TestClient ()
    {
        super(new UsernamePasswordCreds(new Name("Ray"), "fork-u-2"));
        setServer("tasman.sea.earth.threerings.net", DEFAULT_SERVER_PORT);
        logon();
    }
}
}
