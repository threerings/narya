//
// $Id: CrowdClient.java,v 1.8 2002/03/05 05:33:25 mdb Exp $

package com.threerings.crowd.server;

import com.threerings.presents.server.PresentsClient;

/**
 * The crowd client extends the presents client but doesn't really do
 * anything at present. It exists mainly so that implementation systems
 * will extend it and ensure that we have the option of adding
 * functionality here in the future.
 */
public class CrowdClient extends PresentsClient
{
}
