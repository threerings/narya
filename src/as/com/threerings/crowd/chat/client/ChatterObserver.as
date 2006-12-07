//
// $Id$

package com.threerings.crowd.chat.client {

/**
 * An interface to receive information about the most recent users
 * that we've been chatting with.
 */
public interface ChatterObserver
{
    /**
     * Called when the list of chatters has been changed.
     */
    function chattersUpdated (chatterNames :Array) :void;
}
}
