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

package com.threerings.presents.util;

import java.math.BigInteger;

import java.security.GeneralSecurityException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.RSAPrivateKeySpec;
import java.security.spec.RSAPublicKeySpec;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import static com.threerings.presents.Log.log;

/**
 * Security utilities for performing secure authentication.
 */
public class SecureUtil
{
    /** The version of our security protocol (for backwards compatability with older clients). */
    public static final int VERSION = 1;

    /**
     * Creates our AES cipher.
     */
    public static Cipher getAESCipher (int mode, byte[] key)
    {
        try {
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            SecretKeySpec aesKey = new SecretKeySpec(key, "AES");
            cipher.init(mode, aesKey, IVPS);
            return cipher;
        } catch (GeneralSecurityException gse) {
            log.warning("Failed to create cipher", gse);
        }
        return null;
    }

    /**
     * Creates our RSA cipher.
     */
    public static Cipher getRSACipher (PrivateKey key)
    {
        return getRSACipher(Cipher.DECRYPT_MODE, key);
    }

    /**
     * Creates our RSA cipher.
     */
    public static Cipher getRSACipher (PublicKey key)
    {
        return getRSACipher(Cipher.ENCRYPT_MODE, key);
    }

    /**
     * Creates our RSA cipher.
     */
    public static Cipher getRSACipher (int mode, Key key)
    {
        try {
            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(mode, key);
            return cipher;
        } catch (GeneralSecurityException gse) {
            log.warning("Failed to create cipher", gse);
        }
        return null;
    }

    /**
     * Creates an RSA key pair.
     */
    public static KeyPair genRSAKeyPair (int bits)
    {
        try {
            KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
            kpg.initialize(bits);
            return kpg.genKeyPair();
        } catch (GeneralSecurityException gse) {
            log.warning("Failed to create key pair", gse);
        }
        return null;
    }

    /**
     * Converts an key to a string suitable for a properties file.
     */
    public static String RSAKeyToString (PublicKey key)
    {
        try {
            KeyFactory kf = KeyFactory.getInstance("RSA");
            RSAPublicKeySpec spec = kf.getKeySpec(key, RSAPublicKeySpec.class);
            StringBuilder buf = new StringBuilder();
            buf.append(spec.getModulus().toString(16))
                .append(SPLIT)
                .append(spec.getPublicExponent().toString(16));
            return buf.toString();
        } catch (GeneralSecurityException gse) {
            log.warning("Failed to convert key to string", gse);
        }
        return null;
    }

    /**
     * Converts an key to a string suitable for a properties file.
     */
    public static String RSAKeyToString (PrivateKey key)
    {
        try {
            KeyFactory kf = KeyFactory.getInstance("RSA");
            RSAPrivateKeySpec spec = kf.getKeySpec(key, RSAPrivateKeySpec.class);
            StringBuilder buf = new StringBuilder();
            buf.append(spec.getModulus().toString(16))
                .append(SPLIT)
                .append(spec.getPrivateExponent().toString(16));
            return buf.toString();
        } catch (GeneralSecurityException gse) {
            log.warning("Failed to convert key to string", gse);
        }
        return null;
    }

    /**
     * Creates a public key from the supplied string.
     */
    public static PublicKey stringToRSAPublicKey (String str)
    {
        try {
            BigInteger mod = new BigInteger(str.substring(0, str.indexOf(SPLIT)), 16);
            BigInteger exp = new BigInteger(str.substring(str.indexOf(SPLIT) + 1), 16);
            RSAPublicKeySpec keySpec = new RSAPublicKeySpec(mod, exp);
            KeyFactory kf = KeyFactory.getInstance("RSA");
            return kf.generatePublic(keySpec);
        } catch (NumberFormatException nfe) {
            log.warning("Failed to read key from string.", "str", str, nfe);
        } catch (GeneralSecurityException gse) {
            log.warning("Failed to read key from string.", "str", str, gse);
        }
        return null;
    }

    /**
     * Creates a private key from the supplied string.
     */
    public static PrivateKey stringToRSAPrivateKey (String str)
    {
        try {
            BigInteger mod = new BigInteger(str.substring(0, str.indexOf(SPLIT)), 16);
            BigInteger exp = new BigInteger(str.substring(str.indexOf(SPLIT) + 1), 16);
            RSAPrivateKeySpec keySpec = new RSAPrivateKeySpec(mod, exp);
            KeyFactory kf = KeyFactory.getInstance("RSA");
            return kf.generatePrivate(keySpec);
        } catch (NumberFormatException nfe) {
            log.warning("Failed to read key from string.", "str", str, nfe);
        } catch (GeneralSecurityException gse) {
            log.warning("Failed to read key from string.", "str", str, gse);
        }
        return null;
    }

    /**
     * Returns true if we can generate our ciphers.
     */
    public static boolean ciphersSupported (PrivateKey key)
    {
        return getRSACipher(key) != null && getAESCipher(Cipher.ENCRYPT_MODE, new byte[16]) != null;
    }

    /**
     * Returns true if we can generate our ciphers.
     */
    public static boolean ciphersSupported (PublicKey key)
    {
        return getRSACipher(key) != null && getAESCipher(Cipher.ENCRYPT_MODE, new byte[16]) != null;
    }

    /**
     * Creates a random key.
     */
    public static byte[] createRandomKey (int length)
    {
        byte[] secret = new byte[length];
        _rand.nextBytes(secret);
        return secret;
    }

    /**
     * Encrypts a secret key and salt with a public key.
     */
    public static byte[] encryptBytes (PublicKey key, byte[] secret, byte[] salt)
    {
        byte[] encrypt = new byte[secret.length + salt.length];
        for (int ii = 0; ii < secret.length; ii++) {
            encrypt[ii] = secret[ii];
        }
        for (int ii = 0; ii < salt.length; ii++) {
            encrypt[secret.length + ii] = salt[ii];
        }
        try {
            return getRSACipher(key).doFinal(encrypt);
        } catch (GeneralSecurityException gse) {
            log.warning("Failed to encrypt bytes", gse);
        }
        return encrypt;
    }

    /**
     * Decrypts a secret key and checks for tailing salt.
     *
     * @return the secret key, or null on failure or non-matching salt.
     */
    public static byte[] decryptBytes (PrivateKey key, byte[] encrypted, byte[] salt)
    {
        try {
            byte[] decrypted = getRSACipher(key).doFinal(encrypted);
            for (int ii = 0; ii < salt.length; ii++) {
                if (decrypted[decrypted.length - salt.length + ii] != salt[ii]) {
                    return null;
                }
            }
            byte[] secret = new byte[decrypted.length - salt.length];
            for (int ii = 0; ii < secret.length; ii++) {
                secret[ii] = decrypted[ii];
            }
            return secret;
        } catch (GeneralSecurityException gse) {
            log.warning("Failed to decrypt bytes", gse);
        }
        return null;
    }

    /**
     * XORs a byte array against a key.
     */
    public static byte[] xorBytes (byte[] data, byte[] key)
    {
        byte[] xored = new byte[data.length];
        for (int ii = 0; ii < data.length; ii++) {
            xored[ii] = (byte)(data[ii] ^ key[ii % key.length]);
        }
        return xored;
    }

    protected static final SecureRandom _rand = new SecureRandom();

    /** Our split character. */
    protected static final char SPLIT = '#';

    /** Our initialization vector. */
    protected static final byte[] IV = new byte[] {
        0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08,
        0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08
    };
    protected static final IvParameterSpec IVPS = new IvParameterSpec(IV);
}
