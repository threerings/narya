//
// $Id: TestPresentsServer.java,v 1.1 2001/11/08 05:40:07 mdb Exp $

package com.threerings.presents.server;

/**
 * This test version of the server avoids creating a connection manager
 * because that requires a shared library which we don't have available
 * when testing via ant/JUnit.
 */
public class TestPresentsServer extends PresentsServer
{
    protected boolean createConnectionManager ()
    {
        return false;
    }
}
