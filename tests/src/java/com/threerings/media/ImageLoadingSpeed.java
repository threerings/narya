//
// $Id: ImageLoadingSpeed.java,v 1.2 2003/01/13 22:57:45 mdb Exp $

package com.threerings.media;

import java.awt.Image;
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

        ResourceManager rmgr = new ResourceManager("rsrc");
        rmgr.initBundles(null, "config/resource/manager.properties", null);
        ImageManager imgr = new ImageManager(rmgr, null);
        long start = System.currentTimeMillis();

//         // serialize our image
//         try {
//             FileOutputStream fout = new FileOutputStream("image.dat");
//             ObjectOutputStream oout = new ObjectOutputStream(fout);
//             oout.writeObject(image);
//             oout.close();

//         } catch (IOException ioe) {
//             ioe.printStackTrace(System.err);
//             System.exit(-1);
//         }

        int iter = 0;
        while (true) {
//         for (int iter = 0; iter < IMAGE_LOAD_ITERS; iter++) {
            String path = args[iter%args.length];
            if (path.startsWith("rsrc/")) {
                path = path.substring(5);
            }
            Image image = null;
//             image = imgr.getImage(path);
            image = imgr.getImage("components", path);

            if (++iter == 100) {
                long now = System.currentTimeMillis();
                long elapsed = now - start;
                System.err.println("Loaded " + args.length +
                                   " images a total of " + iter +
                                   " times in " + elapsed + "ms.");
                System.err.println("An average of " + (elapsed/iter) +
                                   "ms per image.");
                start = now;
                iter = 0;

                System.gc();
                try { Thread.sleep(5000); } catch (Throwable t) {}
            }
        }

//         try {
//             FileInputStream fin = new FileInputStream("image.dat");
//             BufferedInputStream bin = new BufferedInputStream(fin);
//             ObjectInputStream oin = new ObjectInputStream(bin);
//             image = (Image)oin.readObject();
//             System.out.println(image);

//         } catch (ClassNotFoundException cnfe) {
//             cnfe.printStackTrace(System.err);
//             System.exit(-1);

//         } catch (IOException ioe) {
//             ioe.printStackTrace(System.err);
//             System.exit(-1);
//         }
        
//         long elapsed = System.currentTimeMillis() - start;

//         System.err.println("Loaded " + args.length + " images a total of " +
//                            IMAGE_LOAD_ITERS + " times in " + elapsed + "ms.");
//         System.err.println("An average of " + (elapsed/IMAGE_LOAD_ITERS) +
//                            "ms per image.");
    }

    protected static final int IMAGE_LOAD_ITERS = 100;
}
