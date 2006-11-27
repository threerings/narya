package com.threerings.util {

import flash.net.URLRequest;
//import flash.net.navigateToURL; // function import

public class NetUtil
{
    /**
     * Convenience method to load a web page in the browser window without
     * having to worry about SecurityErrors in various conditions.
     *
     * @return true if the url was unable to be loaded.
     */
    public static function navigateToURL (
            url :String, preferSameWindowOrTab :Boolean = true) :Boolean
    {
        var ureq :URLRequest = new URLRequest(url);
        if (preferSameWindowOrTab) {
            try {
                flash.net.navigateToURL(ureq, "_self");
                return true;
            } catch (err :SecurityError) {
                // ignore; fall back to using a blank window, below...
            }
        }

        // open in a blank window
        try {
            flash.net.navigateToURL(ureq);
            return true;
        } catch (err :SecurityError) {
            Log.getLog(NetUtil).warning(
                "Unable to navigate to URL [e=" + err + "].");
        }

        return false; // failure!
    }
}
}
