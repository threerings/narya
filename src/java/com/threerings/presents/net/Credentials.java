//
// $Id: Credentials.java,v 1.1 2001/05/22 06:07:59 mdb Exp $

package com.samskivert.cocktail.cher.net;

import com.samskivert.cocktail.cher.io.TypedObject;
import com.samskivert.cocktail.cher.io.TypedObjectFactory;

/**
 * Credentials are supplied by the client implementation and sent along to
 * the server during the authentication process. To provide support for a
 * variety of authentication methods, the credentials class is meant to be
 * subclassed for the particular method (ie. username + password) in use
 * in a given system.
 *
 * <p> All derived classes should provide a no argument constructor so
 * that they can be instantiated prior to reconstruction from a data input
 * stream.
 */
public abstract class Credentials extends TypedObject
{
    /**
     * All credential derived classes should base their typed object code
     * on this base value.
     */
    public static final short TYPE_BASE = 300;

    // register our credential classes
    static {
        TypedObjectFactory.registerClass(UsernamePasswordCreds.TYPE,
                                         UsernamePasswordCreds.class);
    }
}
