//
// $Id: ChatDirector.java,v 1.13 2001/12/16 21:02:57 mdb Exp $

package com.threerings.crowd.chat;

import java.util.ArrayList;

import com.threerings.presents.client.*;
import com.threerings.presents.dobj.*;

import com.threerings.crowd.Log;
import com.threerings.crowd.client.LocationObserver;
import com.threerings.crowd.data.PlaceObject;
import com.threerings.crowd.util.CrowdContext;

/**
 * The chat director is the client side coordinator of all chat related
 * services. It handles both place constrainted chat as well as direct
 * messaging.
 */
public class ChatDirector
    implements LocationObserver, MessageListener, InvocationReceiver,
               ChatCodes
{
    /**
     * Creates a chat director and initializes it with the supplied
     * context. The chat director will register itself as a location
     * observer so that it can automatically process place constrained
     * chat.
     */
    public ChatDirector (CrowdContext ctx)
    {
        // keep the context around
        _ctx = ctx;

        // register a client observer that will register us as the chat
        // receiver when we log on
        _ctx.getClient().addObserver(new ClientAdapter() {
            public void clientDidLogon (Client client)
            {
                client.getInvocationDirector().registerReceiver(
                    MODULE_NAME, ChatDirector.this);
            }
        });

        // register ourselves as a location observer
        _ctx.getLocationDirector().addLocationObserver(this);
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
     * displays when it arrives, or -1 if we were unable to make the
     * request because we are not currently in a place.
     */
    public int requestSpeak (String message)
    {
        // make sure we're currently in a place
        if (_place == null) {
            return -1;
        }

        // dispatch a speak request on the active place object
        int reqid =
            _ctx.getClient().getInvocationDirector().nextInvocationId();
        Object[] args = new Object[] { new Integer(reqid), message };
        MessageEvent mevt = new MessageEvent(
            _place.getOid(), SPEAK_REQUEST, args);
        _ctx.getDObjectManager().postEvent(mevt);
        return reqid;
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
        return ChatService.tell(_ctx.getClient(), target, message, this);
    }

    /**
     * Adds an additional object via which chat messages may arrive. The
     * chat director assumes the caller will be managing the subscription
     * to this object and will remain subscribed to it for as long as it
     * remains in effect as an auxilliary chat source.
     */
    public void addAuxilliarySource (DObject source)
    {
        source.addListener(this);
    }

    /**
     * Removes a previously added auxilliary chat source.
     */
    public void removeAuxilliarySource (DObject source)
    {
        source.removeListener(this);
    }

    // documentation inherited
    public boolean locationMayChange (int placeId)
    {
        // we accept all location change requests
        return true;
    }

    // documentation inherited
    public void locationDidChange (PlaceObject place)
    {
        if (_place != null) {
            // unlisten to our old object
            _place.removeListener(this);
        }

        // listen to the new object
        _place = place;
        _place.addListener(this);
    }

    // documentation inherited
    public void locationChangeFailed (int placeId, String reason)
    {
        // nothing we care about
    }

    // documentation inherited
    public void messageReceived (MessageEvent event)
    {
        String name = event.getName();
        if (name.equals(ChatService.SPEAK_NOTIFICATION)) {
            handleSpeakMessage(event.getArgs());
        } else if (name.equals(ChatService.SYSTEM_NOTIFICATION)) {
            handleSystemMessage(event.getArgs());
        }
    }

    /**
     * Called by the invocation director when another client has requested
     * a tell message be delivered to this client.
     */
    public void handleTellNotification (String source, String message)
    {
        // pass this on to our chat displays
        for (int i = 0; i < _displays.size(); i++) {
            ChatDisplay display = (ChatDisplay)_displays.get(i);
            display.displayTellMessage(source, message);
        }
    }

    /**
     * Called in response to a tell request that succeeded.
     *
     * @param invid the invocation id of the tell request.
     */
    public void handleTellSucceded (int invid)
    {
        // pass this on to our chat displays
        for (int i = 0; i < _displays.size(); i++) {
            ChatDisplay display = (ChatDisplay)_displays.get(i);
            display.handleResponse(invid, SUCCESS);
        }
    }

    /**
     * Called in response to a tell request that failed.
     *
     * @param invid the invocation id of the tell request.
     * @param reason the code that describes the reason for failure.
     */
    public void handleTellFailed (int invid, String reason)
    {
        // pass this on to our chat displays
        for (int i = 0; i < _displays.size(); i++) {
            ChatDisplay display = (ChatDisplay)_displays.get(i);
            display.handleResponse(invid, reason);
        }
    }

    protected void handleSpeakMessage (Object[] args)
    {
        String speaker = (String)args[0];
        String message = (String)args[1];

        // pass this on to our chat displays
        for (int i = 0; i < _displays.size(); i++) {
            ChatDisplay display = (ChatDisplay)_displays.get(i);
            display.displaySpeakMessage(speaker, message);
        }
    }

    protected void handleSystemMessage (Object[] args)
    {
        String message = (String)args[0];

        // pass this on to our chat displays
        for (int i = 0; i < _displays.size(); i++) {
            ChatDisplay display = (ChatDisplay)_displays.get(i);
            display.displaySystemMessage(message);
        }
    }

    protected CrowdContext _ctx;
    protected PlaceObject _place;
    protected ArrayList _displays = new ArrayList();
}
