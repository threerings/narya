//
// $Id: MiCasaContext.java,v 1.1 2001/10/03 23:24:09 mdb Exp $

package com.threerings.micasa.util;

import com.threerings.parlor.util.ParlorContext;

/**
 * The micasa context encapsulates the contexts of all of the services
 * that are used by the micasa client so that we can pass around one
 * single context implementation that provides all of the necessary
 * components to all of the services in use.
 */
public interface MiCasaContext
    extends ParlorContext
{
}
