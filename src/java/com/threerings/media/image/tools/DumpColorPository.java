//
// $Id: DumpColorPository.java,v 1.1 2003/01/31 23:10:45 mdb Exp $

package com.threerings.media.image.tools;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Iterator;

import com.threerings.media.image.ColorPository;

/**
 * Simple tool for dumping a serialized color pository.
 */
public class DumpColorPository
{
    public static void main (String[] args)
    {
        if (args.length == 0) {
            System.err.println("Usage: DumpColorPository colorpos.dat");
            System.exit(-1);
        }

        try {
            ColorPository pos = ColorPository.loadColorPository(
                new FileInputStream(args[0]));
            Iterator iter = pos.enumerateClasses();
            while (iter.hasNext()) {
                System.out.println(iter.next());
            }

        } catch (IOException ioe) {
            ioe.printStackTrace(System.err);
        }
    }
}
