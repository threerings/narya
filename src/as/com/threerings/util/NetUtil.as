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

import flash.net.URLRequest;
//import flash.net.navigateToURL; // function import

public class NetUtil
{
    /**
     * Convenience method to load a web page in the browser window without
     * having to worry about SecurityErrors in various conditions.
     *
     * @return true if the url was able to be loaded.
     */
    public static function navigateToURL (url :String, preferredWindow :String = "_self") :Boolean
    {
        var ureq :URLRequest = new URLRequest(url);
        while (true) {
            try {
                flash.net.navigateToURL(ureq, preferredWindow);
                return true;
            } catch (err :SecurityError) {
                if (preferredWindow != null) {
                    preferredWindow = null; // try again with no preferred window

                } else {
                    Log.getLog(NetUtil).warning("Unable to navigate to URL [e=" + err + "].");
                    break;
                }
            }
        }

        return false; // failure!
    }
}
}
