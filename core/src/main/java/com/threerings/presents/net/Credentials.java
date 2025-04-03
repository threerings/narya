//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2025 Three Rings Design, Inc., All Rights Reserved
// https://github.com/threerings/narya/blob/master/LICENSE

package com.threerings.presents.net;

import com.threerings.io.Streamable;

/**
 * Credentials are supplied by the client implementation and sent along to the server during the
 * authentication process. To provide support for a variety of authentication methods, the
 * credentials class is meant to be subclassed for the particular method (ie. password, auth
 * digest, etc.)  in use in a given system.
 *
 * <p> All derived classes should provide a no argument constructor so that they can be
 * instantiated prior to reconstruction from a data input stream.
 */
public abstract class Credentials implements Streamable
{
    /**
     * Implemented by credentials that provide a machine identifier.
     */
    public interface HasMachineIdent
    {
        /**
         * Gets the machine identifier associated with the submitting user's machine.
         */
        String getMachineIdent ();
    }

    /**
     * Implemented by credentials that provide a language.
     */
    public interface HasLanguage
    {
        /**
         * Gets the language selected by the submitting user.
         */
        String getLanguage ();
    }

    /**
     * Returns a string to use in a hash on the datagram contents to authenticate client datagrams.
     */
    public String getDatagramSecret ()
    {
        return "";
    }
}
