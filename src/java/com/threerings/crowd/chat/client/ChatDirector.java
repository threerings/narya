//
// $Id$
//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2004 Three Rings Design, Inc., All Rights Reserved
// http://www.threerings.net/code/narya/
//
// This library is free software; you can redistribute it and/or modify it
// under the terms of the GNU Lesser General Public License as published
// by the Free Software Foundation; either version 2.1 of the License, or
// (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA

package com.threerings.crowd.chat.client;

import java.util.Iterator;
import java.util.LinkedList;

import com.samskivert.util.HashIntMap;
import com.samskivert.util.ObserverList;
import com.samskivert.util.ResultListener;
import com.samskivert.util.StringUtil;

import com.threerings.presents.client.BasicDirector;
import com.threerings.presents.client.Client;
import com.threerings.presents.data.ClientObject;
import com.threerings.presents.dobj.DObject;
import com.threerings.presents.dobj.MessageEvent;
import com.threerings.presents.dobj.MessageListener;

import com.threerings.util.MessageBundle;
import com.threerings.util.MessageManager;
import com.threerings.util.Name;
import com.threerings.util.TimeUtil;

import com.threerings.crowd.Log;
import com.threerings.crowd.client.LocationObserver;
import com.threerings.crowd.data.BodyObject;
import com.threerings.crowd.data.PlaceObject;
import com.threerings.crowd.util.CrowdContext;

import com.threerings.crowd.chat.data.ChatCodes;
import com.threerings.crowd.chat.data.ChatMessage;
import com.threerings.crowd.chat.data.SystemMessage;
import com.threerings.crowd.chat.data.TellFeedbackMessage;
import com.threerings.crowd.chat.data.UserMessage;

/**
 * The chat director is the client side coordinator of all chat related
 * services. It handles both place constrained chat as well as direct
 * messaging.
 */
