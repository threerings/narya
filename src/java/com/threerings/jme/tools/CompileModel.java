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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;
import java.util.logging.Level;

import com.jme.scene.Spatial;
import com.jme.util.LoggingSystem;
import com.jmex.model.XMLparser.Converters.DummyDisplaySystem;

import com.samskivert.util.PropertiesUtil;
import com.samskivert.util.StringUtil;

import com.threerings.jme.model.Model;
import com.threerings.jme.model.ModelMesh;
import com.threerings.jme.model.ModelNode;
import com.threerings.jme.model.SkinMesh;
import com.threerings.jme.tools.xml.AnimationParser;
import com.threerings.jme.tools.xml.ModelParser;

/**
 * An application for compiling 3D models defined in XML to fast-loading binary
 * files.
 */
public class CompileModel
{
    /**
     * Loads the model described by the given properties file and compiles it
     * to a <code>.dat</code> file in the same directory.
     *
     * @return the loaded model, or <code>null</code> if the compiled version
     * is up-to-date
     */
    public static Model compile (File source)
        throws Exception
    {
        String spath = source.toString();
        int didx = spath.lastIndexOf('.');
        String root = (didx == -1) ? spath : spath.substring(0, didx);
        File content = new File(root + ".xml"),
            target = new File(root + ".dat");
        boolean needsUpdate = false;
        if (source.lastModified() >= target.lastModified() ||
            content.lastModified() >= target.lastModified()) {
            needsUpdate = true;
        }

        // load the model properties
        Properties props = new Properties();
        FileInputStream in = new FileInputStream(source);
        props.load(in);
        in.close();
        
        // locate the animations, if any
        String[] anims =
            StringUtil.parseStringArray(props.getProperty("animations", ""));
        File[] afiles = new File[anims.length];
        File dir = source.getParentFile();
        for (int ii = 0; ii < anims.length; ii++) {
            afiles[ii] = new File(dir, anims[ii] + ".xml");
            if (afiles[ii].lastModified() >= target.lastModified()) {
                needsUpdate = true;
            }
        }
        if (!needsUpdate) {
            return null;
        }
        System.out.println("Compiling " + source.getParent() + "...");
        
        // load the model content
        ModelDef mdef = _mparser.parseModel(content.toString());
        HashMap<String, Spatial> nodes = new HashMap<String, Spatial>();
        Model model = mdef.createModel(props, nodes);
        model.initPrototype();
        
        // load the animations, if any
        for (int ii = 0; ii < anims.length; ii++) {
            System.out.println("  Adding " + afiles[ii] + "...");
            AnimationDef adef = _aparser.parseAnimation(afiles[ii].toString());
            model.addAnimation(anims[ii], adef.createAnimation(
                PropertiesUtil.getSubProperties(props, anims[ii]), nodes));
        }
        
        // write and return the model
        model.writeToFile(target);
        return model;
    }
    
    public static void main (String[] args)
    {
        if (args.length < 1) {
            System.err.println("Usage: CompileModel source.properties");
            System.exit(-1);
        }   
        // create a dummy display system
        new DummyDisplaySystem();
        LoggingSystem.getLogger().setLevel(Level.WARNING);
        
        try {
            compile(new File(args[0]));
        } catch (Exception e) {
            System.err.println("Error compiling model: " + e);
        }
    }
    
    /** A parser for the model definitions. */
    protected static ModelParser _mparser = new ModelParser();
    
    /** A parser for the animation definitions. */
    protected static AnimationParser _aparser = new AnimationParser();
}
