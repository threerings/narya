//
// $Id: DObjectManager.java,v 1.1 2001/06/01 05:01:52 mdb Exp $

package com.threerings.cocktail.cher.dobj;

/**
 * The distributed object manager is responsible for managing the creation
 * and destruction of distributed objects and propagating dobj events to
 * the appropriate subscribers. On the client, objects are managed as
 * proxies to the real objects managed by the server, so attribute change
 * requests are forwarded to the server and events coming down from the
 * server are delivered to the local subscribers. On the server, the
 * objects are managed directly.
 */
public interface DObjectManager
{
    public void createObject (Class dclass, Subscriber sub);

    public void subscribeToObject (int oid, Subscriber sub);

    public void fetchObject (int oid, Subscriber sub);
}
