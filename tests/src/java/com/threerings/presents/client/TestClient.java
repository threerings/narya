//
// $Id: TestClient.java,v 1.1 2001/05/29 03:27:59 mdb Exp $

package com.samskivert.cocktail.cher.client.test;

import com.samskivert.cocktail.cher.net.*;
import com.samskivert.cocktail.cher.client.*;

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
