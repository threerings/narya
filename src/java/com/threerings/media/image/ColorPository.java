//
// $Id: ColorPository.java,v 1.6 2004/08/27 02:12:38 mdb Exp $
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

package com.threerings.media.image;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Iterator;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;

import com.samskivert.util.HashIntMap;
import com.samskivert.util.StringUtil;

import com.threerings.media.Log;
import com.threerings.resource.ResourceManager;
import com.threerings.util.CompiledConfig;
import com.threerings.util.RandomUtil;

/**
 * A repository of image recoloration information. It was called the
 * recolor repository but the re-s cancelled one another out.
 */
public class ColorPository implements Serializable
{
    /**
     * Used to store information on a class of colors. These are public to
     * simplify the XML parsing process, so pay them no mind.
     */
    public static class ClassRecord implements Serializable
    {
        /** An integer identifier for this class. */
        public int classId;

        /** The name of the color class. */
        public String name;

        /** The source color to use when recoloring colors in this class. */
        public Color source;

        /** Data identifying the range of colors around the source color
         * that will be recolored when recoloring using this class. */
        public float[] range;

        /** The default starting legality value for this color class. See
         * {@link ColorRecord#starter}. */
        public boolean starter;

        /** A table of target colors included in this class. */
        public HashIntMap colors = new HashIntMap();

        /** Used when parsing the color definitions. */
        public void addColor (ColorRecord record)
        {
            // validate the color id
            if (record.colorId > 255) {
                Log.warning("Refusing to add color record; colorId > 255 " +
                            "[class=" + this + ", record=" + record + "].");
            } else {
                record.cclass = this;
                colors.put(record.colorId, record);
            }
        }

        /** Returns a random starting id from the entries in this
         * class. */
        public ColorRecord randomStartingColor ()
        {
            // figure out our starter ids if we haven't already
            if (_starters == null) {
                ArrayList list = new ArrayList();
                Iterator iter = colors.values().iterator();
                while (iter.hasNext()) {
                    ColorRecord color = (ColorRecord)iter.next();
                    if (color.starter) {
                        list.add(color);
                    }
                }
                _starters = (ColorRecord[])
                    list.toArray(new ColorRecord[list.size()]);
            }

            // sanity check
            if (_starters.length < 1) {
                Log.warning("Requested random starting color from " +
                            "colorless component class " + this + "].");
                return null;
            }

            // return a random entry from the array
            return _starters[RandomUtil.getInt(_starters.length)];
        }

        /**
         * Returns a string representation of this instance.
         */
        public String toString ()
        {
            return "[id=" + classId + ", name=" + name + ", source=#" +
                Integer.toString(source.getRGB() & 0xFFFFFF, 16) +
                ", range=" + StringUtil.toString(range) +
                ", starter=" + starter + ", colors=" +
                StringUtil.toString(colors.values().iterator()) + "]";
        }

        protected transient ColorRecord[] _starters;

        /** Increase this value when object's serialized state is impacted
         * by a class change (modification of fields, inheritance). */
        private static final long serialVersionUID = 2;
    }

    /**
     * Used to store information on a particular color. These are public
     * to simplify the XML parsing process, so pay them no mind.
     */
    public static class ColorRecord implements Serializable
    {
        /** The colorization class to which we belong. */
        public ClassRecord cclass;

        /** A unique colorization identifier (used in fingerprints). */
        public int colorId;

        /** The name of the target color. */
        public String name;

        /** Data indicating the offset (in HSV color space) from the
         * source color to recolor to this color. */
        public float[] offsets;

        /** Tags this color as a legal starting color or not. This is a
         * shameful copout, placing application-specific functionality
         * into a general purpose library class. */
        public boolean starter;

        /**
         * Returns a value that is the composite of our class id and color
         * id which can be used to identify a colorization record. This
         * value will always be a positive integer that fits into 16 bits.
         */
        public int getColorPrint ()
        {
            return ((cclass.classId << 8) | colorId);
        }

        /**
         * Returns the data in this record configured as a colorization
         * instance.
         */
        public Colorization getColorization ()
        {
//             if (_zation == null) {
//                 _zation = new Colorization(getColorPrint(), cclass.source,
//                                            cclass.range, offsets);
//             }
//             return _zation;
            return new Colorization(getColorPrint(), cclass.source,
                                    cclass.range, offsets);
        }

        /**
         * Returns a string representation of this instance.
         */
        public String toString ()
        {
            return "[id=" + colorId + ", name=" + name +
                ", offsets=" + StringUtil.toString(offsets) +
                ", starter=" + starter + "]";
        }

        /** Our data represented as a colorization. */
        protected transient Colorization _zation;

        /** Increase this value when object's serialized state is impacted
         * by a class change (modification of fields, inheritance). */
        private static final long serialVersionUID = 2;
    }

    /**
     * Returns an iterator over all color classes in this pository.
     */
    public Iterator enumerateClasses ()
    {
        return _classes.values().iterator();
    }

