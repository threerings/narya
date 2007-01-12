package com.threerings.util {

import flash.display.Loader;
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
 */
public class EmbededClassLoader extends EventDispatcher 
{
    public static var SWF_LOADED :String = "swfLoaded";
    public static var LOAD_ERROR :String = "loadError";

    public function EmbededClassLoader ()
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
        if (_loader.contentLoaderInfo.bytesLoaded != _loader.contentLoaderInfo.bytesTotal) {
            throw new IllegalOperationError("SWF has not completed loading");
        }
        try {
            return _loader.contentLoaderInfo.applicationDomain.getDefinition(className) as Class;
        } catch (e: Error) {
            throw new IllegalOperationError(className + " definition not found");
        }
        return null;
    }

    /**
     * Checks if a class exists in the library.
     *
     * @throws IllegalOperationError when the swf has not completed loading.
     */
    public function isClass (className :String) :Boolean
    {
        if (_loader.contentLoaderInfo.bytesLoaded != _loader.contentLoaderInfo.bytesTotal) {
            throw new IllegalOperationError("SWF has not completed loading");
        }
        return _loader.contentLoaderInfo.applicationDomain.hasDefinition(className);
    }

    protected function completeHandler (e :Event) :void
    {
        dispatchEvent(new Event(SWF_LOADED));
    }

    protected function ioErrorHandler (E :Event) :void
    {
        dispatchEvent(new Event(LOAD_ERROR));
    }

    protected var _loader :Loader;
}
}
