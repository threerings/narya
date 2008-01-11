//
// $Id$
//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2007 Three Rings Design, Inc., All Rights Reserved
// http://www.threerings.net/code/narya/
//
// This library is free software; you can redistribute it and/or modify it
// under the terms of the GNU Lesser General Public License as published
// by the Free Software Foundation; either version 2.1 of the License, or
// (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA

package com.threerings.util {

import flash.display.DisplayObject;
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
 * Allows you to load an embeded SWF stored as a ByteArray then access any stored classes
 * within the SWF.
 * 
 * An Event.COMPLETE will be dispatched upon the successful completion of a call to
 * <code>load</code>.  IOErrorEvent.IO_ERROR will be dispatched if there's a problem reading the
 * ByteArray.
 *
 * Embed your swf like this (take note of the mimeType!):
 * <code><pre>
 * [Embed(source="foo.swf", mimeType="application/octet-stream")]
 * private static const FOO_RESOURCE :Class;
 * </pre></code>
 *
 * Then:
 *
 * <code><pre>
 * var loader :EmbeddedSwfLoader = new EmbeddedSwfLoader();
 * loader.addEventListener(Event.COMPLETE, handleComplete);
 * loader.addEventListener(IOErrorEvent.IO_ERROR, handleFailure);
 * loader.load(FOO_RESOURCE);
 *
 * ...
 * function handleComplete (event :Event) :void
 * {
 *     var myMovie = loader.getContent();
 * ...
 * </pre></code>
 *
 * @deprecated Content packs are coming, and symbols can be embedded directly
 * by using [Embed(source="foo.swf#somesymbol")]
 */
public class EmbeddedSwfLoader extends EventDispatcher 
{
    /**
     * Create an EmbeddedSwfLoader, good for loading one SWF from a ByteArray.
     *
     * @param useSubDomain if true, load the SWF and all its classes and symbols into a
     *  child ApplicationDomain. This means that they would be not normally reachable by the rest
     *  of your code after they're loaded, you would only be able to access the contents using
     *  this EmbeddedSwfLoader. This also means that those classes or symbols won't interfere
     *  with others loaded with the same name, and the classes can be garbage collected when
     *  no longer in use.
     */
    public function EmbeddedSwfLoader (useSubDomain :Boolean = false)
    {
        _useSubDomain = useSubDomain;
        _loader = new Loader();
        _loader.contentLoaderInfo.addEventListener(Event.COMPLETE, dispatchEvent);
        _loader.contentLoaderInfo.addEventListener(IOErrorEvent.IO_ERROR, dispatchEvent);
    }

    /**
     * Load the SWF from a ByteArray. A COMPLETE event will be dispatched on successful 
     * completion of the load. If any errors occur, a IO_ERROR event will be dispatched.
     *
     * @throws TypeError The parameter must be a ByteArray, or a Class that becomes one. If AS
     * had overloading, this wouldn't be necessary, we could catch these problems at compile time.
     *
     * @param byteArrayOrClass a flash.utils.ByteArray or a Class object that becomes one.
     */
    public function load (byteArrayOrClass :Object) :void
    {
        var byteArray :ByteArray = ByteArray( // this cast may throw a TypeError
            (byteArrayOrClass is Class) ? new (byteArrayOrClass as Class)() // voila!
                                        : byteArrayOrClass); // and wa-la!

        var context :LoaderContext = new LoaderContext();
        context.applicationDomain = _useSubDomain ? 
            new ApplicationDomain(ApplicationDomain.currentDomain) : 
            ApplicationDomain.currentDomain;
        _loader.loadBytes(byteArray, context);
    }

// This doesn't work. We cannot write parameters to the contentLoaderInfo.
// So: there is no way to pass parameters to content loaded using loadBytes,
// and there's no way to pass parameters to any other content without
// destroying caching (because you must append them to the url). Stupid flash.
//    /**
//     * Set a parameter accessible to the loaded content.
//     */
//    public function setParameter (name :String, val :String) :void
//    {
//        _loader.contentLoaderInfo.parameters[name] = val;
//    }

    /**
     * Get the top-level display object defined in the loaded SWF.
     *
     * @throws IllegalOperationError if the SWF is not yet completely loaded.
     */
    public function getContent () :DisplayObject
    {
        checkLoaded();
        return _loader.content;
    }

    /**
     * Retrieves a class definition from the loaded swf.
     *
     * @throws flash.errors.IllegalOperationError when the swf has not completed loading
     * or the class does not exist.
     */
    public function getClass (className :String) :Class
    {
        return getSymbol(className) as Class;
    }

    /**
     * Retrieves a Function definition from the loaded swf.
     *
     * @throws flash.errors.IllegalOperationError when the swf has not completed loading
     * or the function does not exist.
     */
    public function getFunction (functionName :String) :Function
    {
        return getSymbol(functionName) as Function;
    }

    /**
     * Retrieves a symbol definition from the loaded swf.
     *
     * @throws flash.errors.IllegalOperationError when the swf has not completed loading
     * or the symbol does not exist.
     */
    public function getSymbol (symbolName :String) :Object
    {
        checkLoaded();
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
     * @throws flash.errors.IllegalOperationError when the swf has not completed loading.
     */
    public function isSymbol (className :String) :Boolean
    {
        checkLoaded();
        return _loader.contentLoaderInfo.applicationDomain.hasDefinition(className);
    }

    /**
     * Validate that the load operation is complete.
     */
    protected function checkLoaded () :void
    {
        var loaderInfo :LoaderInfo = _loader.contentLoaderInfo;
        if (loaderInfo.bytesTotal == 0 || loaderInfo.bytesLoaded != loaderInfo.bytesTotal) {
            throw new IllegalOperationError("SWF has not completed loading.");
        }
    }

    protected var _loader :Loader;
    protected var _useSubDomain :Boolean;
}
}
