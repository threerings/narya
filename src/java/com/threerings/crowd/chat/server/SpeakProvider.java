//
// $Id: SpeakProvider.java,v 1.18 2003/12/09 21:01:43 mdb Exp $

package com.threerings.crowd.chat.server;

import java.util.ArrayList;
import java.util.HashMap;

import com.samskivert.util.ObserverList;

import com.threerings.presents.data.ClientObject;
import com.threerings.presents.dobj.DObject;
import com.threerings.presents.dobj.MessageEvent;
import com.threerings.presents.server.InvocationProvider;

import com.threerings.crowd.Log;
import com.threerings.crowd.data.BodyObject;
import com.threerings.crowd.server.CrowdServer;

import com.threerings.crowd.chat.data.ChatCodes;
import com.threerings.crowd.chat.data.ChatMessage;
import com.threerings.crowd.chat.data.SpeakObject;
import com.threerings.crowd.chat.data.SystemMessage;
import com.threerings.crowd.chat.data.UserMessage;

/**
 * Provides the back-end of the chat speaking facilities. A server entity
 * can make "speech" available among the subscribers of a particular
 * distributed object by constructing a speak provider and registering it
 * with the invocation manager, then placing the resulting marshaller into
 * the distributed object in question so that subscribers to that object
 * can use it to generate "speak" requests on that object.
 */
