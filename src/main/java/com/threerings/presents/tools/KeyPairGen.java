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

package com.threerings.presents.tools;

import java.security.KeyPair;

import com.threerings.presents.util.SecureUtil;

/**
 * Generates a RSA public/private key pair and outputs them in a format suitable for a properties
 * file.
 */
public class KeyPairGen
{
    public static void main (String[] args)
    {
        if (args.length != 1) {
            System.err.println("Usage: KeyPairGen bits");
            System.exit(-1);
        }

        try {
            int bits = Integer.parseInt(args[0]);
            KeyPair kp = SecureUtil.genRSAKeyPair(bits);
            System.out.println("key.public = " + SecureUtil.RSAKeyToString(kp.getPublic()));
            System.out.println("key.private = " + SecureUtil.RSAKeyToString(kp.getPrivate()));
        } catch (NumberFormatException nfe) {
            System.err.println("Usage: KeyPairGen bits");
            System.exit(-1);
        }
    }
}
