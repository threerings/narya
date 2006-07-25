package com.threerings.util {

import flash.net.URLRequest;
//import flash.net.navigateToURL; // function import

public class NetUtil
{
    /**
     * Convenience method to load a web page in the browser window without
     * having to worry about SecurityErrors in various conditions.
     */
    public static function navigateToURL (
            url :String, newWindow :Boolean = false) :void
    {
        var ureq :URLRequest = new URLRequest(url);
        if (!newWindow) {
            try {
                flash.net.navigateToURL(ureq, "_self");
                return;
            } catch (err :SecurityError) {
                // ignore; fall back to using a blank window, below...
            }
        }

        // open in a blank window
        try {
            flash.net.navigateToURL(ureq);
        } catch (err :SecurityError) {
            Log.getLog(NetUtil).warning(
                "Unable to navigate to URL [e=" + err + "].");
        }
    }


}
}
