//
// $Id: SimulatorManager.java,v 1.3 2001/12/19 23:30:47 shaper Exp $

package com.threerings.micasa.simulator.server;

import java.util.ArrayList;

import com.samskivert.util.Config;

import com.threerings.presents.dobj.DObject;
import com.threerings.presents.dobj.ObjectAccessException;
import com.threerings.presents.dobj.Subscriber;
import com.threerings.presents.server.InvocationManager;
import com.threerings.presents.server.InvocationProvider;

import com.threerings.crowd.data.BodyObject;
import com.threerings.crowd.data.PlaceObject;
import com.threerings.crowd.server.LocationProvider;
import com.threerings.crowd.server.PlaceManager;
import com.threerings.crowd.server.PlaceRegistry;
import com.threerings.crowd.server.PlaceRegistry.CreationObserver;

import com.threerings.parlor.game.GameConfig;
import com.threerings.parlor.game.GameManager;
import com.threerings.parlor.game.GameObject;

import com.threerings.micasa.Log;
import com.threerings.micasa.simulator.client.Simulant;
import com.threerings.micasa.simulator.client.SimulatorCodes;

/**
 * The simulator manager is responsible for handling the simulator
 * services on the server side.
 */
public class SimulatorManager
    implements SimulatorCodes
{
    /**
     * Initializes the simulator manager manager. This should be called by
     * the server that is making use of the simulator services on the
     * single instance of simulator manager that it has created.
     *
     * @param config the configuration object in use by this server.
     * @param invmgr a reference to the invocation manager in use by this
     * server.
     */
    public void init (Config config, InvocationManager invmgr)
    {
        // register our simulator provider
        SimulatorProvider sprov = new SimulatorProvider(this);
        invmgr.registerProvider(MODULE_NAME, sprov);
    }

    /**
     * Creates a game along with the specified number of simulant players
     * and forcibly moves all players into the game room.
     */
    public void createGame (
        BodyObject source, GameConfig config, String simClass, int playerCount)
    {
        new CreateGameTask(source, config, simClass, playerCount);
    }

    public static class CreateGameTask implements CreationObserver
    {
        public CreateGameTask (
            BodyObject source, GameConfig config, String simClass,
            int playerCount)
        {
            // save off game request info
            _source = source;
            _config = config;
            _simClass = simClass;
            _playerCount = playerCount;
        
            try {
                // create the game manager and begin its initialization
                // process. the game manager will take care of notifying
                // the players that the game has been created once it has
                // been started up (which is done by the place registry
                // once the game object creation has completed)

                // we needn't hang around and wait for game object
                // creation if it's just us
                CreationObserver obs = (_playerCount == 1) ? null : this;
                _gmgr = (GameManager)
                    SimulatorServer.plreg.createPlace(config, obs);

                // give the game manager the player names
                String[] names = new String[_playerCount];
                names[0] = _source.username;
                for (int ii = 1; ii < _playerCount; ii++) {
                    names[ii] = "simulant" + ii;
                }
                _gmgr.setPlayers(names);

            } catch (Exception e) {
                Log.warning("Unable to create game manager [e=" + e + "].");
                Log.logStackTrace(e);
            }
        }

        // documentation inherited
        public void placeCreated (PlaceObject place, PlaceManager pmgr)
        {
            // cast the place to the game object for the game we're creating
            _gobj = (GameObject)place;

            // create a subscriber to await creation of each simulant body
            // object
            Subscriber sub = new Subscriber() {
                public void objectAvailable (DObject object)
                {
                    // set up the simulant's body object
                    BodyObject bobj = (BodyObject)object;
                    bobj.username = "simulant" + (_sims.size() + 1);

                    // hold onto it for later game creation
                    _sims.add(bobj);

                    // create the game if we've received all body objects
                    if (_sims.size() == (_playerCount - 1)) {
                        createSimulants();
                    }
                }

                public void requestFailed (
                    int oid, ObjectAccessException cause)
                {
                    Log.warning("Unable to create simulant body object " +
                                "[error=" + cause + "].");
                }
            };

            // fire off simulant body object creation requests
            Class simobjClass = SimulatorServer.clmgr.getClientObjectClass();
            for (int ii = 1; ii < _playerCount; ii++) {
                SimulatorServer.omgr.createObject(simobjClass, sub);
            }
        }

        /**
         * Called when all simulant body objects are present and the
         * simulants are ready to be created.
         */
        protected void createSimulants ()
        {
            // finish setting up the simulants
            for (int ii = 1; ii < _playerCount; ii++) {
                // create the simulant object
                Simulant sim;
                try {
                    sim = (Simulant)Class.forName(_simClass).newInstance();
                } catch (Exception e) {
                    Log.warning("Unable to create simulant " +
                                "[class=" + _simClass + "].");
                    return;
                }

                // give the simulant its body
                BodyObject bobj = (BodyObject)_sims.get(ii - 1);
                sim.init(bobj, _config);

                // give the simulant a chance to engage in place antics
                sim.willEnterPlace(_gobj);

                // move the simulant into the game room since they have no
                // location director to move them automagically
                try {
                    LocationProvider.moveTo(bobj, _gobj.getOid());
                } catch (Exception e) {
                    Log.warning("Failed to move simulant into room " +
                                "[e=" + e + "].");
                    return;
                }
            }
        }

        /** The simulant body objects. */
        protected ArrayList _sims = new ArrayList();

        /** The game object for the game being created. */
        protected GameObject _gobj;

        /** The game manager for the game being created. */
        protected GameManager _gmgr;

        /** The number of players in the game. */
        protected int _playerCount;

        /** The simulant class instantiated on game creation. */
        protected String _simClass;

        /** The game config object. */
        protected GameConfig _config;

        /** The body object of the player requesting the game creation. */
        protected BodyObject _source;
    }
}
