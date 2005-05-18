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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;

import com.jme.app.SimpleGame;
import com.jme.renderer.lwjgl.LWJGLRenderer;
import com.jme.system.DisplaySystem;
import com.jmex.model.XMLparser.Converters.AseToJme;
import com.jmex.model.XMLparser.Converters.FormatConverter;
import com.jmex.model.XMLparser.Converters.MaxToJme;
import com.jmex.model.XMLparser.Converters.Md2ToJme;
import com.jmex.model.XMLparser.Converters.Md3ToJme;
import com.jmex.model.XMLparser.Converters.ObjToJme;

/**
 * A tool for converting various 3D model formats into JME's internal
 * format.
 */
public class ConvertModel extends SimpleGame
{
    public static void main (String[] args)
    {
        if (args.length < 2) {
            System.err.println("Usage: ConvertModel source.ext dest.jme");
            System.exit(-1);
        }

        ConvertModel app = new ConvertModel();
        app._source = new File(args[0]);
        app._target = new File(args[1]);

        app.setDialogBehaviour(
            SimpleGame.FIRSTRUN_OR_NOCONFIGFILE_SHOW_PROPS_DIALOG);
        app.start();
    }

    protected void simpleInitGame ()
    {
        String path = _source.getPath().toLowerCase();
        String type = path.substring(path.lastIndexOf(".") + 1);

        // set up our converter
        FormatConverter convert = null;
        if (type.equals("obj")) {
            convert = new ObjToJme();
            try {
                convert.setProperty("mtllib", new URL("file:" + _source));
            } catch (Exception e) {
                System.err.println("Failed to create material URL: " + e);
                System.exit(-1);
            }
        } else if (type.equals("3ds")) {
            convert = new MaxToJme();
        } else if (type.equals("md2")) {
            convert = new Md2ToJme();
        } else if (type.equals("md3")) {
            convert = new Md3ToJme();
        } else if (type.equals("ase")) {
            convert = new AseToJme();
        } else {
            System.err.println("Unknown model type '" + type + "'.");
            System.exit(-1);
        }

        // and do the deed
        try {
            BufferedOutputStream bout = new BufferedOutputStream(
                new FileOutputStream(_target));
            BufferedInputStream bin = new BufferedInputStream(
                new FileInputStream(_source));
            convert.convert(bin, bout);
            bout.close();
        } catch (IOException ioe) {
            System.err.println("Error converting '" + _source +
                               "' to '" + _target + "'.");
            ioe.printStackTrace(System.err);
            System.exit(-1);
        }

        finish();
    }

    protected File _source, _target;
}
