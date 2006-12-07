//
// $Id$

package com.threerings.crowd.chat.client {

import com.threerings.util.Name;

/**
 * An interface used with the ChatDirector to validate which usernames
 * may be added to the chatter list.
 */
public interface ChatterValidator
{
    /**
     * Arbitrates whether the username may be added to the chatters list.
     */
    function isChatterValid (username :Name) :Boolean;
}
}
