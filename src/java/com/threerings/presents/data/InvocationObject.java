//
// $Id: InvocationObject.java,v 1.1 2001/07/19 05:56:20 mdb Exp $

package com.threerings.cocktail.cher.data;

import com.threerings.cocktail.cher.dobj.DObject;

/**
 * A single invocation object is created by the server invocation manager
 * and is used to receive invocation request messages from the client. The
 * server presently delivers invocation messages to the client via the
 * client object.
 */
public class InvocationObject extends DObject
{
    /**
     * This constant is used to identify messages on both ends of the
     * invocation services.
     */
    public static final String MESSAGE_NAME = "invoke";
}
