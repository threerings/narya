//
// $Id: ImageLoadingSpeed.java,v 1.6 2004/08/27 02:20:57 mdb Exp $
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

package com.threerings.media;

import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;

import java.io.*;

import com.threerings.media.image.FastImageIO;

/**
 * Tests our image loading speed.
 */
public class ImageLoadingSpeed
{
    public static void main (String[] args)
    {
        if (args.length < 1) {
            System.err.println("Usage: ImageLoadingTest image");
            System.exit(-1);
        }

        File file = new File(args[0]);
        File ffile = new File(args[0] + FastImageIO.FILE_SUFFIX);
        try {
            BufferedImage image = ImageIO.read(file);
            FastImageIO.write(image, new FileOutputStream(ffile));
        } catch (IOException ioe) {
            ioe.printStackTrace(System.err);
            System.exit(-1);
        }

        long start = System.currentTimeMillis();
        int iter = 0;
        while (true) {
            try {
                FastImageIO.read(ffile).getWidth();
            } catch (IOException ioe) {
                ioe.printStackTrace(System.err);
                System.exit(-1);
            }

            if (++iter == 100) {
                long now = System.currentTimeMillis();
                long elapsed = now - start;
                System.err.println("Loaded " + args.length +
                                   " images a total of " + iter +
                                   " times in " + elapsed + "ms.");
                System.err.println("An average of " + (elapsed/iter) +
                                   "ms per image.");

                System.gc();
                try { Thread.sleep(1000); } catch (Throwable t) {}

                start = now;
                iter = 0;
            }
        }
    }
}
