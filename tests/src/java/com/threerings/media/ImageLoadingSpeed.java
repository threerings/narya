//
// $Id: ImageLoadingSpeed.java,v 1.1 2002/12/23 20:32:28 mdb Exp $

package com.threerings.media;

import java.awt.Image;
import java.io.*;
import com.threerings.resource.ResourceManager;

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

        for (int iter = 0; iter < IMAGE_LOAD_ITERS; iter++) {
            String path = args[iter%args.length];
            if (path.startsWith("rsrc/")) {
                path = path.substring(5);
            }
            Image image = null;
            try {
                image = imgr.loadImage(path);
            } catch (IOException ioe) {
                ioe.printStackTrace(System.err);
                System.exit(-1);
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
        
        long elapsed = System.currentTimeMillis() - start;

        System.err.println("Loaded " + args.length + " images a total of " +
                           IMAGE_LOAD_ITERS + " times in " + elapsed + "ms.");
        System.err.println("An average of " + (elapsed/IMAGE_LOAD_ITERS) +
                           "ms per image.");
    }

    protected static final int IMAGE_LOAD_ITERS = 100;
}
