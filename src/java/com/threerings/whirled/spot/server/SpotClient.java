//
// $Id: SpotClient.java,v 1.1 2001/12/14 23:12:39 mdb Exp $

package com.threerings.whirled.spot.server;

import com.threerings.whirled.server.WhirledClient;

/**
 * Extends the Whirled client and handles the necessary notifications that
 * take place when a user logs off to let the clients displaying the scene
 * they were occupying know that they didn't exit via a portal, but
 * instead just disappeared.
 */
public class SpotClient extends WhirledClient
{
}
