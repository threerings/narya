package com.threerings.presents.client {

import flash.util.describeType;

import mx.collections.ArrayCollection;

import com.threerings.util.Name;
import com.threerings.presents.Log;
import com.threerings.presents.data.TimeBaseMarshaller;
import com.threerings.presents.dobj.DSet;
import com.threerings.presents.dobj.QSet;
import com.threerings.presents.net.UsernamePasswordCreds;

import com.threerings.presents.dobj.DObjectManager;

import com.threerings.presents.net.BootstrapData;


public class TestClient extends Client
{
    public function TestClient ()
    {
        super(new UsernamePasswordCreds(new Name("Ray"), "fork-u-2"));
        setServer("tasman.sea.earth.threerings.net", DEFAULT_SERVER_PORT);
        logon();

        var g1 :String = null;
        var g2 :String = (com.threerings.util.Util.cast(g1, String) as String);
        Log.debug("foo: " + (g1 === g2) + ", *" + g2 + "*, "); // + g2.length);

        var ob :Object = "this is a string";
        Log.debug("part of an object: " + ob.substring(1));

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
        //testFunc(2);

        var bob :Person = new Person("Bob");
        var jim :Person = new Person("Jim");
        var bob2 :Person = new Person("Bob");
        bob.callPrintName(jim.printName);

        Log.debug("bob == jim: " + (bob == jim));
        Log.debug("bob === jim: " + (bob === jim));
        Log.debug("bob == bob2: " + (bob == bob2));
        Log.debug("bob === bob2: " + (bob === bob2));
        Log.debug("bob == bob: " + (bob == bob));
        Log.debug("bob === bob: " + (bob === bob));

        var list :ArrayCollection = new ArrayCollection();
        list.addItem(bob);
        Log.debug("jim's indeX: " + list.getItemIndex(jim));
        Log.debug("bob2's indeX: " + list.getItemIndex(bob2));
        Log.debug("function: " + describeType(testFunc).toXMLString());
        Log.debug("func length: " + testFunc.length);
        Log.debug("funcToString: " + testFunc);
        Log.debug("interfaces: " + describeType(Bint));

        var funcy :Function = function (i :int) :void {
            Log.debug("i is " + i);
            Log.debug("first list item is " + list[0]);
        }

        _savedFunc = funcy;
        _listy = list;


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

    public override function gotBootstrap (
            data :BootstrapData, omgr :DObjectManager) :void
    {
        _listy.addItemAt("new item 0", 0);
        super.gotBootstrap(data, omgr);
        _savedFunc(3);
    }

    // If a class isn't used anywhere, it won't get added to the .swf.
    // Here, I hack.
    public function fuckingCompiler () :void
    {
        var i :int = TimeBaseMarshaller.GET_TIME_OID;
    }

    public function testFunc (one :int, two :int = 0, three :int = -1) :void
    {
        Log.debug("this: " + ", args: " + arguments.length +
            ": " + arguments);
    }

    public function crazyArgs (num :int = 0, str :String = "lol") :void
    {
        Log.debug("crazyArgs " + num + ", " + str);
    }

    prototype var _foo :String;

    protected var _savedFunc :Function;
    protected var _listy :ArrayCollection;
}
}

import com.threerings.presents.Log;
import com.threerings.presents.client.TestClient;

interface Aint {
    function foo () :void;
}

interface Bint extends Aint {
    function bar () :void;
}

class Person
{
    public function Person (name :String)
    {
        _name = name;
    }

    public function printName () :void
    {
        Log.debug("printName called on " + _name + "! (this=" + this + ")");
    }

    public function callPrintName (funcy :Function) :void
    {
        funcy();
    }

    public function toString () :String
    {
        return "Person[" + _name + "]";
    }

    protected var _name :String;
}

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
