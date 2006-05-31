//
// $Id: JabberApp.java 3098 2004-08-27 02:12:55Z mdb $
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

package com.threerings.jme.client;

import java.io.InputStream;
import java.io.InputStreamReader;

import com.jme.bounding.BoundingBox;
import com.jme.math.FastMath;
import com.jme.math.Matrix3f;
import com.jme.math.Vector3f;
import com.jme.renderer.ColorRGBA;
import com.jme.scene.shape.Box;
import com.jme.util.LoggingSystem;
import com.jme.util.geom.BufferUtils;
import com.jmex.bui.BStyleSheet;

import com.threerings.util.Name;

import com.threerings.presents.client.Client;
import com.threerings.presents.net.UsernamePasswordCreds;

import com.threerings.jme.Log;
import com.threerings.jme.JmeApp;

/**
 * The main point of entry for the Jabber client application. It creates
 * and initializes the myriad components of the client and sets all the
 * proper wheels in motion.
 */
public class JabberApp extends JmeApp
{
    /** Used to configure our user interface. */
    public static BStyleSheet stylesheet;

    // documentation inherited
    public boolean init ()
    {
        if (!super.init()) {
            return false;
        }

        // initialize our client instance
        _client = new JabberClient();
        _client.init(this);

        // add some simple geometry for kicks
        Vector3f max = new Vector3f(15, 15, 15);
        Vector3f min = new Vector3f(5, 5, 5);

        Box t = new Box("Box", min, max);
        t.setModelBound(new BoundingBox());
        t.updateModelBound();
        t.setLocalTranslation(new Vector3f(0, 0, -15));
        ColorRGBA[] colors = new ColorRGBA[24];
        for (int i = 0; i < 24; i++) {
            colors[i] = ColorRGBA.randomColor();
        }
        t.setColorBuffer(0, BufferUtils.createFloatBuffer(colors));
        _root.attachChild(t);
        _root.updateRenderState();

        // set up the camera
        Vector3f loc = new Vector3f(0, -200, 200);
        _camera.setLocation(loc);
        Matrix3f rotm = new Matrix3f();
        rotm.fromAngleAxis(-FastMath.PI/5, _camera.getLeft());
        rotm.mult(_camera.getDirection(), _camera.getDirection());
        rotm.mult(_camera.getUp(), _camera.getUp());
        rotm.mult(_camera.getLeft(), _camera.getLeft());
        _camera.update();

        // speed up key input
        _input.setActionSpeed(100f);

        return true;
    }

    public void run (String server, String username, String password)
    {
        Client client = _client.getContext().getClient();

        // pass them on to the client
        Log.info("Using [server=" + server + ".");
        client.setServer(server, Client.DEFAULT_SERVER_PORTS);

        // configure the client with some credentials and logon
        if (username != null && password != null) {
            // create and set our credentials
            client.setCredentials(
                new UsernamePasswordCreds(new Name(username), password));
            client.logon();
        }

        // now start up the main event loop
        run();
    }

    // documentation inherited
    public void stop ()
    {
        // log off before we shutdown
        Client client = _client.getContext().getClient();
        if (client.isLoggedOn()) {
            client.logoff(false);
        }
        Log.info("Stopping.");
        super.stop();
    }

    public static void main (String[] args)
    {
        LoggingSystem.getLogger().setLevel(java.util.logging.Level.OFF);

        String server = "localhost";
        if (args.length > 0) {
            server = args[0];
        }

        // load up the default BUI stylesheet
        try {
            InputStream stin = JabberApp.class.getClassLoader().
                getResourceAsStream("rsrc/style.bss");
            stylesheet = new BStyleSheet(
                new InputStreamReader(stin),
                new BStyleSheet.DefaultResourceProvider());
        } catch (Exception e) {
            e.printStackTrace(System.err);
            System.exit(-1);
        }

        String username = (args.length > 1) ? args[1] : null;
        String password = (args.length > 2) ? args[2] : null;

        JabberApp app = new JabberApp();
        app.init();
        app.run(server, username, password);
    }

    protected JabberClient _client;
}
