//
// $Id: ChatDirector.java,v 1.31 2002/07/27 01:58:57 ray Exp $

package com.threerings.crowd.chat;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;

import com.samskivert.util.HashIntMap;
import com.samskivert.util.Tuple;

import com.threerings.presents.client.*;
import com.threerings.presents.dobj.*;

import com.threerings.util.MessageBundle;
import com.threerings.util.MessageManager;

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
     * An interface that can receive information about the 6 most recent
     * users that we've been chatting with.
     */
    public static interface ChatterObserver
    {
        /**
         * The list of chatters has been changed.
         */
        public void chattersUpdated (Iterator chatternames);
    }

    /**
     * Creates a chat director and initializes it with the supplied
     * context. The chat director will register itself as a location
     * observer so that it can automatically process place constrained
     * chat.
     */
    public ChatDirector (CrowdContext ctx, MessageManager msgmgr, String bundle)
    {
        // keep the context around
        _ctx = ctx;
        _msgmgr = msgmgr;
        _bundle = bundle;

        // register for chat notifications
        _ctx.getClient().getInvocationDirector().registerReceiver(
            MODULE_NAME, this);

        // register ourselves as a location observer
        _ctx.getLocationDirector().addLocationObserver(this);
    }

    /**
     * Sets the mute director, if one is desired.
     */
    public void setMuteDirector (MuteDirector muter)
    {
        if (_muter == null) {
            _muter = muter;
            _muter.setChatDirector(this);
        }
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
     * Add the specified chat validator to the list of validators.
     * All chat requests will be validated with all validators before
     * they may be accepted.
     */
    public void addChatValidator (ChatValidator validator)
    {
        _validators.add(validator);
    }

    /**
     * Removes the specified chat validator from the list of chat validators.
     */
    public void removeChatValidator (ChatValidator validator)
    {
        _validators.remove(validator);
    }

    /**
     * Add an Observer that is watching the Chatters list, and update
     * it immediately.
     */
    public void addChatterObserver (ChatterObserver co)
    {
        _chatterObservers.add(co);
        co.chattersUpdated(_chatters.listIterator());
    }

    /**
     * Remove a Chatters list observer.
     */
    public void removeChatterObserver (ChatterObserver co)
    {
        _chatterObservers.remove(co);
    }

    /**
     * Add a chatter to our list of recent chatters.
     */
    protected void addChatter (String name)
    {
        boolean wasthere = _chatters.remove(name);
        _chatters.addFirst(name);

        if (!wasthere) {
            if (_chatters.size() > MAX_CHATTERS) {
                _chatters.removeLast();
            }

            for (Iterator iter = _chatterObservers.iterator();
                iter.hasNext(); ) {
                ChatterObserver co = (ChatterObserver) iter.next();
                co.chattersUpdated(_chatters.listIterator());
            }
        }
    }

    /**
     * Requests that the specified system message be dispatched to all
     * registered chat displays. The message will be delivered as if it
     * were received on the main chat object (meaning the chat type will
     * be {@link ChatCodes#PLACE_CHAT_TYPE}).
     *
     * @param bundle the message bundle identifier that should be used to
     * localize this message.
     * @param message the localizable message string.
     */
    public void displaySystemMessage (String bundle, String message)
    {
        displaySystemMessage(bundle, message, PLACE_CHAT_TYPE);
    }

    /**
     * Requests that the specified system message be dispatched to all
     * registered chat displays.
     *
     * @param bundle the message bundle identifier that should be used to
     * localize this message.
     * @param message the localizable message string.
     * @param localtype {@link ChatCodes#PLACE_CHAT_TYPE} if the message was
     * received on the place object or the type associated with the
     * auxiliary chat object on which the message was received.
     */
    public void displaySystemMessage (
        String bundle, String message, String localtype)
    {
        dispatchMessage(new SystemMessage(xlate(bundle, message), localtype));
    }

    /**
     * Display the feedback message, translated with the default bundle.
     */
    public void displayFeedbackMessage (String message)
    {
        displayFeedbackMessage(_bundle, message);
    }

    /**
     * Display a feedback message.
     */
    public void displayFeedbackMessage (String bundle, String message)
    {
        displayFeedbackMessage(bundle, message, PLACE_CHAT_TYPE);
    }

    /**
     * Display a feedback message.
     */
    public void displayFeedbackMessage (String bundle, String message,
        String localtype)
    {
        dispatchMessage(new FeedbackMessage(xlate(bundle, message), localtype));
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

        // make sure they can say what they want to say
        for (Iterator iter = _validators.iterator(); iter.hasNext(); ) {
            if (!((ChatValidator) iter.next()).validateSpeak(message)) {
                return -1;
            }
        }

        // dispatch a speak request on the active place object
        int reqid =
            _ctx.getClient().getInvocationDirector().nextInvocationId();
        Object[] args = new Object[] { new Integer(reqid), message };
        MessageEvent mevt = new MessageEvent(
            _place.getOid(), SPEAK_REQUEST, args);
        _ctx.getDObjectManager().postEvent(mevt);
        // TODO: when this gets changed such that we actually validate
        // this on the server, we have to make sure that the
        // user is not on a portal before we allow the 'shout' to go through
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
        // make sure they can say what they want to say
        for (Iterator iter = _validators.iterator(); iter.hasNext(); ) {
            if (!((ChatValidator) iter.next()).validateTell(
                    target, message)) {
                return -1;
            }
        }

        int invid = ChatService.tell(_ctx.getClient(), target, message, this);
        // cache the tell info for use when reporting success or failure
        // to our various chat displays
        _tells.put(invid, new Tuple(target, message));
        return invid;
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
        String name = event.getName();
        if (name.equals(ChatService.SPEAK_NOTIFICATION)) {
            handleSpeakMessage(getLocalType(event.getTargetOid()),
                event.getArgs());
        } else if (name.equals(ChatService.SYSTEM_NOTIFICATION)) {
            handleSystemMessage(getLocalType(event.getTargetOid()),
                event.getArgs());
        }
    }

    /**
     * Called by the invocation director when another client has requested
     * a tell message be delivered to this client.
     */
    public void handleTellNotification (String source, String message)
    {
        if (isBlocked(source)) {
            return;
        }

        dispatchMessage(new UserMessage(message, ChatCodes.TELL_CHAT_TYPE,
            source, ChatCodes.DEFAULT_MODE));
        addChatter(source);
    }

    /**
     * Called by the invocation director when an entity on the server has
     * requested that we deliver a tell notification to this
     * client. Because the server generated the notification, displays
     * will want to translate the message itself using the supplied bundle
     * identifier.
     */
    public void handleTellNotification (
        String source, String bundle, String message)
    {
        handleTellNotification(source, xlate(bundle, message));
    }

    /**
     * Called in response to a tell request that succeeded.
     *
     * @param invid the invocation id of the tell request.
     */
    public void handleTellSucceeded (int invid)
    {
        // remove the tell info for the successful request
        Tuple tup = (Tuple)_tells.remove(invid);
        if (tup == null) {
            Log.warning("Notified of successful tell request but no " +
                        "tell info available [invid=" + invid + "].");
            return;
        }

        // pass this on to our chat displays
        String target = (String)tup.left, message = (String)tup.right;
        displayFeedbackMessage(_bundle,
            MessageBundle.tcompose("m.told_format", target, message));
        addChatter(target);
    }

    /**
     * Called in response to a tell request that failed.
     *
     * @param invid the invocation id of the tell request.
     * @param reason the code that describes the reason for failure.
     */
    public void handleTellFailed (int invid, String reason)
    {
        // remove the tell info for the failed request
        Tuple tup = (Tuple)_tells.remove(invid);
        if (tup == null) {
            Log.warning("Notified of failed tell request but no " +
                        "tell info available [invid=" + invid + "].");
            return;
        }

        // pass this on to our chat displays
        String target = (String)tup.left;

        displayFeedbackMessage(_bundle,
            MessageBundle.compose("m.tell_failed", target, reason));
    }

    /**
     * Called when a speak message is received on the place object or one
     * of our auxiliary chat objects.
     *
     * @param type {@link ChatCodes#PLACE_CHAT_TYPE} if the message was
     * received on the place object or the type associated with the
     * auxiliary chat object on which the message was received.
     * @param args the arguments provided with the speak notification.
     */
    protected void handleSpeakMessage (String localtype, Object[] args)
    {
        String speaker = (String)args[0];
        if (isBlocked(speaker)) {
            return;
        }

        String message;
        byte mode;

        // determine whether this speak message originated from another
        // client or from a server entity
        if (args.length == 3) {
            message = (String)args[1];
            mode = ((Byte) args[2]).byteValue();

        } else {
            message = xlate((String) args[1], (String) args[2]);
            mode = ((Byte) args[3]).byteValue();
        }

        dispatchMessage(new UserMessage(message, localtype, speaker, mode));
    }

    /**
     * Called when a system message is delivered on one of our chat
     * objects.
     *
     * @param type {@link ChatCodes#PLACE_CHAT_TYPE} if the message was
     * received on the place object or the type associated with the
     * auxiliary chat object on which the message was received.
     * @param args the arguments provided with the system message
     * notification.
     */
    protected void handleSystemMessage (String localtype, Object[] args)
    {
        displaySystemMessage((String) args[0], (String) args[1], localtype);
    }

    /**
     * Translate the specified message using the specified bundle.
     */
    protected String xlate (String bundle, String message)
    {
        if (bundle != null && _msgmgr != null) {
            MessageBundle msgb = _msgmgr.getBundle(bundle);
            if (msgb == null) {
                Log.warning("No message bundle available to translate " +
                    "message [bundle=" + bundle + ", message=" +
                    message + "].");
            } else {
                message = msgb.xlate(message);
            }
        }
        return message;
    }

    /**
     * Dispatch the provided message to our ChatDisplays.
     */
    protected void dispatchMessage (ChatMessage msg)
    {
        for (Iterator iter = _displays.iterator(); iter.hasNext(); ) {
            ((ChatDisplay) iter.next()).displayMessage(msg);
        }
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
     * Do an internal check to see if we can distribute chat from the
     * specified user.
     */
    protected boolean isBlocked (String username)
    {
        return (_muter != null) && _muter.isMuted(username);
    }

    /** Our active chat context. */
    protected CrowdContext _ctx;

    /** The message manager. */
    protected MessageManager _msgmgr;

    /** The bundle to use for our own internal messages. */
    protected String _bundle;

    /** The place object that we currently occupy. */
    protected PlaceObject _place;

    /** A list of registered chat displays. */
    protected ArrayList _displays = new ArrayList();

    /** A list of registered chat validators. */
    protected ArrayList _validators = new ArrayList();

    /** A mapping from auxiliary chat objects to the types under which
     * they are registered. */
    protected HashIntMap _auxes = new HashIntMap();

    /** An optionally present mutelist director. */
    protected MuteDirector _muter;

    /** A cache of the target and message text associated with outstanding
     * tell chat requests. */
    protected HashIntMap _tells = new HashIntMap();

    /** Usernames of users we've recently chatted with. */
    protected LinkedList _chatters = new LinkedList();

    /** Observers that are watching our chatters list. */
    protected ArrayList _chatterObservers = new ArrayList();

    /** The maximum number of chatter usernames to track. */
    protected static final int MAX_CHATTERS = 6;
}
