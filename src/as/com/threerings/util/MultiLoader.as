//
// $Id$

package com.threerings.util {

import flash.display.Loader;
import flash.display.LoaderInfo;

import flash.events.AsyncErrorEvent;
import flash.events.ErrorEvent;
import flash.events.Event;
import flash.events.IEventDispatcher;
import flash.events.IOErrorEvent;
import flash.events.SecurityErrorEvent;

import flash.net.URLRequest;

import flash.system.ApplicationDomain;
import flash.system.LoaderContext;

import flash.utils.ByteArray;
import flash.utils.Dictionary;

/**
 * Easy loader for many things, including managing multiple downloads.
 * More documentation coming.
 */
public class MultiLoader
{
    /**
     * Load one or more sources and return DisplayObjects.
     *
     * @param sources an Array, Dictionary, or Object containing sources as values, or a single
     * source value. The sources may be Strings (representing urls), URLRequests, ByteArrays,
     * or a Class that can be instantiated to become a URLRequest or ByteArray. Note that
     * the format of your sources Object dictates the format of the return Object.
     * @param completeCallback the function to call when complete. The signature should be:
     * <code>function (value :Object) :void</code>. Note that the structure of the return Object
     * is dictated by the sources parameter. If you pass in an Array, you get your results
     * in an Array. If you use a Dictionary or Object, the results will be returned as the same,
     * with the same keys used in sources now pointing to the results. If your sources parameter
     * was just a single source (like a String) then the result will just be a single result,
     * like a DisplayObject. Each result will be a DisplayObject or an Error
     * describing the problem.
     * @param forEach if true, each value or error will be returned as soon as possible. The values
     * or errors will be returned directly to the completeCallback. Any keys are lost, so you
     * probably only want to use this with an Array sources.
     * @param appDom the ApplicationDomain in which to load the contents, or null to specify
     * that it should load in a child of the current ApplicationDomain.
     * 
     * @example
     * <listing version="3.0">
     * MultiLoader.getContents(EMBED_CONSTANT, addChild);
     * </listing>
     *
     * @example
     * <listing version="3.0">
     * MultiLoader.getContents([EMBED1, EMBED2, EMBED3], addChild, true);
     * </listing>
     *
     * @example
     * <listing version="3.0">
     * function handleComplete (result :Object) :void {
     *    // process a result here. Result may be a DisplayObject or an Error.
     * };
     * 
     * var obj :Object = {
     *     key1: "http://somehost.com/someImage.gif",
     *     key2: "http://somehost.com/someOtherImage.gif"
     * };
     * 
     * MultiLoader.getContents(obj, handleComplete, true);
     * </listing>
     * 
     * @example
     * <listing version="3.0">
     * function handleComplete (results :Array) :void {
     *     // process results here
     * };
     *
     * MultiLoader.getContents([EMBED1, EMBED2, EMBED3], handleComplete);
     * </listing>
     */
    public static function getContents (
        sources :Object, completeCallback :Function, forEach :Boolean = false,
        appDom :ApplicationDomain = null) :void
    {
        var complete :Function = function (retval :Object) :void {
            completeCallback(processProperty(retval, Loader, "content"));
        };
        getLoaders(sources, complete, forEach, appDom);
    }

    /**
     * Exactly like getContents() only it returns the Loader objects rather than their contents.
     * @see getContents()
     */
    public static function getLoaders (
        sources :Object, completeCallback :Function, forEach :Boolean = false,
        appDom :ApplicationDomain = null) :void
    {
        var generator :Function = function (source :*) :Object {
            // first transform common sources to their more useful nature
            if (source is String) {
                source = new URLRequest(String(source));
            } else if (source is Class) {
                // it's probably a ByteArray from an Embed, but don't cast it
                source = new (source as Class)();
            }
            var l :Loader = new Loader();
            var lc :LoaderContext = new LoaderContext(false, appDom);
            // now we only need handle the two cases
            if (source is URLRequest) {
                l.load(URLRequest(source), lc);
            } else if (source is ByteArray) {
                l.loadBytes(ByteArray(source), lc);
            } else {
                return new Error("Unknown source: " + source);
            }
            return l.contentLoaderInfo;
        };

        var complete :Function = function (retval :Object) :void {
            completeCallback(processProperty(retval, LoaderInfo, "loader"));
        };

        new MultiLoader(sources, generator, complete, forEach);
    }

    /**
     * Coordinate loading some asynchronous objects.
     *
     * @param sources An Array, Dictionary, or Object of sources, or just a single source.
     * @param generatorFunciton a function to call to generate the loaders.
     * @param completeCallack the function to call when complete.
     * @param forEach whether to call the completeCallback for each source, or all-at-once at
     * the end. If forEach is used, keys will never be returned.
     * @param errorTypes an Array of event types that will be dispatched by the loader.
     * If unspecifed, all the normal error event types are used.
     * @param completeType, the event complete type. If unspecifed @default Event.COMPLETE.
     */
    public function MultiLoader (
        sources :Object, generatorFn :Function, completeCallback :Function,
        forEach :Boolean = false, errorTypes :Array = null, completeType :String = null)
    {
        if (errorTypes == null) {
            errorTypes = [ ErrorEvent.ERROR, AsyncErrorEvent.ASYNC_ERROR,
                IOErrorEvent.IO_ERROR, SecurityErrorEvent.SECURITY_ERROR ];
        }
        if (completeType == null) {
            completeType = Event.COMPLETE;
        }

        _complete = completeCallback;
        _forEach = forEach;

        if (sources is Array) {
            _result = new Array();

        } else if (sources is Dictionary) {
            _result = new Dictionary();

        } else {
            _result = new Object();
            if (!Util.isPlainObject(sources)) {
                // stash the singleton source
                sources = { singleton_key: sources };
            }
        }

        for (var key :* in sources) {
            var sourceVal :Object = sources[key];
            var val :Object;
            try {
                val = (sourceVal is Array) ? generatorFn.apply(null, sourceVal as Array)
                                           : generatorFn(sourceVal);
            } catch (err :Error) {
                val = err;
            }
            _result[key] = val;
            if (val is IEventDispatcher) {
                var ed :IEventDispatcher = IEventDispatcher(val);
                _remaining++;
                _targetsToKeys[ed] = key;
                ed.addEventListener(completeType, handleComplete);
                for each (var type :String in errorTypes) {
                    ed.addEventListener(type, handleError);
                }
            } else if (_forEach) {
                checkReport(key);
            }
        }

        if (!_forEach) {
            checkReport(null);
        }
    }

    protected function handleError (event :ErrorEvent) :void
    {
        _result[_targetsToKeys[event.target]] = new Error(event.text) // , event.errorID); ???
        handleComplete(event); // the rest is the same as complete
    }

    protected function handleComplete (event :Event) :void
    {
        _remaining--;
        checkReport(_targetsToKeys[event.target]);
    }

    protected function checkReport (key :*) :void
    {
        if (!_forEach && _remaining > 0) {
            return;
        }

        var thisResult :Object = (_forEach || (key === "singleton_key")) ? _result[key] : _result;
        try {
            _complete(thisResult);
        } catch (err :Error) {
            trace("MultiLoader: Could not call completeCallback: " + err);
        }
    }

    /**
     * Utility method used in this class.
     */
    protected static function processProperty (
        retval :Object, testClass :Class, prop :String) :Object
    {
        if (retval is testClass) {
            retval = retval[prop];
        } else {
            for (var key :* in retval) {
                var o :Object = retval[key];
                if (o is testClass) {
                    retval[key] = o[prop];
                } // else keep it the same
            }
        }
        return retval;
    }

    protected var _complete :Function;

    protected var _result :Object;

    protected var _targetsToKeys :Dictionary = new Dictionary(true);

    protected var _forEach :Boolean;

    protected var _remaining :int = 0;

}
}
