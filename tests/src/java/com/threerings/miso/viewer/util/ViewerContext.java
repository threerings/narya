//
// $Id: ViewerContext.java,v 1.1 2001/07/25 17:38:15 shaper Exp $

package com.threerings.miso.viewer.util;

import com.samskivert.util.Context;
import com.threerings.miso.util.MisoContext;

/**
 * A mix-in interface that combines the MisoContext and Context
 * interfaces to provide an interface with the best of both worlds.
 */
public interface ViewerContext extends MisoContext, Context
{
    // nothing for now.
}
