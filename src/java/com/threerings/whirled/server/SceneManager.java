//
// $Id: SceneManager.java,v 1.7 2001/10/24 00:58:12 mdb Exp $

package com.threerings.whirled.server;

import com.threerings.crowd.chat.ChatMessageHandler;
import com.threerings.crowd.chat.ChatService;
import com.threerings.crowd.server.PlaceManager;

import com.threerings.whirled.data.Scene;

public class SceneManager extends PlaceManager
{
    /**
     * Returns the scene object (not the scene distributed object)
     * associated with this scene.
     */
    public Scene getScene ()
    {
        return _scene;
    }

    /**
     * Called by the scene registry once the scene manager has been
     * created (and initialized), but before it is started up.
     */
    protected void postInit (Scene scene, SceneRegistry screg)
    {
        _scene = scene;
        _screg = screg;
    }

    /**
     * We're fully ready to go, so now we register ourselves with the
     * scene registry which will make us available to the clients and
     * system at large.
     */
    protected void didStartup ()
    {
        super.didStartup();

        // let the scene registry know that we're up and running
        _screg.sceneManagerDidStart(this);

        // register a chat message handler because we want to support
        // chatting
        MessageHandler handler = new ChatMessageHandler();
        registerMessageHandler(ChatService.SPEAK_REQUEST, handler);
    }

    /**
     * Called when we have shutdown.
     */
    protected void didShutdown ()
    {
        super.didShutdown();

        // unregister ourselves with the scene registry
        _screg.unmapSceneManager(this);
    }

    // documentation inherited
    protected void toString (StringBuffer buf)
    {
        super.toString(buf);
        buf.append(", scene=").append(_scene);
    }

    protected Scene _scene;
    protected SceneRegistry _screg;
}
