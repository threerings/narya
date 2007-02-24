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

import flash.events.NetStatusEvent;

import flash.net.SharedObject;
import flash.net.SharedObjectFlushStatus;

public class Config
{
    /**
     * Constructs a new config object which will obtain configuration
     * information from the specified path.
     */
    public function Config (path :String)
    {
        _so = SharedObject.getLocal("config_" + path, "/");
    }

    /**
     * Fetches and returns the value for the specified configuration property.
     */
    public function getValue (name :String, defValue :Object) :Object
    {
        var val :* = _so.data[name];
        return (val === undefined) ? defValue : val;
    }

    /**
     * Returns the value specified.
     */
    public function setValue (name :String, value :Object) :void
    {
        _so.data[name] = value;
        _so.flush(); // flushing is not strictly necessary
    }

    /**
     * Remove any set value for the specified preference.
     */
    public function remove (name :String) :void
    {
        delete _so.data[name];
        _so.flush(); // flushing is not strictly necessary
    }

    /**
     * Ensure that we can store preferences up to the specified size.
     * Note that calling this method may pop up a dialog to the user, asking
     * them if it's ok to increase the capacity. The result listener may
     * never be called if the user doesn't answer the pop-up.
     *
     * @param rl an optional listener that will be informed as to whether
     * the request succeeded.
     */
    public function ensureCapacity (kilobytes :int, rl :ResultListener) :void
    {
        // flush with the size, see if we're cool
        var result :String = _so.flush(1024 * kilobytes);
        if (rl == null) {
            return;
        }

        // success
        if (result == SharedObjectFlushStatus.FLUSHED) {
            rl.requestCompleted(this);
            return;
        }

        // otherwise we'll hear back in a sec
        var thisConfig :Config = this;
        var listener :Function = function (evt :NetStatusEvent) :void {
                // TODO: There is a bug where the status is always
                // "SharedObject.Flush.Failed", even on success, if the request
                // was for a large enough storage that the player calls it
                // "unlimited".
                trace("================[" + evt.info.code + "]");
                if ("SharedObject.Flush.Success" == evt.info.code) {
                    rl.requestCompleted(thisConfig);
                } else {
                    rl.requestFailed(new Error(String(evt.info.code)));
                }
                _so.removeEventListener(NetStatusEvent.NET_STATUS, listener);
            };

        _so.addEventListener(NetStatusEvent.NET_STATUS, listener);
    }

    /** The shared object that contains our preferences. */
    protected var _so :SharedObject;
}
}
