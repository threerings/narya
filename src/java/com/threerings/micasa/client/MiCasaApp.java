//
// $Id: MiCasaApp.java,v 1.1 2001/10/03 23:24:09 mdb Exp $

package com.threerings.micasa.client;

import java.io.IOException;
import com.samskivert.swing.util.SwingUtil;
import com.threerings.micasa.Log;

/**
 * The micasa app is the main point of entry for the MiCasa client
 * application. It creates and initializes the myriad components of the
 * client and sets all the proper wheels in motion.
 */
public class MiCasaApp
{
    public void init ()
        throws IOException
    {
        // create a frame
        _frame = new MiCasaFrame();

        // create our client instance
        _client = new MiCasaClient(_frame);
    }

    public void run ()
    {
        // position everything and show the frame
        _frame.setSize(800, 600);
        SwingUtil.centerWindow(_frame);
        _frame.show();
    }

    public static void main (String[] args)
    {
        MiCasaApp app = new MiCasaApp();
        try {
            // initialize the app
            app.init();
        } catch (IOException ioe) {
            Log.warning("Error initializing application.");
            Log.logStackTrace(ioe);
        }
        // and run it
        app.run();
    }

    protected MiCasaClient _client;
    protected MiCasaFrame _frame;
}
