//
// $Id: ImageLoadingSpeed.java,v 1.3 2003/04/25 15:52:39 mdb Exp $

package com.threerings.media;

import java.awt.Image;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import javax.imageio.stream.FileImageInputStream;

import java.io.*;

import com.threerings.resource.ResourceManager;
import com.threerings.media.image.ImageManager;

/**
 * Tests our image loading speed.
 */
public class ImageLoadingSpeed
{
    public static void main (String[] args)
    {
        if (args.length < 1) {
            System.err.println("Usage: ImageLoadingTest image [image ...]");
            System.exit(-1);
        }

        long start = System.currentTimeMillis();
        int iter = 0;
        while (true) {
            String path = args[iter%args.length];

            try {
                FileImageInputStream fis =
                    new FileImageInputStream(new File(path));
                BufferedImage image =  ImageIO.read(fis);
                int width = image.getWidth();
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
