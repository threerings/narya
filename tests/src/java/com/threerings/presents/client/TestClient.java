//
// $Id: TestClient.java,v 1.2 2001/05/30 23:58:31 mdb Exp $

package com.threerings.cocktail.cher.client.test;

import com.threerings.cocktail.cher.net.*;
import com.threerings.cocktail.cher.client.*;

/**
 * A standalone test client.
 */
public class TestClient
{
    public static void main (String[] args)
    {
        UsernamePasswordCreds creds =
            new UsernamePasswordCreds("test", "test");
        Client client = new Client(creds);
        client.setServer("localhost", 4007);
        client.logon();
    }
}
