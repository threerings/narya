//
// $Id: SimulatorApp.java,v 1.6 2002/04/03 21:53:17 shaper Exp $

package com.threerings.micasa.simulator.client;

import javax.swing.JFrame;

import com.samskivert.swing.Controller;
import com.samskivert.swing.util.SwingUtil;

import com.threerings.presents.client.Client;
import com.threerings.presents.client.ClientAdapter;
import com.threerings.presents.net.Credentials;
import com.threerings.presents.net.UsernamePasswordCreds;

import com.threerings.micasa.Log;
import com.threerings.micasa.simulator.data.SimulatorInfo;
import com.threerings.micasa.simulator.server.SimpleServer;
import com.threerings.micasa.simulator.server.SimulatorServer;

/**
 * The simulator application is a test harness to facilitate development
 * and debugging of games.
 */
public class SimulatorApp
{
    public void init (String[] args) throws Exception
    {
        // create the server
        SimulatorServer server = createSimulatorServer();
        server.init();
        _serverThread = new ServerThread(server);

        // create a frame
        _frame = createSimulatorFrame();

        // create the simulator info object
        SimulatorInfo siminfo = new SimulatorInfo();
        siminfo.gameConfigClass = args[0];
        siminfo.simClass = args[1];
        siminfo.playerCount = getInt(
            System.getProperty("playercount"), DEFAULT_PLAYER_COUNT);

        // create our client instance
        _client = createSimulatorClient(_frame);

        // set up the top-level client controller
        Controller ctrl = new ClientController(
            _client.getParlorContext(), _frame, siminfo);
        _frame.setController(ctrl);
    }

    protected SimulatorServer createSimulatorServer ()
    {
        return new SimpleServer();
    }

    protected SimulatorFrame createSimulatorFrame ()
    {
        return new SimpleFrame();
    }

    protected SimulatorClient createSimulatorClient (SimulatorFrame frame)
        throws Exception
    {
        return new SimpleClient(_frame);
    }

    public void run ()
    {
        // size and display the window
        int wid = getInt(System.getProperty("width"), 800);
        int hei = getInt(System.getProperty("height"), 600);
        JFrame frame = _frame.getFrame();
        frame.setSize(wid, hei);
        SwingUtil.centerWindow(frame);
        frame.show();

        // start up the server
        _serverThread.start();

        // start up the client
        Client client = _client.getParlorContext().getClient();

        // we're connecting to our own server
        client.setServer("localhost", Client.DEFAULT_SERVER_PORT);

        // we want to exit when we logged off or failed to log on
        client.addClientObserver(new ClientAdapter() {
            public void clientFailedToLogon (Client c, Exception cause) {
                System.exit(0);
            }
            public void clientDidLogoff (Client c) {
                System.exit(0);
            }
        });

        // configure the client with some credentials and logon
        String username = System.getProperty("username");
        if (username == null) {
            username =
                "bob" + ((int)(Math.random() * Integer.MAX_VALUE) % 500);
        }
        String password = System.getProperty("password");
        if (password == null) {
            password = "test";
        }

        // create and set our credentials
        Credentials creds = new UsernamePasswordCreds(username, password);
        client.setCredentials(creds);
        client.logon();
    }

    public static void main (String[] args)
    {
        if (!checkArgs(args)) {
            return;
        }

        SimulatorApp app = new SimulatorApp();
        try {
            app.init(args);
        } catch (Exception e) {
            Log.warning("Error initializing application.");
            Log.logStackTrace(e);
        }

        app.run();
    }

    protected static boolean checkArgs (String[] args)
    {
        if (args.length < 2) {
            String msg = "Usage:\n" +
                "    java com.threerings.simulator.SimulatorApp " +
                "<game config class name> <simulant class name>\n" +
                "Optional properties:\n" +
                "    -Dusername=<user>\n" +
                "    -Dplayercount=<number>\n" +
                "    -Dwidth=<width>\n" +
                "    -Dheight=<height>";
            System.out.println(msg);
            return false;
        }

        return true;
    }

    protected int getInt (String value, int defval)
    {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException nfe) {
            return defval;
        }
    }

    protected static class ServerThread extends Thread
    {
        public ServerThread (SimulatorServer server)
        {
            _server = server;
        }

        public void run ()
        {
            _server.run();
        }

        protected SimulatorServer _server;
    }

    /** The default number of players in the game. */
    protected static final int DEFAULT_PLAYER_COUNT = 2;

    protected SimulatorClient _client;
    protected SimulatorFrame _frame;
    protected ServerThread _serverThread;
}
