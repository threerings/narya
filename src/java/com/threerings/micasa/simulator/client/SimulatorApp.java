//
// $Id: SimulatorApp.java,v 1.11 2002/07/15 03:09:29 mdb Exp $

package com.threerings.micasa.simulator.client;

import javax.swing.JFrame;

import com.samskivert.swing.Controller;
import com.samskivert.swing.util.SwingUtil;
import com.samskivert.util.ResultListener;

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
    public void start (final String[] args) throws Exception
    {
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

        // create the server
        SimulatorServer server = createSimulatorServer();
        server.init(new ResultListener() {
            public void requestCompleted (Object result) {
                try {
                    run();
                } catch (Exception e) {
                    Log.warning("Simulator initialization failed " +
                                "[e=" + e + "].");
                }
            }
            public void requestFailed (Exception e) {
                Log.warning("Simulator initialization failed [e=" + e + "].");
            }
        });

        // run the server on a separate thread
        _serverThread = new ServerThread(server);
        // start up the server so that we can be notified when
        // initialization is complete
        _serverThread.start();
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
        // configure and display the main frame
        JFrame frame = _frame.getFrame();
        // position everything and show the frame
        frame.setSize(800, 600);
        SwingUtil.centerWindow(frame);
        frame.show();

        // start up the client
        Client client = _client.getParlorContext().getClient();

        // obtain the port information from system properties
        int port = Client.DEFAULT_SERVER_PORT;
        String portstr = System.getProperty("port");
        if (portstr != null) {
            try {
                port = Integer.parseInt(portstr);
            } catch (NumberFormatException nfe) {
                Log.warning("Invalid port specification '" + portstr + "'.");
            }
        }
        Log.info("Connecting to localhost:" + port + ".");
        client.setServer("localhost", port);

        // we want to exit when we logged off or failed to log on
        client.addClientObserver(new ClientAdapter() {
            public void clientFailedToLogon (Client c, Exception cause) {
                Log.info("Client failed to logon: " + cause);
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
            app.start(args);
        } catch (Exception e) {
            Log.warning("Error starting up application.");
            Log.logStackTrace(e);
        }
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

    protected SimulatorClient _client;
    protected SimulatorFrame _frame;
    protected ServerThread _serverThread;

    /** The default number of players in the game. */
    protected static final int DEFAULT_PLAYER_COUNT = 2;
}
