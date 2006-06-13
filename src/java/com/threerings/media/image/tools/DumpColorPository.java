//
// $Id$
//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2004 Three Rings Design, Inc., All Rights Reserved
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
