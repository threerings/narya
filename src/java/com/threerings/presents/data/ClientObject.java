//
// $Id: ClientObject.java,v 1.1 2001/07/19 05:56:20 mdb Exp $

package com.threerings.cocktail.cher.data;

import com.threerings.cocktail.cher.dobj.DObject;

/**
 * Every client in the system has an associated client object to which
 * only they subscribe. The client object can be used to deliver messages
 * solely to a particular client as well as to publish client-specific
 * data.
 */
public class ClientObject extends DObject
{
}