public class SpeakProvider
    implements InvocationProvider, ChatCodes
{
    /**
     * Used to prevent abitrary users from issuing speak requests.
     */
    public static interface SpeakerValidator
    {
        /**
         * Should return true if the supplied speaker is allowed to speak
         * via the speak provider with which this validator was
         * registered.
         */
        public boolean isValidSpeaker (DObject speakObj, ClientObject speaker);
    }

    /**
     * An interface used to notify external systems whenever a chat
     * message is spoken by one user and heard by another.
     */
    public static interface MessageObserver
    {
        /**
         * Called for each player that hears a particular chat message.
         */
        public void messageDelivered (String hearer, UserMessage message);
    }

    /**
     * Creates a speak provider that will provide speech on the supplied
     * distributed object.
     *
     * @param speakObj the object for which speech requests will be
     * processed.
     * @param validator an optional validator that can be used to prevent
     * arbitrary users from using the speech services on this object.
     */
    public SpeakProvider (DObject speakObj, SpeakerValidator validator)
    {
        _speakObj = speakObj;
        _validator = validator;
    }

    /**
     * Set the authorizer we will use to see if the user is allowed to
     * perform various speaking actions.
     */
    public static void setCommunicationAuthorizer (
        CommunicationAuthorizer comAuth)
    {
        _comAuth = comAuth;
    }

    /**
     * Registers a {@link MessageObserver} to be notified whenever a
     * user-originated chat message is heard by another user.
     */
    public static void registerMessageObserver (MessageObserver obs)
    {
        _messageObs.add(obs);
    }

    /**
     * Removes a registration made previously with {@link
     * #registerMessageObserver}.
     */
    public static void removeMessageObserver (MessageObserver obs)
    {
        _messageObs.remove(obs);
    }

    /**
     * Handles a {@link SpeakService#speak} request.
     */
    public void speak (ClientObject caller, String message, byte mode)
    {
        // make sure the caller is authorized to perform this action
        if ((_comAuth != null) && (!_comAuth.authorized(caller))) {
            return;
        }

        // ensure that the speaker is valid
        // TODO: broadcast should be handled more like a system message
        // rather than as a mode for a user message so that we don't
        // have to do this validation here. Or not.
        if ((mode == BROADCAST_MODE) ||
            !_validator.isValidSpeaker(_speakObj, caller)) {
            Log.warning("Refusing invalid speak request " +
                        "[caller=" + caller.who() +
                        ", speakObj=" + _speakObj.which() +
                        ", message=" + message + ", mode=" + mode + "].");

        } else {
            // issue the speak message on our speak object
            sendSpeak(_speakObj, ((BodyObject)caller).username,
                      null, message, mode);
        }
    }

    /**
     * Sends a speak notification to the specified place object
     * originating with the specified speaker (the speaker optionally
     * being a server entity that wishes to fake a "speak" message) and
     * with the supplied message content.
     *
     * @param speakObj the object on which to generate the speak message.
     * @param speaker the username of the user that generated the message
     * (or some special speaker name for server messages).
     * @param bundle null when the message originates from a real human,
     * the bundle identifier that will be used by the client to translate
     * the message text when the message originates from a server entity
     * "faking" a chat message.
     * @param message the text of the speak message.
     */
    public static void sendSpeak (DObject speakObj, String speaker,
                                  String bundle, String message)
    {
        sendSpeak(speakObj, speaker, bundle, message, ChatCodes.DEFAULT_MODE);
    }

    /**
     * Sends a speak notification to the specified place object
     * originating with the specified speaker (the speaker optionally
     * being a server entity that wishes to fake a "speak" message) and
     * with the supplied message content.
     *
     * @param speakObj the object on which to generate the speak message.
     * @param speaker the username of the user that generated the message
     * (or some special speaker name for server messages).
     * @param bundle null when the message originates from a real human,
     * the bundle identifier that will be used by the client to translate
     * the message text when the message originates from a server entity
     * "faking" a chat message.
     * @param message the text of the speak message.
     * @param mode the mode of the message, see {@link
     * ChatCodes#DEFAULT_MODE}.
     */
    public static void sendSpeak (DObject speakObj, String speaker,
                                  String bundle, String message, byte mode)
    {
        sendMessage(speakObj, new UserMessage(message, bundle, speaker, mode));
    }

    /**
     * Sends a system INFO message notification to the specified object with
     * the supplied message content. A system message is one that will be
     * rendered where the speak messages are rendered, but in a way that
     * makes it clear that it is a message from the server.
     *
     * Info messages are sent when something happens that was neither
     * directly triggered by the user, nor requires direct action.
     *
     * @param speakObj the object on which to deliver the message.
     * @param bundle the name of the localization bundle that should be
     * used to translate this system message prior to displaying it to the
     * client.
     * @param message the text of the message.
     */
    public static void sendInfo (
        DObject speakObj, String bundle, String message)
    {
        sendSystem(speakObj, bundle, message, SystemMessage.INFO);
    }

    /**
     * Sends a system FEEDBACK message notification to the specified
     * object with the supplied message content. A system message is one
     * that will be rendered where the speak messages are rendered,
     * but in a way that makes it clear that it is a message from the server.
     *
     * Feedback messages are sent in direct response to a user action,
     * usually to indicate success or failure of the user's action.
     *
     * @param speakObj the object on which to deliver the message.
     * @param bundle the name of the localization bundle that should be
     * used to translate this system message prior to displaying it to the
     * client.
     * @param message the text of the message.
     */
    public static void sendFeedback (
        DObject speakObj, String bundle, String message)
    {
        sendSystem(speakObj, bundle, message, SystemMessage.FEEDBACK);
    }

    /**
     * Sends a system ATTENTION message notification to the specified
     * object with the supplied message content. A system message is one
     * that will be rendered where the speak messages are rendered,
     * but in a way that makes it clear that it is a message from the server.
     *
     * Attention messages are sent when something requires user action
     * that did not result from direct action by the user.
     *
     * @param speakObj the object on which to deliver the message.
     * @param bundle the name of the localization bundle that should be
     * used to translate this system message prior to displaying it to the
     * client.
     * @param message the text of the message.
     */
    public static void sendAttention (
        DObject speakObj, String bundle, String message)
    {
        sendSystem(speakObj, bundle, message, SystemMessage.ATTENTION);
    }

    /**
     * Send the specified system message on the specified dobj.
     */
    protected static void sendSystem (
        DObject speakObj, String bundle, String message, byte attLevel)
    {
        sendMessage(speakObj, new SystemMessage(message, bundle, attLevel));
    }

    /**
     * Send the specified message on the specified object.
     */
    public static void sendMessage (DObject speakObj, ChatMessage msg)
    {
        if (speakObj == null) {
            Log.warning("Dropping speak message, no speak obj '" + msg + "'.");
            Thread.dumpStack();
            return;
        }

        // post the message to the relevant object
        speakObj.postMessage(CHAT_NOTIFICATION, new Object[] { msg });

        // if this is a user message; add it to the heard history of all
        // users that can "hear" it
        if (!(msg instanceof UserMessage)) {
            return;
        } else if (speakObj instanceof SpeakObject) {
            _messageMapper.message = (UserMessage)msg;
            ((SpeakObject)speakObj).applyToListeners(_messageMapper);
            _messageMapper.message = null;
        } else {
            Log.info("Unable to note listeners [dclass=" + speakObj.getClass() +
                     ", msg=" + msg + "].");
        }
    }

    /**
     * Returns a list of {@link ChatMessage} objects to which this user
     * has been privy in the recent past.
     */
    public static ArrayList getChatHistory (String username)
    {
        HistoryList history = getHistoryList(username);
        pruneHistory(System.currentTimeMillis(), history);
        return history;
    }

    /**
     * Called to clear the chat history for the specified user.
     */
    public static void clearHistory (String username)
    {
        // Log.info("Clearing history for " + username + ".");
        _histories.remove(username);
    }

    /**
     * Notes that the specified user was privy to the specified
     * message. If {@link ChatMessage#timestamp} is not already filled in,
     * it will be.
     */
    protected static void noteMessage (String username, UserMessage msg)
    {
        // fill in the message's time stamp if necessary
        if (msg.timestamp == 0L) {
            msg.timestamp = System.currentTimeMillis();
        }

        // add the message to this user's chat history
        HistoryList history = getHistoryList(username);
        history.add(msg);

        // if the history is big enough, potentially prune it (we always
        // prune when asked for the history, so this is just to balance
        // memory usage with CPU expense)
        if (history.size() > 15) {
            pruneHistory(msg.timestamp, history);
        }
        // Log.info("Noted that " + username + " heard " + msg + ".");

        // notify any message observers
        _messageOp.init(username, msg);
        _messageObs.apply(_messageOp);
    }

    /**
     * Returns this user's chat history, creating one if necessary.
     */
    protected static HistoryList getHistoryList (String username)
    {
        HistoryList history = (HistoryList)_histories.get(username);
        if (history == null) {
            _histories.put(username, history = new HistoryList());
        }
        return history;
    }

    /**
     * Prunes all messages from this history which are expired.
     */
    protected static void pruneHistory (long now, HistoryList history)
    {
        int prunepos = 0;
        for (int ll = history.size(); prunepos < ll; prunepos++) {
            ChatMessage msg = (ChatMessage)history.get(prunepos);
            if (now - msg.timestamp < HISTORY_EXPIRATION) {
                break; // stop when we get to the first valid message
            }
        }
        history.remove(0, prunepos);
    }

    /** Used to note the recipients of a chat message. */
    protected static class MessageMapper implements SpeakObject.ListenerOp
    {
        public UserMessage message;

        public void apply (int bodyOid) {
            DObject dobj = CrowdServer.omgr.getObject(bodyOid);
            if (dobj != null && dobj instanceof BodyObject) {
                noteMessage(((BodyObject)dobj).username, message);
            }
        }

        public void apply (String username) {
            noteMessage(username, message);
        }
    }

    /** Extends {@link ArrayList} for the sole purpose of exposing {@link
     * ArrayList#removeRange}. Yay! */
    protected static class HistoryList extends ArrayList
    {
        public void remove (int fromIndex, int toIndex) {
            removeRange(fromIndex, toIndex);
        }
    }

    /** Used to notify our {@link MessageObserver}s. */
    protected static class MessageObserverOp implements ObserverList.ObserverOp
    {
        public void init (String hearer, UserMessage message) {
            _hearer = hearer;
            _message = message;
        }

        public boolean apply (Object observer) {
            ((MessageObserver)observer).messageDelivered(_hearer, _message);
            return true;
        }

        protected String _hearer;
        protected UserMessage _message;
    }

    /** Our speech object. */
    protected DObject _speakObj;

    /** The entity that will validate our speakers. */
    protected SpeakerValidator _validator;

    /** The entity that will authorize our speakers. */
    protected static CommunicationAuthorizer _comAuth;
    
    /** Recent chat history for the server. */
    protected static HashMap _histories = new HashMap();

    /** Used to note the recipients of a chat message. */
    protected static MessageMapper _messageMapper = new MessageMapper();

    /** A list of {@link MessageObserver}s. */
    protected static ObserverList _messageObs = new ObserverList(
        ObserverList.FAST_UNSAFE_NOTIFY);

    /** Used to notify our {@link MessageObserver}s. */
    protected static MessageObserverOp _messageOp = new MessageObserverOp();

    /** The amount of time before chat history becomes... history. */
    protected static final long HISTORY_EXPIRATION = 5L * 60L * 1000L;
}