public class ChatDirector extends BasicDirector
    implements ChatCodes, LocationObserver, MessageListener
{
    /**
     * An interface to receive information about the {@link #MAX_CHATTERS}
     * most recent users that we've been chatting with.
     */
    public static interface ChatterObserver
    {
        /**
         * Called when the list of chatters has been changed.
         */
        public void chattersUpdated (Iterator chatternames);
    }

    /**
     * An interface for those who would like to validate whether usernames
     * may be added to the chatter list.
     */
    public static interface ChatterValidator
    {
        /**
         * Returns whether the username may be added to the chatters list.
         */
        public boolean isChatterValid (Name username);
    }

    /**
     * Creates a chat director and initializes it with the supplied
     * context. The chat director will register itself as a location
     * observer so that it can automatically process place constrained
     * chat.
     */
    public ChatDirector (CrowdContext ctx, MessageManager msgmgr, String bundle)
    {
        super(ctx);

        // keep the context around
        _ctx = ctx;
        _msgmgr = msgmgr;
        _bundle = bundle;

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
     * Adds the specified chat filter to the list of filters.  All
     * chat requests and receipts will be filtered with all filters
     * before they being sent or dispatched locally.
     */
    public void addChatFilter (ChatFilter filter)
    {
        _filters.add(filter);
    }

    /**
     * Removes the specified chat validator from the list of chat validators.
     */
    public void removeChatFilter (ChatFilter filter)
    {
        _filters.remove(filter);
    }

    /**
     * Adds an observer that watches the chatters list, and updates it
     * immediately.
     */
    public void addChatterObserver (ChatterObserver co)
    {
        _chatterObservers.add(co);
        co.chattersUpdated(_chatters.listIterator());
    }

    /**
     * Removes an observer from the list of chatter observers.
     */
    public void removeChatterObserver (ChatterObserver co)
    {
        _chatterObservers.remove(co);
    }

    /**
     * Sets the validator that decides if a username is valid to be
     * added to the chatter list, or null if no such filtering is desired.
     */
    public void setChatterValidator (ChatterValidator validator)
    {
        _chatterValidator = validator;
    }

    /**
     * Adds a chatter to our list of recent chatters.
     */
    protected void addChatter (Name name)
    {
        // check to see if the chatter validator approves..
        if ((_chatterValidator != null) &&
            (!_chatterValidator.isChatterValid(name))) {
            return;
        }

        boolean wasthere = _chatters.remove(name);
        _chatters.addFirst(name);

        if (!wasthere) {
            if (_chatters.size() > MAX_CHATTERS) {
                _chatters.removeLast();
            }

            notifyChatterObservers();
        }
    }

    /**
     * Notifies all registered {@link ChatterObserver}s that the list of
     * chatters has changed.
     */
    protected void notifyChatterObservers ()
    {
        _chatterObservers.apply(new ObserverList.ObserverOp() {
            public boolean apply (Object observer) {
                ((ChatterObserver)observer).chattersUpdated(
                    _chatters.listIterator());
                return true;
            }
        });
    }

    /**
     * Requests that all chat displays clear their contents.
     */
    public void clearDisplays ()
    {
        _displays.apply(new ObserverList.ObserverOp() {
            public boolean apply (Object observer) {
                ((ChatDisplay)observer).clear();
                return true;
            }
        });
    }
    
    /**
     * Display a system INFO message as if it had come from the server.
     * The localtype of the message will be PLACE_CHAT_TYPE.
     *
     * Info messages are sent when something happens that was neither
     * directly triggered by the user, nor requires direct action.
     */
    public void displayInfo (String bundle, String message)
    {
        displaySystem(bundle, message, SystemMessage.INFO, PLACE_CHAT_TYPE);
    }

    /**
     * Display a system INFO message as if it had come from the server.
     *
     * Info messages are sent when something happens that was neither
     * directly triggered by the user, nor requires direct action.
     */
    public void displayInfo (String bundle, String message, String localtype)
    {
        displaySystem(bundle, message, SystemMessage.INFO, localtype);
    }

    /**
     * Display a system FEEDBACK message as if it had come from the server.
     * The localtype of the message will be PLACE_CHAT_TYPE.
     *
     * Feedback messages are sent in direct response to a user action,
     * usually to indicate success or failure of the user's action.
     */
    public void displayFeedback (String bundle, String message)
    {
        displaySystem(
            bundle, message, SystemMessage.FEEDBACK, PLACE_CHAT_TYPE);
    }

    /**
     * Display a system ATTENTION message as if it had come from the server.
     * The localtype of the message will be PLACE_CHAT_TYPE.
     *
     * Attention messages are sent when something requires user action
     * that did not result from direct action by the user.
     */
    public void displayAttention (String bundle, String message)
    {
        displaySystem(
            bundle, message, SystemMessage.ATTENTION, PLACE_CHAT_TYPE);
    }

    /**
     * Display the specified system message as if it had come from the server.
     */
    protected void displaySystem (
        String bundle, String message, byte attLevel, String localtype)
    {
        // nothing should be untranslated, so pass the default bundle if need
        // be.
        if (bundle == null) {
            bundle = _bundle;
        }
        SystemMessage msg = new SystemMessage();
        msg.attentionLevel = attLevel;
        msg.setClientInfo(xlate(bundle, message), localtype);
        dispatchMessage(msg);
    }

    /**
     * Dispatches a {@link #requestSpeak(SpeakService,String,byte)} on the
     * place object that we currently occupy.
     */
    public void requestSpeak (String message)
    {
        requestSpeak(message, DEFAULT_MODE);
    }

    /**
     * Dispatches a {@link #requestSpeak(SpeakService,String,byte)} on the
     * place object that we currently occupy.
     */
    public void requestSpeak (String message, byte mode)
    {
        // make sure we're currently in a place
        if (_place == null) {
            return;
        }

        // dispatch a speak request on the active place object
        requestSpeak(_place.speakService, message, mode);
    }

    /**
     * Requests that a speak message with the specified mode be generated
     * and delivered via the supplied speak service instance (which will
     * be associated with a particular "speak object"). The message will
     * first be validated by all registered {@link ChatValidator}s (and
     * possibly vetoed) before being dispatched.
     *
     * @param speakService the speak service to use when generating the
     * speak request.
     * @param message the contents of the speak message.
     * @param mode a speech mode that will be interpreted by the {@link
     * ChatDisplay} implementations that eventually display this speak
     * message.
     */
    public void requestSpeak (
        SpeakService speakService, String message, byte mode)
    {
        // make sure they can say what they want to say
        message = filter(message, null, true);
        if (message == null) {
            return;
        }

        // dispatch a speak request using the supplied speak service
        speakService.speak(_ctx.getClient(), message, mode);
    }

    /**
     * Requests to send a site-wide broadcast message.
     *
     * @param message the contents of the message.
     */
    public void requestBroadcast (String message)
    {
        message = filter(message, null, true);
        if (message == null) {
            displayFeedback(_bundle,
                MessageBundle.compose("m.broadcast_failed", "m.filtered"));
            return;
        }

        _cservice.broadcast(
            _ctx.getClient(), message, new ChatService.InvocationListener() {
                public void requestFailed (String reason) {
                    reason = MessageBundle.compose(
                        "m.broadcast_failed", reason);
                    displayFeedback(_bundle, reason);
                }
            });
    }

    /**
     * Requests that a tell message be delivered to the specified target
     * user.
     *
     * @param target the username of the user to which the tell message
     * should be delivered.
     * @param message the contents of the tell message.
     * @param rl an optional result listener if you'd like to be notified
     * of success or failure.
     */
    public void requestTell (final Name target, String msg,
                             final ResultListener rl)
    {
        // make sure they can say what they want to say
        final String message = filter(msg, target, true);
        if (message == null) {
            if (rl != null) {
                rl.requestFailed(null);
            }
            return;
        }

        // create a listener that will report success or failure
        ChatService.TellListener listener = new ChatService.TellListener() {
            public void tellSucceeded (long idletime, String awayMessage) {
                success(xlate(_bundle, MessageBundle.tcompose(
                                  "m.told_format", target, message)));

                // if they have an away message, report that
                if (awayMessage != null) {
                    awayMessage = filter(awayMessage, target, false);
                    if (awayMessage != null) {
                        String msg = MessageBundle.tcompose(
                            "m.recipient_afk", target, awayMessage);
                        displayFeedback(_bundle, msg);
                    }
                }

                // if they are idle, report that
                if (idletime > 0L) {
                    // adjust by the time it took them to become idle
                    idletime += _ctx.getConfig().getValue(
                        IDLE_TIME_KEY, DEFAULT_IDLE_TIME);
                    String msg = MessageBundle.compose(
                        "m.recipient_idle", MessageBundle.taint(target),
                        TimeUtil.getTimeOrderString(idletime, TimeUtil.MINUTE));
                    displayFeedback(_bundle, msg);
                }
            }

            protected void success (String feedback) {
                dispatchMessage(new TellFeedbackMessage(feedback));
                addChatter(target);
                if (rl != null) {
                    rl.requestCompleted(target);
                }
            }

            public void requestFailed (String reason) {
                String msg = MessageBundle.compose(
                    "m.tell_failed", MessageBundle.taint(target), reason);
                displayFeedback(_bundle, msg);
                if (rl != null) {
                    rl.requestFailed(null);
                }
            }
        };

        _cservice.tell(_ctx.getClient(), target, message, listener);
    }

    /**
     * Configures a message that will be automatically reported to anyone
     * that sends a tell message to this client to indicate that we are
     * busy or away from the keyboard.
     */
    public void setAwayMessage (String message)
    {
        if (message != null) {
            message = filter(message, null, true);
            if (message == null) {
                // they filtered away their own away message..
                // change it to something
                message = "...";
            }
        }
        // pass the buck right on along
        _cservice.away(_ctx.getClient(), message);
    }

    /**
     * Adds an additional object via which chat messages may arrive. The
     * chat director assumes the caller will be managing the subscription
     * to this object and will remain subscribed to it for as long as it
     * remains in effect as an auxiliary chat source.
     *
     * @param localtype a type to be associated with all chat messages
     * that arrive on the specified DObject.
     */
    public void addAuxiliarySource (DObject source, String localtype)
    {
        source.addListener(this);
        _auxes.put(source.getOid(), localtype);
    }

    /**
     * Removes a previously added auxiliary chat source.
     */
    public void removeAuxiliarySource (DObject source)
    {
        source.removeListener(this);
        _auxes.remove(source.getOid());
    }

    // documentation inherited from interface
    protected void fetchServices (Client client)
    {
        // get a handle on our chat service
        _cservice = (ChatService)client.requireService(ChatService.class);
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
        if (_place != null) {
            _place.addListener(this);
        }
    }

    // documentation inherited
    public void locationChangeFailed (int placeId, String reason)
    {
        // nothing we care about
    }

    // documentation inherited
    public void messageReceived (MessageEvent event)
    {
        if (CHAT_NOTIFICATION.equals(event.getName())) {
            ChatMessage msg = (ChatMessage) event.getArgs()[0];
            String localtype = getLocalType(event.getTargetOid());
            String message = msg.message;
            String autoResponse = null;

            // if the message came from a user, make sure we want to hear it
            if (msg instanceof UserMessage) {
                UserMessage umsg = (UserMessage)msg;
                Name speaker = umsg.speaker;
                if ((message = filter(message, speaker, false)) == null) {
                    return;
                }

                if (USER_CHAT_TYPE.equals(localtype) &&
                    umsg.mode == ChatCodes.DEFAULT_MODE) {
                    // if it was a tell, add the speaker as a chatter
                    addChatter(speaker);

                    // note whether or not we have an auto-response
                    BodyObject self = (BodyObject)
                        _ctx.getClient().getClientObject();
                    if (!StringUtil.blank(self.awayMessage)) {
                        autoResponse = self.awayMessage;
                    }
                }
            }

            // initialize the client-specific fields of the message
            msg.setClientInfo(xlate(msg.bundle, message), localtype);

            // and send it off!
            dispatchMessage(msg);

            // if we auto-responded, report as much
            if (autoResponse != null) {
                Name teller = ((UserMessage) msg).speaker;
                String amsg = MessageBundle.tcompose(
                    "m.auto_responded", teller, autoResponse);
                displayFeedback(_bundle, amsg);
            }
        }
    }

    /**
     * Translates the specified message using the specified bundle.
     */
    protected String xlate (String bundle, String message)
    {
        if (bundle != null && _msgmgr != null) {
            MessageBundle msgb = _msgmgr.getBundle(bundle);
            if (msgb == null) {
                Log.warning(
                    "No message bundle available to translate message " +
                    "[bundle=" + bundle + ", message=" + message + "].");
            } else {
                message = msgb.xlate(message);
            }
        }
        return message;
    }

    /**
     * Dispatches the provided message to our chat displays.
     */
    protected void dispatchMessage (ChatMessage message)
    {
        _displayMessageOp.setMessage(message);
        _displays.apply(_displayMessageOp);
    }

    /**
     * Looks up and returns the message type associated with the specified
     * oid.
     */
    protected String getLocalType (int oid)
    {
        String type = (String)_auxes.get(oid);
        return (type == null) ? PLACE_CHAT_TYPE : type;
    }

    /**
     * Used to assign unique ids to all speak requests.
     */
    protected synchronized int nextRequestId ()
    {
        return _requestId++;
    }

    // documentation inherited
    public void clientDidLogon (Client client)
    {
        super.clientDidLogon(client);

        // listen on the client object for tells
        addAuxiliarySource(_clobj = client.getClientObject(), USER_CHAT_TYPE);
    }

    // documentation inherited
    public void clientObjectDidChange (Client client)
    {
        super.clientObjectDidChange(client);

        // change what we're listening to for tells
        removeAuxiliarySource(_clobj);
        addAuxiliarySource(_clobj = client.getClientObject(), USER_CHAT_TYPE);

        clearDisplays();
    }

    // documentation inherited
    public void clientDidLogoff (Client client)
    {
        super.clientDidLogoff(client);

        // stop listening to it for tells
        if (_clobj != null) {
            removeAuxiliarySource(_clobj);
            _clobj = null;
        }
        // in fact, clear out all auxiliary sources
        _auxes.clear();

        clearDisplays();

        // clear out the list of people we've chatted with
        _chatters.clear();
        notifyChatterObservers();

        // clear the _place
        locationDidChange(null);

        // clear our service
        _cservice = null; 
    }

    /**
     * Run a message through all the currently registered filters.
     */
    public String filter (String msg, Name otherUser, boolean outgoing)
    {
        _filterMessageOp.setMessage(msg, otherUser, outgoing);
        _filters.apply(_filterMessageOp);
        return _filterMessageOp.getMessage();
    }

    /**
     * An operation that checks with all chat filters to properly filter
     * a message prior to sending to the server or displaying.
     */
    protected static class FilterMessageOp implements ObserverList.ObserverOp
    {
        public void setMessage (String msg, Name otherUser, boolean outgoing)
        {
            _msg = msg;
            _otherUser = otherUser;
            _out = outgoing;
        }

        public boolean apply (Object observer)
        {
            if (_msg != null) {
                _msg = ((ChatFilter) observer).filter(_msg, _otherUser, _out);
            }
            return true;
        }

        public String getMessage ()
        {
            return _msg;
        }

        protected Name _otherUser;
        protected String _msg;
        protected boolean _out;
    }

    /**
     * An observer op used to dispatch ChatMessages on the client.
     */
    protected static class DisplayMessageOp implements ObserverList.ObserverOp
    {
        public void setMessage (ChatMessage message)
        {
            _message = message;
        }

        public boolean apply (Object observer)
        {
            ((ChatDisplay)observer).displayMessage(_message);
            return true;
        }

        protected ChatMessage _message;
    }

    /** Our active chat context. */
    protected CrowdContext _ctx;

    /** Provides access to chat-related server-side services. */
    protected ChatService _cservice;

    /** The message manager. */
    protected MessageManager _msgmgr;

    /** The bundle to use for our own internal messages. */
    protected String _bundle;

    /** The place object that we currently occupy. */
    protected PlaceObject _place;

    /** The client object that we're listening to for tells. */
    protected ClientObject _clobj;

    /** A list of registered chat displays. */
    protected ObserverList _displays =
        new ObserverList(ObserverList.FAST_UNSAFE_NOTIFY);

    /** A list of registered chat filters. */
    protected ObserverList _filters =
        new ObserverList(ObserverList.FAST_UNSAFE_NOTIFY);

    /** A mapping from auxiliary chat objects to the types under which
     * they are registered. */
    protected HashIntMap _auxes = new HashIntMap();

    /** Validator of who may be added to the chatters list. */
    protected ChatterValidator _chatterValidator;

    /** Usernames of users we've recently chatted with. */
    protected LinkedList _chatters = new LinkedList();

    /** Observers that are watching our chatters list. */
    protected ObserverList _chatterObservers =
        new ObserverList(ObserverList.FAST_UNSAFE_NOTIFY);

    /** Used by {@link #nextRequestId}. */
    protected int _requestId;

    /** Operation used to filter chat messages. */
    protected FilterMessageOp _filterMessageOp = new FilterMessageOp();

    /** Operation used to display chat messages. */
    protected DisplayMessageOp _displayMessageOp = new DisplayMessageOp();

    /** The maximum number of chatter usernames to track. */
    protected static final int MAX_CHATTERS = 6;
}
