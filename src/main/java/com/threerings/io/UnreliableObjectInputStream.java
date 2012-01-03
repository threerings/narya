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

package com.threerings.io;

import java.io.IOException;
import java.io.InputStream;

/**
 * The counterpart of {@link UnreliableObjectOutputStream}.
 */
public class UnreliableObjectInputStream extends ObjectInputStream
{
    /**
     * Constructs an object input stream which will read its data from the supplied source stream.
     */
    public UnreliableObjectInputStream (InputStream source)
    {
        super(source);
    }

    @Override
    protected ClassMapping mapClass (short code, String cname)
        throws IOException, ClassNotFoundException
    {
        // see if we already have a mapping
        ClassMapping cmap = (code < _classmap.size()) ? _classmap.get(code) : null;
        if (cmap != null) {
            // sanity check
            if (!cmap.sclass.getName().equals(cname)) {
                throw new RuntimeException(
                    "Received mapping for class that conflicts with existing mapping " +
                    "[code=" + code + ", oclass=" + cmap.sclass.getName() + ", nclass=" +
                    cname + "]");
            }
            return cmap;
        }
        // insert null entries for missing mappings
        cmap = createClassMapping(code, cname);
        for (int ii = 0, nn = (code + 1) - _classmap.size(); ii < nn; ii++) {
            _classmap.add(null);
        }
        _classmap.set(code, cmap);
        return cmap;
    }

    @Override
    protected void mapIntern (short code, String value)
    {
        // see if we already have a mapping
        String ovalue = (code < _internmap.size()) ? _internmap.get(code) : null;
        if (ovalue != null) {
            // sanity check
            if (!ovalue.equals(value)) {
                throw new RuntimeException(
                    "Received mapping for intern that conflicts with existing mapping " +
                    "[code=" + code + ", ovalue=" + ovalue + ", nvalue=" + value + "]");
            }
            return;
        }
        // insert null entries for missing mappings
        for (int ii = 0, nn = (code + 1) - _internmap.size(); ii < nn; ii++) {
            _internmap.add(null);
        }
        _internmap.set(code, value);
    }
}
