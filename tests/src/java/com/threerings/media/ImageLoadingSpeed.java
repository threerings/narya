//
// $Id: ImageLoadingSpeed.java,v 1.5 2004/02/25 14:51:25 mdb Exp $

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
