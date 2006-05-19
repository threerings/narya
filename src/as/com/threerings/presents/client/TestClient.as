package com.threerings.presents.client {

import flash.utils.describeType;

import mx.collections.ArrayCollection;

import com.threerings.util.Name;
import com.threerings.presents.data.TimeBaseMarshaller;
import com.threerings.presents.dobj.DSet;
import com.threerings.presents.dobj.QSet;
import com.threerings.presents.net.UsernamePasswordCreds;

import com.threerings.presents.dobj.DObjectManager;

import com.threerings.presents.net.BootstrapData;


public class TestClient extends Client
{
    private static const log :Log = Log.getLog(TestClient);

    public function TestClient ()
    {
        super(new UsernamePasswordCreds(new Name("Ray"), "fork-u-2"));
        setServer("tasman.sea.earth.threerings.net", DEFAULT_SERVER_PORT);
        logon();

        var g1 :String = null;
        var g2 :String = (com.threerings.util.Util.cast(g1, String) as String);
        log.debug("foo: " + (g1 === g2) + ", *" + g2 + "*, "); // + g2.length);

        var ob :Object = "this is a string";
        log.debug("part of an object: " + ob.substring(1));

        var arr :Array = new Array();
        arr[0] = "Florp";
        arr[1] = "Blanger";
        arr[2] = "Swissbrat";

        for (var key:* in arr) {
            log.debug("a key=" + key + " -> " +  arr[key]);
        }
        delete arr[1];
        arr["1"] = "oinkenheimer";
        for (var key:* in arr) {
            log.debug("a key=" + key + " -> " +  arr[key]);
        }
        log.debug("length: " + arr.length);
        log.debug("arr[0]: " + arr[0]);
        log.debug("arr[1]: " + arr[1]);
        log.debug("arr[2]: " + arr[2]);
        //testFunc(2);

        var bob :Person = new Person("Bob");
        var jim :Person = new Person("Jim");
        var bob2 :Person = new Person("Bob");
        bob.callPrintName(jim.printName);

        log.debug("bob == jim: " + (bob == jim));
        log.debug("bob === jim: " + (bob === jim));
        log.debug("bob == bob2: " + (bob == bob2));
        log.debug("bob === bob2: " + (bob === bob2));
        log.debug("bob == bob: " + (bob == bob));
        log.debug("bob === bob: " + (bob === bob));

        var list :ArrayCollection = new ArrayCollection();
        list.addItem(bob);
        log.debug("jim's indeX: " + list.getItemIndex(jim));
        log.debug("bob2's indeX: " + list.getItemIndex(bob2));
        log.debug("function: " + describeType(testFunc).toXMLString());
        log.debug("func length: " + testFunc.length);
        log.debug("funcToString: " + testFunc);
        log.debug("interfaces: " + describeType(Bint));

        var funcy :Function = function (i :int) :void {
            log.debug("i is " + i);
            log.debug("first list item is " + list[0]);
        }

        _savedFunc = funcy;
        _listy = list;


        /*
        var a :Object = new OldClass();
        var c :Class = OldClass;

        log.debug("instance: " + describeType(a).toXMLString());
        log.debug("class   : " + describeType(Pork).toXMLString());
        */

        /*
        var b :Object = new OldClass();
        var c :Object = 2.4;
        var d :Object = new HooperClass(this);

        log.debug("1) " + (a as HelperClass));
        log.debug("2) " + (b as HelperClass));
        log.debug("3) " + HelperClass(a));
        //log.debug("4) " + HelperClass(b));
        log.debug("5) " + (c as int));
        log.debug("6) " + int(c));

        //log.debug("7) " + (d === a));

        log.debug("8) " + (a as HooperClass));
        log.debug("9) " + HelperClass(d));
        //log.debug("X) " + HooperClass(a));

        var x :HelperClass = HelperClass(d);
        var y :HelperClass = (d as HelperClass);
        var z :HooperClass = HooperClass(d);
        d.hype();
        x.hype();
        y.hype();
        z.hype();
        log.debug("x === d: " + (x === d));
        log.debug("y === d: " + (y === d));
        log.debug("x === y: " + (x === y));
        log.debug("d === z: " + (d === z));
        log.debug("y === z: " + (y === z));
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
        log.debug("this: " + ", args: " + arguments.length +
            ": " + arguments);
    }

    public function crazyArgs (num :int = 0, str :String = "lol") :void
    {
        log.debug("crazyArgs " + num + ", " + str);
    }

    prototype var _foo :String;

    protected var _savedFunc :Function;
    protected var _listy :ArrayCollection;
}
}

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
        Log.getLog(this).debug(
            "printName called on " + _name + "! (this=" + this + ")");
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
        Log.getLog(this).debug("helper hype");
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
        Log.getLog(this).debug("hooper hype");
    }

    public function porker () :void
    {
        // nada
    }
}
