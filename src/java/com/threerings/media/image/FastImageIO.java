//
// $Id: FastImageIO.java,v 1.2 2003/04/28 17:38:06 mdb Exp $

package com.threerings.media.image;

import java.awt.Point;

import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.IndexColorModel;
import java.awt.image.PixelInterleavedSampleModel;
import java.awt.image.WritableRaster;

import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;

import java.nio.IntBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

/**
 * Provides routines for writing and reading uncompressed 8-bit color
 * mapped images in a manner that is extremely fast and generates a
 * minimal amount of garbage during the loading process.
 */
public class FastImageIO
{
    /** A suffix for use when storing raw images in bundles or on the file
     * system. */
    public static final String FILE_SUFFIX = ".raw";

    /**
     * Returns true if the supplied image is of a format that is supported
     * by the fast image I/O services, false if not.
     */
    public static boolean canWrite (BufferedImage image)
    {
        return (image.getColorModel() instanceof IndexColorModel) &&
            (image.getRaster().getDataBuffer() instanceof DataBufferByte);
    }

    /**
     * Writes the supplied image to the supplied output stream.
     *
     * @exception IOException thrown if an error occurs writing to the
     * output stream.
     */
    public static void write (BufferedImage image, OutputStream out)
        throws IOException
    {
        DataOutputStream dout = new DataOutputStream(out);

        // write the image dimensions
        int width = image.getWidth(), height = image.getHeight();
        dout.writeInt(width);
        dout.writeInt(height);

        // write the color model information
        IndexColorModel cmodel = (IndexColorModel)image.getColorModel();
        int tpixel = cmodel.getTransparentPixel();
        dout.writeInt(tpixel);
        int msize = cmodel.getMapSize();
        int[] map = new int[msize];
        cmodel.getRGBs(map);
        dout.writeInt(msize);
        for (int ii = 0; ii < map.length; ii++) {
            dout.writeInt(map[ii]);
        }

        // write the raster data
        DataBufferByte dbuf = (DataBufferByte)image.getRaster().getDataBuffer();
        byte[] data = dbuf.getData();
        if (data.length != width * height) {
            String errmsg = "Raster data not same size as image! [" +
                width + "x" + height + " != " + data.length + "]";
            throw new IllegalStateException(errmsg);
        }
        dout.write(data);
        dout.flush();
    }

    /**
     * Reads an image from the supplied file (which must contain an image
     * previously written via a call to {@link #write}).
     *
     * @exception IOException thrown if an error occurs reading from the
     * file.
     */
    public static BufferedImage read (File file)
        throws IOException
    {
        RandomAccessFile raf = new RandomAccessFile(file, "r");
        FileChannel fchan = raf.getChannel();

        try {
            MappedByteBuffer mbuf = fchan.map(
                FileChannel.MapMode.READ_ONLY, 0, file.length());

            // read in our integer fields
            IntBuffer ibuf = mbuf.asIntBuffer();
            int width = ibuf.get();
            int height = ibuf.get();
            int tpixel = ibuf.get();
            int msize = ibuf.get();

            if (width > Short.MAX_VALUE || width < 0 ||
                height > Short.MAX_VALUE || height < 0) {
                throw new IOException("Bogus image size " +
                                      width + "x" + height);
            }

            IndexColorModel cmodel;
            synchronized (_origin) { // any old object will do
                // make sure our colormap array is big enough
                if  (_cmap == null || _cmap.length < msize) {
                    _cmap = new int[msize];
                }
                // read in the data and create our colormap
                ibuf.get(_cmap, 0, msize);
                cmodel = new IndexColorModel(
                    8, msize, _cmap, 0, DataBuffer.TYPE_BYTE, null);
            }

            // advance the byte buffer accordingly
            mbuf.position(ibuf.position() * 4);

            // read in the image data itself
            byte[] data = new byte[width*height];
            mbuf.get(data);

            // create the image from our component parts
            DataBuffer dbuf = new DataBufferByte(data, data.length, 0);
            int[] offsets = new int[] { 0 };
            PixelInterleavedSampleModel smodel =
                new PixelInterleavedSampleModel(
                    DataBuffer.TYPE_BYTE, width, height, 1, width, offsets);
            WritableRaster raster = WritableRaster.createWritableRaster(
                smodel, dbuf, _origin);
            return new BufferedImage(cmodel, raster, false, null);

        } finally {
            fchan.close();
            raf.close();
        }
    }

    /** Used when loading our color map. */
    protected static int[] _cmap;

    /** Used when creating our writable raster. */
    protected static Point _origin = new Point(0, 0);
}
