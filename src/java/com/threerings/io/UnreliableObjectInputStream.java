//
// $Id$

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

    @Override // documentation inherited
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
}
