//
// $Id$
//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2009 Three Rings Design, Inc., All Rights Reserved
// http://www.threerings.net/code/narya/
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

package com.threerings.util {

/**
 * Utility methods relating to the electronic mails.
 */
public class MailUtil
{
    /**
     * Returns true if the supplied email address appears valid (according to a widely used regular
     * expression). False if it does not.
     */
    public static function isValidAddress (email :String) :Boolean
    {
        var matches :Array = email.match(EMAIL_REGEX);
        // in theory there should only be one match and we should check for 'matches.length == 1',
        // but Flash's regular expression code likes to return nonsensical things, so we just check
        // that the first match is equal to the entire supplied address
        return (matches != null && matches.length > 0) && ((matches[0] as String) == email);
    }

    /** Originally formulated by lambert@nas.nasa.gov. */
    protected static const EMAIL_REGEX :RegExp = new RegExp(
        "^([-a-z0-9_.!%+]+@[-a-z0-9]+(\\.[-a-z0-9]+)*\\.[-a-z0-9]+)$", "i");
}
}
