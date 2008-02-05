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
import flash.display.LoaderInfo;

import flash.events.Event;
import flash.events.IOErrorEvent;

import flash.net.URLLoader;
import flash.net.URLRequest;

/**
 * A utility for loading parameters from an XML file when run from
 * the local filesystem.
 *
 * The file "parameters.xml" should reside in the current directory and contains:
 *  <parameters>
 *      <param name="name1" value="val1"/>
 *      <param name="name2" value="val2"/>
 *  </parameters>
 */
public class ParameterUtil
{
    /**
     * Get the parameters.
     * Note: the callback function may be called prior to this method
     * returning.
     */
    public static function getParameters (disp :DisplayObject, callback :Function) :void
    {
        return getInfoParameters(disp.root.loaderInfo, callback);
    }

    /**
     * Get the parameters.
     * Note: the callback function may be called prior to this method
     * returning.
     */
    public static function getInfoParameters (loaderInfo :LoaderInfo, callback :Function) :void
    {
        // ensure that it's initialized...
        if (loaderInfo.url == null) {
            // Create a function to wait until the loaded object is initialized
            var initWaiter :Function = function (event :Event) :void {
                loaderInfo.removeEventListener(Event.INIT, initWaiter);
                if (loaderInfo.url != null) {
                    // re-call
                    getInfoParameters(loaderInfo, callback);

                } else {
                    // url is still null, don't infinite loop
                    logWarning("Unable to determine url, bailing");
                    callback(loaderInfo.parameters);
                }
            }; // end- initWaiter function

            // and wait.
            loaderInfo.addEventListener(Event.INIT, initWaiter);
            return;
        }

        // Simply use the parameters in the loaderInfo if we were not
        // loaded from a file.
        if (0 != loaderInfo.url.indexOf("file:")) {
            callback(loaderInfo.parameters);
            return;
        }

        // If we were loaded from a file, read our parameters from the
        // parameters.xml file.
        var loader :URLLoader = new URLLoader();
        loader.addEventListener(IOErrorEvent.IO_ERROR,
            function (event :Event) :void {
                logWarning("Error loading params: " + event);
                callback(loaderInfo.parameters);
            }
        );
        loader.addEventListener(Event.COMPLETE,
            function (event :Event) :void  {
                var data :XML = Util.newXML(loader.data);
                var params :Object = {};
                for each (var param :XML in data..param) {
                    params[param.@name] = String(param.@value);
                }
                callback(params);
            }
        );
        loader.load(new URLRequest("file:parameters.xml"));
    }

    /**
     * Convenience function to log a warning, since we don't keep around
     * a Log instance.
     */
    protected static function logWarning (msg :String) :void
    {
        Log.getLog(ParameterUtil).warning(msg);
    }
}
}