    /**
     * Returns an array containing the records for the colors in the
     * specified class.
     */
    public ColorRecord[] enumerateColors (String className)
    {
        // make sure the class exists
        ClassRecord record = getClassRecord(className);
        if (record == null) {
            return null;
        }

        // create the array
        ColorRecord[] crecs = new ColorRecord[record.colors.size()];
        Iterator iter = record.colors.values().iterator();
        for (int i = 0; iter.hasNext(); i++) {
            crecs[i] = ((ColorRecord)iter.next());
        }
        return crecs;
    }

    /**
     * Returns an array containing the ids of the colors in the specified
     * class.
     */
    public int[] enumerateColorIds (String className)
    {
        // make sure the class exists
        ClassRecord record = getClassRecord(className);
        if (record == null) {
            return null;
        }

        int[] cids = new int[record.colors.size()];
        Iterator crecs = record.colors.values().iterator();
        for (int i = 0; crecs.hasNext(); i++) {
            cids[i] = ((ColorRecord)crecs.next()).colorId;
        }
        return cids;
    }

    /**
     * Returns true if the specified color is legal for use at character
     * creation time. false is always returned for non-existent colors or
     * classes.
     */
    public boolean isLegalStartColor (int classId, int colorId)
    {
        ColorRecord color = getColorRecord(classId, colorId);
        return (color == null) ? false : color.starter;
    }

    /**
     * Returns a random starting color from the specified color class.
     */
    public ColorRecord getRandomStartingColor (String className)
    {
        // make sure the class exists
        ClassRecord record = getClassRecord(className);
        return (record == null) ? null : record.randomStartingColor();
    }

    /**
     * Looks up a colorization by id.
     */
    public Colorization getColorization (int classId, int colorId)
    {
        ColorRecord color = getColorRecord(classId, colorId);
        return (color == null) ? null : color.getColorization();
    }

    /**
     * Looks up a colorization by color print.
     */
    public Colorization getColorization (int colorPrint)
    {
        return getColorization(colorPrint >> 8, colorPrint & 0xFF);
    }

    /**
     * Looks up a colorization by name.
     */
    public Colorization getColorization (String className, int colorId)
    {
        ClassRecord crec = getClassRecord(className);
        if (crec != null) {
            ColorRecord color = (ColorRecord)crec.colors.get(colorId);
            if (color != null) {
                return color.getColorization();
            }
        }
        return null;
    }

    /**
     * Loads up a colorization class by name and logs a warning if it
     * doesn't exist.
     */
    public ClassRecord getClassRecord (String className)
    {
        Iterator iter = _classes.values().iterator();
        while (iter.hasNext()) {
            ClassRecord crec = (ClassRecord)iter.next();
            if (crec.name.equals(className)) {
                return crec;
            }
        }
        Log.warning("No such color class [class=" + className + "].");
        Thread.dumpStack();
        return null;
    }

    /**
     * Looks up the requested color record.
     */
    protected ColorRecord getColorRecord (int classId, int colorId)
    {
        ClassRecord record = (ClassRecord)_classes.get(classId);
        if (record == null) {
            // if they request color class zero, we assume they're just
            // decoding a blank colorprint, otherwise we complain
            if (classId != 0) {
                Log.warning("Requested unknown color class " +
                            "[classId=" + classId +
                            ", colorId=" + colorId + "].");
                Thread.dumpStack();
            }
            return null;
        }
        return (ColorRecord)record.colors.get(colorId);
    }

    /**
     * Adds a fully configured color class record to the pository. This is
     * only called by the XML parsing code, so pay it no mind.
     */
    public void addClass (ClassRecord record)
    {
        // validate the class id
        if (record.classId > 255) {
            Log.warning("Refusing to add class; classId > 255 " + record + ".");
        } else {
            _classes.put(record.classId, record);
        }
    }

    /**
     * Loads up a serialized color pository from the supplied resource
     * manager.
     */
    public static ColorPository loadColorPository (ResourceManager rmgr)
    {
        try {
            return loadColorPository(rmgr.getResource(CONFIG_PATH));
        } catch (IOException ioe) {
            Log.warning("Failure loading color pository [path=" + CONFIG_PATH +
                        ", error=" + ioe + "].");
            return new ColorPository();
        }
    }

    /**
     * Loads up a serialized color pository from the supplied resource
     * manager.
     */
    public static ColorPository loadColorPository (InputStream source)
    {
        try {
            return (ColorPository)CompiledConfig.loadConfig(source);
        } catch (IOException ioe) {
            Log.warning("Failure loading color pository: " + ioe + ".");
            return new ColorPository();
        }
    }

    /**
     * Serializes and saves color pository to the supplied file.
     */
    public static void saveColorPository (ColorPository posit, File root)
    {
        File path = new File(root, CONFIG_PATH);
        try {
            CompiledConfig.saveConfig(path, posit);
        } catch (IOException ioe) {
            Log.warning("Failure saving color pository " +
                        "[path=" + path + ", error=" + ioe + "].");
        }
    }

    /** Our mapping from class names to class records. */
    protected HashIntMap _classes = new HashIntMap();

    /** Increase this value when object's serialized state is impacted by
     * a class change (modification of fields, inheritance). */
    private static final long serialVersionUID = 1;

    /**
     * The path (relative to the resource directory) at which the
     * serialized recolorization repository should be loaded and stored.
     */
    protected static final String CONFIG_PATH = "config/media/colordefs.dat";
}
