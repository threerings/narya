package com.threerings.util {

import flash.display.DisplayObject;

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
        var url :String = disp.root.loaderInfo.url;
        // normal parameters
        if (url == null || 0 != url.indexOf("file:")) {
            callback(disp.root.loaderInfo.parameters);
            return;
        }

        // instead read from XML
        var loader :URLLoader = new URLLoader();
        loader.addEventListener(IOErrorEvent.IO_ERROR,
            function (event :Event) :void {
                trace("Error loading params: " + event);
                callback(disp.root.loaderInfo.parameters);
            }
        );
        loader.addEventListener(Event.COMPLETE,
            function (event :Event) :void  {
                var data :XML = XML(loader.data);
                var params :Object = {};
                for each (var param :XML in data..param) {
                    params[param.@name] = String(param.@value);
                }
                callback(params);
            }
        );
        loader.load(new URLRequest("file:parameters.xml"));
    }
}
}
