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

        var g1 :String = null;
        var g2 :String = String(g1);
        Log.debug("foo: " + (g1 === g2) + ", *" + g2 + "*, " + g2.length);

        var duckie :Duck = new Goose();
        duckie.screw();

        var arr :Array = new Array();
        arr[0] = "Florp";
        arr[1] = "Blanger";
        arr[2] = "Swissbrat";

        for (var key:* in arr) {
            Log.debug("a key=" + key + " -> " +  arr[key]);
        }
        delete arr[1];
        arr["1"] = "oinkenheimer";
        for (var key:* in arr) {
            Log.debug("a key=" + key + " -> " +  arr[key]);
        }
        Log.debug("length: " + arr.length);
        Log.debug("arr[0]: " + arr[0]);
        Log.debug("arr[1]: " + arr[1]);
        Log.debug("arr[2]: " + arr[2]);

        /*
        var a :Object = new OldClass();
        var c :Class = OldClass;

        Log.debug("instance: " + describeType(a).toXMLString());
        Log.debug("class   : " + describeType(Pork).toXMLString());
        */

        /*
        var b :Object = new OldClass();
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
}
}

import com.threerings.presents.Log;
import com.threerings.presents.client.TestClient;

dynamic class OldClass
{
    public function OldClass ()
    {
    }
}

class HelperClass
{
    public function HelperClass (cli :TestClient)
    {
        _cli = cli;
        //_cli.b();
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
