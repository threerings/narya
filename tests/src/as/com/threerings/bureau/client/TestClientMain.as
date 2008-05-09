
/**
 * The main entry point for the bureau test client to be run in thane. Arguments:
 *   0: the token to use to log back into the server
 *   1: the bureau id of this instance
 *   2: the name of the server to log into
 *   3: the port to connect to on the server
 */

import avmplus.System;

for (var i :int = 0; i < System.argv.length; ++i) {
    print("Argv[" + i + "] = " + System.argv[i]);
}

/*
// create the client and log on
var client :TestClient = new TestClient(
    System.getProperty("token"), 
    System.getProperty("bureauId"));
client.setServer(
    System.getProperty("serverName"), 
    new int[] {Integer.parseInt(System.getProperty("serverPort"))});
client.logon();

// run it
client.run();
*/
