//
// $Id: AdminCodes.java,v 1.1 2002/06/07 06:22:24 mdb Exp $

package com.threerings.admin.data;

import com.threerings.presents.data.InvocationCodes;

/**
 * Contains codes used by the admin invocation services.
 */
public interface AdminCodes extends InvocationCodes
{
    /** The module name for the admin services. */
    public static final String MODULE_NAME = "admin";

    /** The message identifier for a getConfigInfo request. */
    public static final String GET_CONFIG_INFO_REQUEST = "GetConfigInfo";

    /** The response identifier for a successful getConfigInfo request. */
    public static final String CONFIG_INFO_RESPONSE = "ConfigInfo";
}
