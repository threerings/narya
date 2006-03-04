package com.threerings.presents.client {

import flash.util.describeType;

import com.threerings.util.Name;
import com.threerings.presents.Log;
import com.threerings.presents.data.TimeBaseMarshaller;
import com.threerings.presents.net.UsernamePasswordCreds;

public class TestClient extends Client
{
    public function TestClient ()
    {
        super(new UsernamePasswordCreds(new Name("Ray"), "fork-u-2"));
        setServer("tasman.sea.earth.threerings.net", DEFAULT_SERVER_PORT);
        logon();

        var a :Object = new HelperClass(this);

        Log.debug("instance: " + describeType(a).toXMLString());
        Log.debug("class   : " + describeType(HelperClass).toXMLString());

        /*
        var b :Object = new PooperClass();
        var c :Object = 2.4;
        var d :Object = new HooperClass(this);

        Log.debug("1) " + (a as HelperClass));
        Log.debug("2) " + (b as HelperClass));
        Log.debug("3) " + HelperClass(a));
        //Log.debug("4) " + HelperClass(b));
        Log.debug("5) " + (c as int));
        Log.debug("6) " + int(c));

        //Log.debug("7) " + (d === a));

        Log.debug("8) " + (a as HooperClass));
        Log.debug("9) " + HelperClass(d));
        //Log.debug("X) " + HooperClass(a));

        var x :HelperClass = HelperClass(d);
        var y :HelperClass = (d as HelperClass);
        var z :HooperClass = HooperClass(d);
        d.hype();
        x.hype();
        y.hype();
        z.hype();
        Log.debug("x === d: " + (x === d));
        Log.debug("y === d: " + (y === d));
        Log.debug("x === y: " + (x === y));
        Log.debug("d === z: " + (d === z));
        Log.debug("y === z: " + (y === z));
        */

        //var ta :TypedArray = new TypedArray(HooperClass);
        //var ta2 :TypedArray = new TypedArray(HelperClass);
        //var ta3 :TypedArray = new TypedArray(Pork);
    }


    // If a class isn't used anywhere, it won't get added to the .swf.
    // Here, I hack.
    public function fuckingCompiler () :void
    {
        var i :int = TimeBaseMarshaller.GET_TIME_OID;
    }

    public function b () :void
    {
    }
}
}

import com.threerings.presents.Log;
import com.threerings.presents.client.TestClient;

class HelperClass
{
    public function HelperClass (cli :TestClient)
    {
        _cli = cli;
        _cli.b();
    }

    public function hype () :void
    {
        Log.debug("helper hype");
    }

    protected var _cli :TestClient;
}

interface Pork
{
    function porker () :void;
}

final class HooperClass extends HelperClass
    implements Pork
{
    public function HooperClass (cli :TestClient)
    {
        super(cli);
    }

    public override function hype () :void
    {
        Log.debug("hooper hype");
    }

    public function porker () :void
    {
        // nada
    }
}

class PooperClass
{
    public function PooperClass ()
    {
    }
}
