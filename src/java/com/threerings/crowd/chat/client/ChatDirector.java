//
// $Id: ChatDirector.java,v 1.40 2002/11/13 01:29:41 ray Exp $

package com.threerings.crowd.chat;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;

import com.samskivert.util.HashIntMap;

import com.threerings.presents.client.*;
import com.threerings.presents.dobj.*;

import com.threerings.util.MessageBundle;
import com.threerings.util.MessageManager;
import com.threerings.util.TimeUtil;

import com.threerings.crowd.Log;
import com.threerings.crowd.client.LocationObserver;
import com.threerings.crowd.data.PlaceObject;
import com.threerings.crowd.util.CrowdContext;

/**
 * The chat director is the client side coordinator of all chat related
 * services. It handles both place constrained chat as well as direct
 * messaging.
 */
public class ChatDirector extends BasicDirector
    implements ChatCodes, LocationObserver, MessageListener, ChatReceiver
{
    /**
     * An interface that can receive information about the {@link
     * #MAX_CHATTERS} most recent users that we've been chatting with.
     */
    public static interface ChatterObserver
    {
        /**
         * Called when the list of chatters has been changed.
         */
        public void chattersUpdated (Iterator chatternames);
    }

    /**
     * A validator for adding usernames to the chatter list.
     */
    public static interface ChatterValidator
    {
        /**
         * Returns true if the username can be added to the chatters list.
         */
        public boolean isChatterValid (String username);
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

        // register for chat notifications
        _ctx.getClient().getInvocationDirector().registerReceiver(
            new ChatDecoder(this));

        // watch the session, clear displays when the user logs off.
        _ctx.getClient().addClientObserver(this);

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
        if (_displays.contains(display)) {
            Log.warning("Tried to add ChatDisplay more than once!");
            Log.logStackTrace(new Exception());
            return;
        }
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
     * Adds the specified chat validator to the list of validators.  All
     * chat requests will be validated with all validators before they may
     * be accepted.
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
    protected void addChatter (String name)
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
     * Notify ChatterObservers that the list of chatters has changed.
     */
    protected void notifyChatterObservers ()
    {
        for (int ii=0, nn=_chatterObservers.size(); ii < nn; ii++) {
            ChatterObserver co = (ChatterObserver) _chatterObservers.get(ii);
            co.chattersUpdated(_chatters.listIterator());
        }
    }

    /**
     * Requests that all chat displays clear their contents.
     */
    public void clearDisplays ()
    {
        for (int ii=0, nn=_displays.size(); ii < nn; ii++) {
            ((ChatDisplay) _displays.get(ii)).clear();
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
     * Displays a feedback message translated with the default bundle.
     */
    public void displayFeedbackMessage (String message)
    {
        displayFeedbackMessage(_bundle, message);
    }

    /**
     * Displays a feedback message.
     */
    public void displayFeedbackMessage (String bundle, String message)
    {
        displayFeedbackMessage(bundle, message, PLACE_CHAT_TYPE);
    }

    /**
     * Displays a feedback message.
     */
    public void displayFeedbackMessage (String bundle, String message,
        String localtype)
    {
        dispatchMessage(new FeedbackMessage(xlate(bundle, message), localtype));
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
        for (Iterator iter = _validators.iterator(); iter.hasNext(); ) {
            if (!((ChatValidator) iter.next()).validateSpeak(message)) {
                return;
            }
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
        _cservice.broadcast(
            _ctx.getClient(), message, new ChatService.InvocationListener () {
                public void requestFailed (String reason) {
                    displayFeedbackMessage(
                        _bundle, MessageBundle.compose(
                            "m.broadcast_failed", reason));
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
     */
    public void requestTell (final String target, final String message)
    {
        // make sure they can say what they want to say
        for (Iterator iter = _validators.iterator(); iter.hasNext(); ) {
            if (!((ChatValidator) iter.next()).validateTell(
                    target, message)) {
                return;
            }
        }

        // create a listener that will report success or failure
        ChatService.TellListener listener = new ChatService.TellListener() {
            public void tellSucceeded () {
                String msg = MessageBundle.tcompose(
                    "m.told_format", target, message);
                displayFeedbackMessage(_bundle, msg);
                addChatter(target);
            }

            public void tellSucceededIdle (long idletime) {
                String msg = MessageBundle.compose(
                    "m.told_idle_format", MessageBundle.taint(target),
                    MessageBundle.taint(message),
                    TimeUtil.getTimeOrderString(idletime, TimeUtil.MINUTE));

                displayFeedbackMessage(_bundle, msg);
                addChatter(target);
            }

            public void requestFailed (String reason) {
                String msg =
                    MessageBundle.compose("m.tell_failed", target, reason);
                displayFeedbackMessage(_bundle, msg);
            }
        };

        _cservice.tell(_ctx.getClient(), target, message, listener);
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
        String name = event.getName();
        if (name.equals(SPEAK_NOTIFICATION)) {
            handleSpeakMessage(
                getLocalType(event.getTargetOid()), event.getArgs());

        } else if (name.equals(SYSTEM_NOTIFICATION)) {
            handleSystemMessage(
                getLocalType(event.getTargetOid()), event.getArgs());
        }
    }

    // documentation inherited from interface
    public void receivedTell (String speaker, String bundle, String message)
    {
        // ignore messages from blocked users
        if (isBlocked(speaker)) {
            return;
        }

        // if the message need be translated, do so
        if (bundle != null) {
            message = xlate(bundle, message);
        }

        UserMessage um = new UserMessage(
            message, ChatCodes.TELL_CHAT_TYPE, speaker, ChatCodes.DEFAULT_MODE);
        dispatchMessage(um);
        addChatter(speaker);
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
        // bail if the speaker is blocked
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
     * Returns whether chat from the specified user is to be distributed.
     */
    protected boolean isBlocked (String username)
    {
        return (_muter != null) && _muter.isMuted(username);
    }

    /**
     * Used to assign unique ids to all speak requests.
     */
    protected synchronized int nextRequestId ()
    {
        return _requestId++;
    }

    // documentation inherited
    public void clientObjectDidChange (Client client)
    {
        super.clientObjectDidChange(client);

        clearDisplays();
    }

    // documentation inherited
    public void clientDidLogoff (Client client)
    {
        super.clientDidLogoff(client);

        clearDisplays();

        // clear out the list of people we've chatted with
        _chatters.clear();
        notifyChatterObservers();

        // clear the _place
        locationDidChange(null);

        // clear our service
        _cservice = null; 
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

    /** A list of registered chat displays. */
    protected ArrayList _displays = new ArrayList();

    /** A list of registered chat validators. */
    protected ArrayList _validators = new ArrayList();

    /** A mapping from auxiliary chat objects to the types under which
     * they are registered. */
    protected HashIntMap _auxes = new HashIntMap();

    /** An optionally present mutelist director. */
    protected MuteDirector _muter;

    /** Validator of who may be added to the chatters list. */
    protected ChatterValidator _chatterValidator;

    /** Usernames of users we've recently chatted with. */
    protected LinkedList _chatters = new LinkedList();

    /** Observers that are watching our chatters list. */
    protected ArrayList _chatterObservers = new ArrayList();

    /** Used by {@link #nextRequestId}. */
    protected int _requestId;

    /** The maximum number of chatter usernames to track. */
    protected static final int MAX_CHATTERS = 6;
}
