//
// $Id: MiCasaContext.java,v 1.2 2001/10/09 17:47:33 mdb Exp $

package com.threerings.micasa.util;

import com.threerings.parlor.util.ParlorContext;

import com.threerings.micasa.client.MiCasaFrame;

/**
 * The micasa context encapsulates the contexts of all of the services
 * that are used by the micasa client so that we can pass around one
 * single context implementation that provides all of the necessary
 * components to all of the services in use.
 */
public interface MiCasaContext
    extends ParlorContext
{
    /** Returns a reference to the primary user interface frame. This can
     * be used to set the top-level panel when we enter a game, etc. */
    public MiCasaFrame getFrame ();
}
