//
// $Id: ChatDirector.java,v 1.1 2001/08/02 23:46:47 mdb Exp $

package com.threerings.cocktail.party.chat;

import java.util.ArrayList;
import com.threerings.cocktail.cher.dobj.*;

import com.threerings.cocktail.party.Log;
import com.threerings.cocktail.party.client.LocationObserver;
import com.threerings.cocktail.party.data.PlaceObject;
import com.threerings.cocktail.party.util.PartyContext;

/**
 * The chat manager is the client side coordinator of all chat related
 * services. It handles both place constrainted chat as well as direct
 * messaging.
 */
public class ChatManager
    implements LocationObserver, Subscriber
{
    /**
     * Creates a chat manager and initializes it with the supplied
     * context. The chat manager will register itself as a location
     * observer so that it can automatically process place constrained
     * chat.
     */
    public ChatManager (PartyContext ctx)
    {
        // keep the context around
        _ctx = ctx;

        // register ourselves as a location observer
        _ctx.getLocationManager().addLocationObserver(this);
    }

    /**
     * Adds the supplied chat display to the chat display list. It will
     * subsequently be notified of incoming chat messages as well as tell
     * responses.
     */
    public void addChatDisplay (ChatDisplay display)
    {
        _displays.add(display);
    }

    /**
     * Removes the specified chat display from the chat display list. The
     * display will no longer receive chat related notifications.
     */
    public void removeChatDisplay (ChatDisplay display)
    {
        _displays.remove(display);
    }

    /**
     * Requests that a speak message be generated and delivered to all
     * users that occupy the place object that we currently occupy.
     *
     * @param message the contents of the speak message.
     *
     * @return an id which can be used to coordinate this speak request
     * with the response that will be delivered to all active chat
     * displays when it arrives.
     */
    public int requestSpeak (String message)
    {
    }

    /**
     * Requests that a tell message be delivered to the specified target
     * user.
     *
     * @param target the username of the user to which the tell message
     * should be delivered.
     * @param message the contents of the tell message.
     *
     * @return an id which can be used to coordinate this request with the
     * tell response that will be delivered to all active chat displays
     * when it arrives.
     */
    public int requestTell (String target, String message)
    {
        return -1;
    }

    public boolean locationMayChange (int placeId)
    {
        // we accept all location change requests
        return true;
    }

    public void locationDidChange (PlaceObject place)
    {
        if (_place != null) {
            // unsubscribe from our old object
            _place.removeSubscriber(this);
        }

        // subscribe to the new object
        _place = place;
        _place.addSubscriber(this);
    }

    public void locationChangeFailed (int placeId, String reason)
    {
        // nothing we care about
    }

    public void objectAvailable (DObject object)
    {
        // nothing to do here
    }

    public void requestFailed (int oid, ObjectAccessException cause)
    {
        // nothing to do here
    }

    public boolean handleEvent (DEvent event, DObject target)
    {
        // we only care about message events
        if (!(event instanceof MessageEvent)) {
            return true;
        }

        // and only those of proper name
        MessageEvent mevt = (MessageEvent)event;
        String name = mevt.getName();
        if (name.equals(ChatService.SPEAK_NOTIFICATION)) {
            handleSpeakMessage(mevt.getArgs());
        }

        return true;
    }

    protected void handleSpeakMessage (Object[] args)
    {
        String speaker = (String)args[0];
        String message = (String)args[1];
    }

    protected PartyContext _ctx;
    protected PlaceObject _place;

    protected ArrayList _displays = new ArrayList();
}
