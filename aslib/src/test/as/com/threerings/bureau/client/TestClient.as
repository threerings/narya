package com.threerings.bureau.client {

import com.threerings.bureau.data.BureauMarshaller;

public class TestClient extends BureauClient
{
    BureauMarshaller;

    /**
     * The main entry point for the bureau test client to be run in thane. Arguments:
     *   0: the token to use to log back into the server
     *   1: the bureau id of this instance
     *   2: the name of the server to log into
     *   3: the port to connect to on the server
     */
    public static function main (argv :Array) :void
    {
        if (argv.length != 4) {
            trace("Expected 4 arguments: (token) (bureauId) (server) (port)");
        }

        var token :String = argv[0];
        var bureauId :String = argv[1];
        var server :String = argv[2];
        var port :int = parseInt(argv[3]);

        trace("Token: " + token);
        trace("BureauId: " + bureauId);
        trace("Server: " + server);
        trace("Port: " + port + " (parsed from " + argv[3] + ")");

        // create the client and log on
        var client :TestClient = new TestClient(token, bureauId);
        client.setServer(server, [port]);
        client.logon();
    }

    public function TestClient (token :String, bureauId :String)
    {
        super(token, bureauId);
    }

    protected override function createDirector () :BureauDirector
    {
        return new TestDirector(_ctx);
    }
}

}

import com.threerings.bureau.client.Agent;
import com.threerings.bureau.client.BureauDirector;
import com.threerings.bureau.Log;
import com.threerings.bureau.data.AgentObject;
import com.threerings.bureau.util.BureauContext;

class TestAgent extends Agent
{
    public override function start () :void
    {
        Log.info("Starting agent " + _agentObj.getOid());
    }

    public override function stop () :void
    {
        Log.info("Stopping agent " + _agentObj.getOid());
    }
}

class TestDirector extends BureauDirector
{
    public function TestDirector (ctx :BureauContext)
    {
        super(ctx);
    }

    // just use our test agent exclusively - in the real world, the agent created would depend 
    // on the object's type and/or properties
    protected override function createAgent (agentObj :AgentObject) :Agent
    {
        return new TestAgent();
    }
}
