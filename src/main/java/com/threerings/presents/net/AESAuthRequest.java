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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import java.security.GeneralSecurityException;

import javax.crypto.Cipher;

import com.threerings.io.ObjectInputStream;
import com.threerings.io.ObjectOutputStream;

import com.threerings.presents.util.SecureUtil;

/**
 * Sends an AES encrypted auth request to the server.  It assumes that
 * {@link SecureUtil#ciphersSupported} has succeeded.
 */
public class AESAuthRequest extends AuthRequest
{
    /**
     * Creates an auth request, secured if able, unsecured if not.
     */
    public static AuthRequest createAuthRequest (
            Credentials creds, String version, String[] bootGroups, boolean requireSecureAuth)
    {
        return createAuthRequest(creds, version, bootGroups, requireSecureAuth, null, null);
    }

    /**
     * Creates an auth request, secured if able, unsecured if not.
     */
    public static AuthRequest createAuthRequest (
            Credentials creds, String version, String[] bootGroups, boolean requireSecureAuth,
            PublicKeyCredentials pkcreds, SecureResponse resp)
    {
        byte[] secret = resp == null ? null : resp.getCodeBytes(pkcreds);
        if (pkcreds == null || secret == null) {
            return new AuthRequest(requireSecureAuth ? null : creds, version, bootGroups);
        }
        return new AESAuthRequest(secret, creds, version, bootGroups);
    }

    /**
     * Zero argument constructor used when unserializing an instance.
     */
    public AESAuthRequest ()
    {
        super();
    }

    /**
     * Constructs a auth request with the supplied credentials and client version information.
     */
    public AESAuthRequest (byte[] key, Credentials creds, String version, String[] bootGroups)
    {
        super(null, version, bootGroups);
        _clearCreds = creds;
        _key = key;
    }

    @Override // documentation inherited
    public Credentials getCredentials ()
    {
        return _clearCreds;
    }

    @Override // documentation inherited
    public byte[] getSecret ()
    {
        return _key;
    }

    @Override // documentation inherited
    public String toString ()
    {
        return "[type=AESAREQ, msgid=" + messageId + ", creds=" + _clearCreds +
            ", version=" + _version + "]";
    }

    /**
     * Decrypts the request after transmission.
     */
    public void decrypt (byte[] key)
        throws IOException, ClassNotFoundException
    {
        if (_clearCreds != null) {
            return;
        }
        _key = key;
        try {
           _contents = SecureUtil.getAESCipher(Cipher.DECRYPT_MODE, _key).doFinal(_contents);
        } catch (GeneralSecurityException gse) {
            IOException ioe = new IOException("Failed to decrypt credentials");
            ioe.initCause(gse);
            throw ioe;
        }

        ByteArrayInputStream byteIn = new ByteArrayInputStream(_contents);
        ObjectInputStream cipherIn = new ObjectInputStream(byteIn);
        _clearCreds = (Credentials)cipherIn.readObject();
    }

    /**
     * A customized AES encrypting write object.
     */
    public void writeObject (ObjectOutputStream out)
        throws IOException
    {
        out.defaultWriteObject();
        ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
        ObjectOutputStream oOut = new ObjectOutputStream(byteOut);
        oOut.writeObject(_clearCreds);
        try {
            byte[] encrypted =
                SecureUtil.getAESCipher(Cipher.ENCRYPT_MODE, _key).doFinal(byteOut.toByteArray());
            out.writeInt(encrypted.length);
            out.write(encrypted);
        } catch (GeneralSecurityException gse) {
            IOException ioe = new IOException("Failed to encrypt credentials");
            ioe.initCause(gse);
            throw ioe;
        }
    }

    /**
     * Read in our encrypted contents.
     */
    @Override
    public void readObject (ObjectInputStream in)
        throws IOException, ClassNotFoundException
    {
        in.defaultReadObject();
        _contents = new byte[in.readInt()];
        in.read(_contents);
    }

    /** Our encryption key. */
    protected transient byte[] _key;

    /** Our encrypted contents. */
    protected transient byte[] _contents;

    /** Our unencrypted credentials. */
    protected transient Credentials _clearCreds;
}
