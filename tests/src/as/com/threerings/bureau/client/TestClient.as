package com.threerings.bureau.client {

public class TestClient extends BureauClient
{
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
