//
// $Id$
//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2012 Three Rings Design, Inc., All Rights Reserved
// http://code.google.com/p/narya/
//
// This library is free software; you can redistribute it and/or modify it
// under the terms of the GNU Lesser General Public License as published
// by the Free Software Foundation; either version 2.1 of the License, or
// (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA

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
