//
// $Id: ClientObject.java,v 1.2 2001/10/11 04:07:52 mdb Exp $

package com.threerings.presents.data;

import com.threerings.presents.dobj.DObject;

/**
 * Every client in the system has an associated client object to which
 * only they subscribe. The client object can be used to deliver messages
 * solely to a particular client as well as to publish client-specific
 * data.
 */
public class ClientObject extends DObject
{
}
