package com.threerings.util {

import flash.display.Loader;
import flash.display.LoaderInfo;
import flash.errors.IllegalOperationError;
import flash.events.Event;
import flash.events.EventDispatcher;
import flash.events.IOErrorEvent;
import flash.utils.ByteArray;
import flash.system.ApplicationDomain;
import flash.system.LoaderContext;

/**
 * Allows you to load an embeded SWF stored as a Byte Array then access any stored classes
 * within the SWF.
 * Embed your swf like:
 * [Embed(source="foo.swf", mimeType="application/octet-stream"]
 * Then, instantiate that class and pass it to load() as a ByteArray.
 *
 * An Event.COMPLETE will be dispatched upon the successful completion of a call to
 * {@link load}.  IOErrorEvent.IO_ERROR will be dispatched if there's a problem reading the
 * ByteArray.
 *
 * @deprecated Content packs are coming, and symbols can be embedded directly
 * by using [Embed(source="foo.swf#somesymbol")]
 */
public class EmbeddedSwfLoader extends EventDispatcher 
{
    public function EmbeddedSwfLoader ()
    {
        _loader = new Loader();
        _loader.contentLoaderInfo.addEventListener(Event.COMPLETE, completeHandler);
        _loader.contentLoaderInfo.addEventListener(IOErrorEvent.IO_ERROR, ioErrorHandler);
    }

    /**
     * Load the SWF from a Byte Array.  A CLASS_LOADED event will be dispatched on successful 
     * completion of the load.  If any errors occur, a LOAD_ERROR event will be dispatched.
     */
    public function load (byteArray :ByteArray) :void
    {
        var context :LoaderContext = new LoaderContext();
        context.applicationDomain = ApplicationDomain.currentDomain;
        _loader.loadBytes(byteArray, context);
    }

    /**
     * Retrieves a class definition from the loaded swf.
     *
     * @throws IllegalOperationError when the swf has not completed loading or the class does
     * not exist
     */
    public function getClass (className :String) :Class
    {
        return getSymbol(className) as Class;
    }

    /**
     * Retrieves a Function definition from the loaded swf.
     *
     * @throws IllegalOperationError when the swf has not completed loading or the class does
     * not exist
     */
    public function getFunction (functionName :String) :Function
    {
        return getSymbol(functionName) as Function;
    }

    /**
     * Retrieves a symbol definition from the loaded swf.
     *
     * @throws IllegalOperationError when the swf has not completed loading or the class does
     * not exist
     */
    public function getSymbol (symbolName :String) :Object
    {
        if (!checkLoaded()) {
            throw new IllegalOperationError("SWF has not completed loading");
        }
        try {
            return _loader.contentLoaderInfo.applicationDomain.getDefinition(symbolName);
        } catch (e: Error) {
            throw new IllegalOperationError(symbolName + " definition not found");
        }
        return null;
    }

    /**
     * Checks if a symbol exists in the library.
     *
     * @throws IllegalOperationError when the swf has not completed loading.
     */
    public function isSymbol (className :String) :Boolean
    {
        if (!checkLoaded()) {
            throw new IllegalOperationError("SWF has not completed loading");
        }
        return _loader.contentLoaderInfo.applicationDomain.hasDefinition(className);
    }

    /**
     * Returns true if we've completed loaded the swf.
     */
    protected function checkLoaded () :Boolean
    {
        var loaderInfo :LoaderInfo = _loader.contentLoaderInfo;
        return !(loaderInfo.bytesTotal == 0 || loaderInfo.bytesLoaded != loaderInfo.bytesTotal);
    }

    protected function completeHandler (e :Event) :void
    {
        dispatchEvent(e);
    }

    protected function ioErrorHandler (e :Event) :void
    {
        dispatchEvent(e);
    }

    protected var _loader :Loader;
}
}
