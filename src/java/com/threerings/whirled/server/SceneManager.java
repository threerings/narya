//
// $Id: SceneManager.java,v 1.4 2001/09/05 01:08:30 shaper Exp $

package com.threerings.whirled.server;

import com.threerings.cocktail.party.chat.ChatMessageHandler;
import com.threerings.cocktail.party.chat.ChatService;
import com.threerings.cocktail.party.server.PlaceManager;

import com.threerings.whirled.data.Scene;

public class SceneManager extends PlaceManager
{
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
        _screg.sceneManagerDidInit(this);

        // register a chat message handler because we want to support
        // chatting
        MessageHandler handler = new ChatMessageHandler();
        registerMessageHandler(ChatService.SPEAK_REQUEST, handler);
    }

    protected void toString (StringBuffer buf)
    {
        super.toString(buf);
        buf.append(", scene=").append(_scene);
    }

    protected Scene _scene;
    protected SceneRegistry _screg;
}
