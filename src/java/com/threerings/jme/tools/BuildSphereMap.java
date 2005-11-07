//
// $Id$
//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2005 Three Rings Design, Inc., All Rights Reserved
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

package com.threerings.jme.tools;

import java.awt.image.BufferedImage;

import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import com.jme.image.Texture;
import com.jme.math.FastMath;
import com.jme.math.Vector3f;

/**
 * A tool for converting cube maps (sky boxes) to sphere maps.
 */
public class BuildSphereMap
{
    public static void main (String[] args)
    {
        if (args.length < 7) {
            System.err.println("Usage: BuildSphereMap front.ext back.ext " +
                "left.ext right.ext up.ext dest.ext size");
            System.exit(-1);
        }

        try {
            execute(new File(args[0]), new File(args[1]), new File(args[2]),
                new File(args[3]), new File(args[4]), new File(args[5]),
                Integer.parseInt(args[6]));
        
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Builds the sphere map.
     *
     * @param front the file containing the front side of the cube map
     * @param back the file containing the back side of the cube map
     * @param left the file containing the left side of the cube map
     * @param right the file containing the right side of the cube map
     * @param up the file containing the up side of the cube map
     * @param target the file to contain the sphere map
     * @param size the size of the sphere map to generate
     */
    public static void execute (File front, File back, File left, File right,
        File up, File target, int size)
        throws IOException
    {
        // load up the sides of the cube map
        BufferedImage[] sides = new BufferedImage[5];
        sides[FRONT] = ImageIO.read(front);
        sides[BACK] = ImageIO.read(back);
        sides[LEFT] = ImageIO.read(left);
        sides[RIGHT] = ImageIO.read(right);
        sides[UP] = ImageIO.read(up);
        
        // compute the pixels
        int[] rgb = new int[size * size];
        Vector3f vec = new Vector3f();
        for (int y = 0, idx = 0; y < size; y++) {
            for (int x = 0; x < size; x++, idx++) {
                float vx = x / (size*0.5f) - 1f, vy = y / (size*0.5f) - 1f,
                    d2 = vx*vx + vy*vy;
                int p = 0;
                if (d2 <= 1f) {
                    vec.set(vx, vy, FastMath.sqrt(1f - d2));
                    rgb[idx] = getCubeMapPixel(vec, sides);
                }
            }
        }
        
        // create and write the image
        BufferedImage image = new BufferedImage(size, size,
            BufferedImage.TYPE_INT_RGB);
        image.setRGB(0, 0, size, size, rgb, 0, size);
        String dest = target.toString(),
            ext = dest.substring(dest.lastIndexOf('.')+1);
        ImageIO.write(image, ext, target);
    }
    
    /**
     * Returns the pixel from the cube map to which the given vector points.
     */
    protected static int getCubeMapPixel (Vector3f vec, BufferedImage[] sides)
    {
        int side = getCubeMapSide(vec);
        
        float s, t;
        switch (side) {
            case FRONT:
                s = -vec.x / vec.y;
                t = vec.z / vec.y;
                break;
            case BACK:
                s = -vec.x / vec.y;
                t = -vec.z / vec.y;
                break;
            case LEFT:
                s = vec.y / vec.x;
                t = -vec.z / vec.x;
                break;
            case RIGHT:
                s = vec.y / vec.x;
                t = vec.z / vec.x;
                break;
            default:
            case UP:
                s = vec.x / vec.z;
                t = -vec.y / vec.z;
                break;
        }
        int width = sides[side].getWidth(), height = sides[side].getHeight();
        return sides[side].getRGB((int)((width-1) * (s+1f)/2f),
            (int)((height-1) * (1f-t)/2f));
    }
    
    /**
     * Returns the side index identifying the face of the cube map to which
     * the given vector points.
     */
    protected static int getCubeMapSide (Vector3f vec)
    {
        if (vec.x > vec.z && vec.x > vec.y && vec.x > -vec.y) {
            return RIGHT;
            
        } else if (vec.x < -vec.z && vec.x < -vec.y && vec.x < vec.y) {
            return LEFT;
        
        } else if (vec.y > vec.z) {
            return FRONT;
            
        } else if (vec.y < -vec.z) {
            return BACK;
            
        } else {
            return UP;
        }
    }
    
    /** The sides of the cube map. */
    protected static final int FRONT = 0, BACK = 1, LEFT = 2, RIGHT = 3,
        UP = 4;
}
