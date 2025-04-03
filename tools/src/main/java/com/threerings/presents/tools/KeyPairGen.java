//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2025 Three Rings Design, Inc., All Rights Reserved
// https://github.com/threerings/narya/blob/master/LICENSE

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
