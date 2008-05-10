
/**
 * The main entry point for the bureau test client to be run in thane. Arguments:
 *   0: the token to use to log back into the server
 *   1: the bureau id of this instance
 *   2: the name of the server to log into
 *   3: the port to connect to on the server
 */

import avmplus.System;
import com.threerings.bureau.client.TestClient;

if (System.argv.length != 4) {
    trace("Expected 4 arguments: (token) (bureauId) (server) (port)");
}

var token :String = System.argv[0];
var bureauId :String = System.argv[1];
var server :String = System.argv[2];
var port :int = parseInt(System.argv[3]);

trace("Token: " + token);
trace("BureauId: " + bureauId);
trace("Server: " + server);
trace("Port: " + port);

// create the client and log on
var client :TestClient = new TestClient(token, bureauId);
client.setServer(server, [port]);
client.logon();

// run it
//client.run();
