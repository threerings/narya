//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2025 Three Rings Design, Inc., All Rights Reserved
// https://github.com/threerings/narya/blob/master/LICENSE

package com.threerings.crowd.chat.client;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import com.samskivert.util.HashIntMap;
import com.samskivert.util.ObserverList;
import com.samskivert.util.ResultListener;
import com.samskivert.util.StringUtil;

import com.threerings.util.MessageBundle;
import com.threerings.util.Name;
import com.threerings.util.TimeUtil;

import com.threerings.presents.client.BasicDirector;
import com.threerings.presents.client.Client;
import com.threerings.presents.data.ClientObject;
import com.threerings.presents.dobj.DObject;
import com.threerings.presents.dobj.MessageEvent;
import com.threerings.presents.dobj.MessageListener;

import com.threerings.crowd.chat.data.ChatCodes;
import com.threerings.crowd.chat.data.ChatMessage;
import com.threerings.crowd.chat.data.SystemMessage;
import com.threerings.crowd.chat.data.TellFeedbackMessage;
import com.threerings.crowd.chat.data.UserMessage;
import com.threerings.crowd.chat.data.UserSystemMessage;
import com.threerings.crowd.client.LocationObserver;
import com.threerings.crowd.data.BodyObject;
import com.threerings.crowd.data.CrowdCodes;
import com.threerings.crowd.data.PlaceObject;
import com.threerings.crowd.util.CrowdContext;

import static com.threerings.crowd.Log.log;

/**
 * The chat director is the client side coordinator of all chat related services. It handles both
 * place constrained chat as well as direct messaging.
 */
public class ChatDirector extends BasicDirector
    implements ChatCodes, LocationObserver, MessageListener
{
    /**
     * An interface to receive information about the {@link #MAX_CHATTERS} most recent users that
     * we've been chatting with.
     */
    public static interface ChatterObserver
    {
        /**
         * Called when the list of chatters has been changed.
         */
        void chattersUpdated (Iterator<Name> chatternames);
    }

    /**
     * An interface for those who would like to validate whether usernames may be added to the
     * chatter list.
     */
    public static interface ChatterValidator
    {
        /**
         * Returns whether the username may be added to the chatters list.
         */
        boolean isChatterValid (Name username);
    }

    /**
     * Used to implement a slash command (e.g. <code>/who</code>).
     */
    public abstract static class CommandHandler
    {
        /**
         * Returns the translatable usage message for the specified command.
         */
        public String getUsage (String command)
        {
            return MessageBundle.tcompose(_usageKey,
                _showAliasesInUsage ? Joiner.on('|').join(_aliases) : command);
        }

        /**
         * Handles the specified chat command.
         *
         * @param speakSvc an optional SpeakService object representing the object to send the chat
         * message on.
         * @param command the slash command that was used to invoke this handler
         * (e.g. <code>/tell</code>).
         * @param args the arguments provided along with the command (e.g. <code>Bob hello</code>)
         * or <code>null</code> if no arguments were supplied.
         * @param history an in/out parameter that allows the command to modify the text that will
         * be appended to the chat history. If this is set to null, nothing will be appended.
         *
         * @return an untranslated string that will be reported to the chat box to convey an error
         * response to the user, or {@link ChatCodes#SUCCESS}.
         */
        public abstract String handleCommand (SpeakService speakSvc, String command, String args,
                                              String[] history);

        /**
         * Returns true if this user should have access to this chat command.
         */
        public boolean checkAccess (BodyObject user) {
            return true;
        }

        /** The command's usage message translation key. */
        protected String _usageKey;

        /** The command's translated aliases. */
        protected String[] _aliases;
    }

    /**
     * Creates a chat director and initializes it with the supplied context. The chat director will
     * register itself as a location observer so that it can automatically process place
     * constrained chat.
     *
     * @param bundle the message bundle from which we obtain our chat-related translation strings.
     */
    public ChatDirector (CrowdContext ctx, String bundle)
    {
        super(ctx);

        // keep the context around
        _ctx = ctx;
        _bundle = bundle;

        // register ourselves as a location observer
        _ctx.getLocationDirector().addLocationObserver(this);

        // register our default chat handlers
        if (_bundle == null || _ctx.getMessageManager() == null) {
            log.warning("Null bundle or message manager given to ChatDirector");
            return;
        }
        registerCommandHandlers();
    }

    /**
     * Adds the supplied chat display to the front of the chat display list. It will subsequently
     * be notified of incoming chat messages as well as tell responses.
     */
    public void pushChatDisplay (ChatDisplay display)
    {
        _displays.add(0, display);
    }

    /**
     * Adds the supplied chat display to the end of the chat display list. It will subsequently be
     * notified of incoming chat messages as well as tell responses.
     */
    public boolean addChatDisplay (ChatDisplay display)
    {
        return _displays.add(display);
    }

    /**
     * Removes the specified chat display from the chat display list. The display will no longer
     * receive chat related notifications.
     */
    public boolean removeChatDisplay (ChatDisplay display)
    {
        return _displays.remove(display);
    }

    /**
     * Adds the specified chat filter to the list of filters. All chat requests and receipts will
     * be filtered with all filters before they being sent or dispatched locally.
     */
    public boolean addChatFilter (ChatFilter filter)
    {
        return _filters.add(filter);
    }

    /**
     * Removes the specified chat filter from the list of chat filter.
     */
    public boolean removeChatFilter (ChatFilter filter)
    {
        return _filters.remove(filter);
    }

    /**
     * Adds an observer that watches the chatters list, and updates it immediately.
     */
    public boolean addChatterObserver (ChatterObserver co)
    {
        boolean added = _chatterObservers.add(co);
        co.chattersUpdated(_chatters.listIterator());

        return added;
    }

    /**
     * Removes an observer from the list of chatter observers.
     */
    public boolean removeChatterObserver (ChatterObserver co)
    {
        return _chatterObservers.remove(co);
    }

    /**
     * Sets the validator that decides if a username is valid to be added to the chatter list, or
     * null if no such filtering is desired.
     */
    public void setChatterValidator (ChatterValidator validator)
    {
        _chatterValidator = validator;
    }

    /**
     * Enables or disables the chat mogrifier. The mogrifier converts chat speak like LOL, WTF,
     * etc. into phrases, words and can also transform them into emotes. The mogrifier is
     * configured via the <code>x.mogrifies</code> and <code>x.transforms</code> translation
     * properties.
     */
    public void setMogrifyChat (boolean mogrifyChat)
    {
        _mogrifyChat = mogrifyChat;
    }

    /**
     * Registers a chat command handler.
     *
     * @param msg the message bundle via which the slash command will be translated (as
     * <code>c.</code><i>command</i>). If no translation exists the command will be
     * <code>/</code><i>command</i>.
     * @param command the name of the command that will be used to invoke this handler (e.g.
     * <code>tell</code> if the command will be invoked as <code>/tell</code>).
     * @param handler the chat command handler itself.
     */
    public void registerCommandHandler (MessageBundle msg, String command, CommandHandler handler)
    {
        // the usage key is derived from the untranslated command
        handler._usageKey = "m.usage_" + command;

        String key = "c." + command;
        handler._aliases = msg.exists(key) ? msg.get(key).split("\\s+") : new String[] { command };
        for (String alias : handler._aliases) {
            _handlers.put(alias, handler);
        }
    }

    /**
     * Return the current size of the history.
     */
    public int getCommandHistorySize ()
    {
        return _history.size();
    }

    /**
     * Get the chat history entry at the specified index, with 0 being the oldest.
     */
    public String getCommandHistory (int index)
    {
        return _history.get(index);
    }

    /**
     * Clear the chat command history.
     */
    public void clearCommandHistory ()
    {
        _history.clear();
    }

    /**
     * Requests that all chat displays clear their contents.
     */
    public void clearDisplays ()
    {
        _displays.apply(new ObserverList.ObserverOp<ChatDisplay>() {
            public boolean apply (ChatDisplay observer) {
                observer.clear();
                return true;
            }
        });
    }

    /**
     * Display a system INFO message as if it had come from the server. The localtype of the
     * message will be PLACE_CHAT_TYPE.
     *
     * Info messages are sent when something happens that was neither directly triggered by the
     * user, nor requires direct action.
     */
    public void displayInfo (String bundle, String message)
    {
        displaySystem(bundle, message, SystemMessage.INFO, PLACE_CHAT_TYPE);
    }

    /**
     * Display a system INFO message as if it had come from the server.
     *
     * Info messages are sent when something happens that was neither directly triggered by the
     * user, nor requires direct action.
     */
    public void displayInfo (String bundle, String message, String localtype)
    {
        displaySystem(bundle, message, SystemMessage.INFO, localtype);
    }

    /**
     * Display a system FEEDBACK message as if it had come from the server. The localtype of the
     * message will be PLACE_CHAT_TYPE.
     *
     * Feedback messages are sent in direct response to a user action, usually to indicate success
     * or failure of the user's action.
     */
    public void displayFeedback (String bundle, String message)
    {
        displaySystem(bundle, message, SystemMessage.FEEDBACK, PLACE_CHAT_TYPE);
    }

    /**
     * Display a system ATTENTION message as if it had come from the server. The localtype of the
     * message will be PLACE_CHAT_TYPE.
     *
     * Attention messages are sent when something requires user action that did not result from
     * direct action by the user.
     */
    public void displayAttention (String bundle, String message)
    {
        displaySystem(bundle, message, SystemMessage.ATTENTION, PLACE_CHAT_TYPE);
    }

    /**
     * Dispatches the provided message to our chat displays.
     */
    public void dispatchMessage (ChatMessage message, String localType)
    {
        setClientInfo(message, localType);
        dispatchPreparedMessage(message);
    }

    /**
     * Parses and delivers the supplied chat message. Slash command processing and mogrification
     * are performed and the message is added to the chat history if appropriate.
     *
     * @param speakSvc the SpeakService representing the target dobj of the speak or null if we
     * should speak in the "default" way.
     * @param text the text to be parsed and sent.
     * @param record if text is a command, should it be added to the history?
     *
     * @return <code>ChatCodes#SUCCESS</code> if the message was parsed and sent correctly, a
     * translatable error string if there was some problem.
     */
    public String requestChat (SpeakService speakSvc, String text, boolean record)
    {
        if (text.startsWith("/")) {
            // split the text up into a command and arguments
            String command = text.substring(1).toLowerCase();
            String[] hist = new String[1];
            String args = "";
            int sidx = text.indexOf(" ");
            if (sidx != -1) {
                command = text.substring(1, sidx).toLowerCase();
                args = text.substring(sidx + 1).trim();
            }

            Map<String, CommandHandler> possibleCommands = getCommandHandlers(command);
            switch (possibleCommands.size()) {
            case 0:
                StringTokenizer tok = new StringTokenizer(text);
                return MessageBundle.tcompose("m.unknown_command", tok.nextToken());

            case 1:
                Map.Entry<String, CommandHandler> entry =
                    possibleCommands.entrySet().iterator().next();
                String cmdName = entry.getKey();
                CommandHandler cmd = entry.getValue();

                String result = cmd.handleCommand(speakSvc, cmdName, args, hist);
                if (!result.equals(ChatCodes.SUCCESS)) {
                    return result;
                }

                if (record) {
                    // get the final history-ready command string
                    hist[0] = "/" + ((hist[0] == null) ? command : hist[0]);

                    // remove from history if it was present and add it to the end
                    addToHistory(hist[0]);
                }

                return result;

            default:
                StringBuilder buf = new StringBuilder();
                for (String pcmd : Sets.newTreeSet(possibleCommands.keySet())) {
                    buf.append(" /").append(pcmd);
                }
                return MessageBundle.tcompose("m.unspecific_command", buf.toString());
            }
        }

        // if not a command then just speak
        String message = text.trim();
        if (StringUtil.isBlank(message)) {
            // report silent failure for now
            return ChatCodes.SUCCESS;
        }

        return deliverChat(speakSvc, message, ChatCodes.DEFAULT_MODE);
    }

    /**
     * Requests that a speak message with the specified mode be generated and delivered via the
     * supplied speak service instance (which will be associated with a particular "speak
     * object"). The message will first be validated by all registered {@link ChatFilter}s (and
     * possibly vetoed) before being dispatched.
     *
     * @param speakService the speak service to use when generating the speak request or null if we
     * should speak in the current "place".
     * @param message the contents of the speak message.
     * @param mode a speech mode that will be interpreted by the {@link ChatDisplay}
     * implementations that eventually display this speak message.
     */
    public void requestSpeak (SpeakService speakService, String message, byte mode)
    {
        if (speakService == null) {
            if (_place == null) {
                return;
            }
            speakService = _place.speakService;
        }

        // make sure they can say what they want to say
        message = filter(message, null, true);
        if (message == null) {
            return;
        }

        // dispatch a speak request using the supplied speak service
        speakService.speak(message, mode);
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
            displayFeedback(_bundle, MessageBundle.compose("m.broadcast_failed", "m.filtered"));
            return;
        }
        _cservice.broadcast(message, new ChatService.InvocationListener() {
            public void requestFailed (String reason) {
                reason = MessageBundle.compose("m.broadcast_failed", reason);
                displayFeedback(_bundle, reason);
            }
        });
    }

    /**
     * Requests that a tell message be delivered to the specified target user.
     *
     * @param target the username of the user to which the tell message should be delivered.
     * @param msg the contents of the tell message.
     * @param rl an optional result listener if you'd like to be notified of success or failure.
     */
    public <T extends Name> void requestTell (
        final T target, String msg, final ResultListener<T> rl)
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
                success();

                // if they have an away message, report that
                if (awayMessage != null) {
                    awayMessage = filter(awayMessage, target, false);
                    if (awayMessage != null) {
                        String msg = MessageBundle.tcompose("m.recipient_afk", target, awayMessage);
                        displayFeedback(_bundle, msg);
                    }
                }

                // if they are idle, report that
                if (idletime > 0L) {
                    // adjust by the time it took them to become idle
                    idletime += _ctx.getConfig().getValue(IDLE_TIME_KEY, DEFAULT_IDLE_TIME);
                    String msg = MessageBundle.compose(
                        "m.recipient_idle", MessageBundle.taint(target),
                        TimeUtil.getTimeOrderString(idletime, TimeUtil.MINUTE));
                    displayFeedback(_bundle, msg);
                }
            }

            protected void success () {
                dispatchMessage(new TellFeedbackMessage(target, message, false),
                                ChatCodes.PLACE_CHAT_TYPE);
                addChatter(target);
                if (rl != null) {
                    rl.requestCompleted(target);
                }
            }

            public void requestFailed (String reason) {
                String msg = MessageBundle.compose(
                    "m.tell_failed", MessageBundle.taint(target), reason);
                TellFeedbackMessage tfm = new TellFeedbackMessage(target, msg, true);
                tfm.bundle = _bundle;
                dispatchMessage(tfm, ChatCodes.PLACE_CHAT_TYPE);
                if (rl != null) {
                    rl.requestFailed(null);
                }
            }
        };

        _cservice.tell(target, message, listener);
    }

    /**
     * Configures a message that will be automatically reported to anyone that sends a tell message
     * to this client to indicate that we are busy or away from the keyboard.
     */
    public void setAwayMessage (String message)
    {
        if (message != null) {
            message = filter(message, null, true);
            if (message == null) {
                // they filtered away their own away message... change it to something
                message = "...";
            }
        }
        // pass the buck right on along
        _cservice.away(message);
    }

    /**
     * Adds an additional object via which chat messages may arrive. The chat director assumes the
     * caller will be managing the subscription to this object and will remain subscribed to it for
     * as long as it remains in effect as an auxiliary chat source.
     *
     * @param localtype a type to be associated with all chat messages that arrive on the specified
     * DObject.
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

    /**
     * Returns the history list containing a trailing window of messages that have passed through
     * this chat director. The history list is created on demand so that systems which don't make
     * use of chat history need not incur the overhead of tracking historical chat messages. Thus
     * messages will only be added to the history <em>after</em> this method has been called at
     * least once.
     */
    public HistoryList getHistory ()
    {
        if (_hlist == null) {
            addChatDisplay(_hlist = new HistoryList());
        }
        return _hlist;
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
     * Runs the supplied message through the various chat mogrifications.
     */
    public String mogrifyChat (String text)
    {
        return mogrifyChat(text, (byte)-1, false, true);
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
            ChatMessage msg = (ChatMessage)event.getArgs()[0];
            String localtype = getLocalType(event.getTargetOid());
            processReceivedMessage(msg, localtype);
        }
    }

    @Override
    public void clientDidLogon (Client client)
    {
        super.clientDidLogon(client);

        // listen on the client object for tells
        addAuxiliarySource(_clobj = client.getClientObject(), USER_CHAT_TYPE);
    }

    @Override
    public void clientObjectDidChange (Client client)
    {
        super.clientObjectDidChange(client);

        // change what we're listening to for tells
        removeAuxiliarySource(_clobj);
        addAuxiliarySource(_clobj = client.getClientObject(), USER_CHAT_TYPE);

        clearDisplays();
    }

    @Override
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
     * Registers all the chat-command handlers.
     */
    protected void registerCommandHandlers ()
    {
        MessageBundle msg = _ctx.getMessageManager().getBundle(_bundle);
        registerCommandHandler(msg, "help", new HelpHandler());
        registerCommandHandler(msg, "clear", new ClearHandler());
        registerCommandHandler(msg, "speak", new SpeakHandler());
        registerCommandHandler(msg, "emote", new EmoteHandler());
        registerCommandHandler(msg, "think", new ThinkHandler());
        registerCommandHandler(msg, "tell", new TellHandler());
        registerCommandHandler(msg, "broadcast", new BroadcastHandler());
    }

    /**
     * Processes and dispatches the specified chat message.
     */
    protected void processReceivedMessage (ChatMessage msg, String localtype)
    {
        String autoResponse = null;
        Name speaker = null;
        Name speakerDisplay = null;
        byte mode = (byte)-1;

        // figure out if the message was triggered by another user
        if (msg instanceof UserMessage) {
            UserMessage umsg = (UserMessage)msg;
            speaker = umsg.speaker;
            speakerDisplay = umsg.getSpeakerDisplayName();
            mode = umsg.mode;

        } else if (msg instanceof UserSystemMessage) {
            speaker = ((UserSystemMessage)msg).speaker;
            speakerDisplay = speaker;
        }

        // Translate and timestamp the message. This would happen during dispatch but we
        // need to do it ahead of filtering.
        setClientInfo(msg, localtype);

        // if there was an originating speaker, see if we want to hear it
        if (speaker != null) {
            if (shouldFilter(msg) && (msg.message = filter(msg.message, speaker, false)) == null) {
                return;
            }

            if (USER_CHAT_TYPE.equals(localtype) &&
                mode == ChatCodes.DEFAULT_MODE) {
                // if it was a tell, add the speaker as a chatter
                addChatter(speaker);

                // note whether or not we have an auto-response
                BodyObject self = (BodyObject)_ctx.getClient().getClientObject();
                if (!StringUtil.isBlank(self.awayMessage)) {
                    autoResponse = self.awayMessage;
                }
            }
        }

        // and send it off!
        dispatchMessage(msg, localtype);

        // if we auto-responded, report as much
        if (autoResponse != null) {
            String amsg = MessageBundle.tcompose(
                "m.auto_responded", speakerDisplay, autoResponse);
            displayFeedback(_bundle, amsg);
        }
    }

    /**
     * Checks whether we should filter the supplied incoming message.
     */
    protected boolean shouldFilter (ChatMessage msg)
    {
        return true;
    }

    /**
     * Dispatch a message to chat displays once it is fully prepared with the clientinfo.
     */
    protected void dispatchPreparedMessage (ChatMessage message)
    {
        _displayMessageOp.setMessage(message);
        _displays.apply(_displayMessageOp);
    }

    /**
     * Called to determine whether we are permitted to post the supplied chat message. Derived
     * classes may wish to throttle chat or restrict certain types in certain circumstances for
     * whatever reason.
     *
     * @return null if the chat is permitted, SUCCESS if the chat is permitted and has already been
     * dealt with, or a translatable string indicating the reason for rejection if not.
     */
    protected String checkCanChat (SpeakService speakSvc, String message, byte mode)
    {
        return null;
    }

    /**
     * Delivers a plain chat message (not a slash command) on the specified speak service in the
     * specified mode. The message will be mogrified and filtered prior to delivery.
     *
     * @return {@link ChatCodes#SUCCESS} if the message was delivered or a string indicating why it
     * failed.
     */
    protected String deliverChat (SpeakService speakSvc, String message, byte mode)
    {
        // run the message through our mogrification process
        message = mogrifyChat(message, mode, true, mode != ChatCodes.EMOTE_MODE);

        // mogrification may result in something being turned into a slash command, in which case
        // we have to run everything through again from the start
        if (message.startsWith("/")) {
            return requestChat(speakSvc, message, false);
        }

        // make sure this client is not restricted from performing this chat message for some
        // reason or other
        String errmsg = checkCanChat(speakSvc, message, mode);
        if (errmsg != null) {
            return errmsg;
        }

        // speak on the specified service
        requestSpeak(speakSvc, message, mode);

        return ChatCodes.SUCCESS;
    }

    /**
     * Adds the specified command to the history.
     */
    protected void addToHistory (String cmd)
    {
        // remove any previous instance of this command
        _history.remove(cmd);

        // append it to the end
        _history.add(cmd);

        // prune the history once it extends beyond max size
        if (_history.size() > MAX_COMMAND_HISTORY) {
            _history.remove(0);
        }
    }

    /**
     * Mogrifies common literary crutches into more appealing chat or commands.
     *
     * @param mode the chat mode, or -1 if unknown.
     * @param transformsAllowed if true, the chat may transformed into a different mode. (lol ->
     * /emote laughs)
     * @param capFirst if true, the first letter of the text is capitalized. This is not desired if
     * the chat is already an emote.
     */
    protected String mogrifyChat (
        String text, byte mode, boolean transformsAllowed, boolean capFirst)
    {
        int tlen = text.length();
        if (tlen == 0) {
            return text;

        // check to make sure there aren't too many caps
        } else if (tlen > 7 && suppressTooManyCaps()) {
            // count caps
            int caps = 0;
            for (int ii=0; ii < tlen; ii++) {
                if (Character.isUpperCase(text.charAt(ii))) {
                    caps++;
                    if (caps > (tlen / 2)) {
                        // lowercase the whole string if there are
                        text = text.toLowerCase();
                        break;
                    }
                }
            }
        }

        StringBuffer buf = new StringBuffer(text);
        buf = mogrifyChat(buf, transformsAllowed, capFirst);
        return buf.toString();
    }

    /** Helper function for {@link #mogrifyChat(String,byte,boolean,boolean)}. */
    protected StringBuffer mogrifyChat (
        StringBuffer buf, boolean transformsAllowed, boolean capFirst)
    {
        if (_mogrifyChat) {
            // do the generic mogrifications and translations
            buf = translatedReplacements("x.mogrifies", buf);

            // perform themed expansions and transformations
            if (transformsAllowed) {
                buf = translatedReplacements("x.transforms", buf);
            }
        }

        /*
        // capitalize the first letter
        if (capFirst) {
            buf.setCharAt(0, Character.toUpperCase(buf.charAt(0)));
        }
        // and capitalize any letters after a sentence-ending punctuation
        Pattern p = Pattern.compile("([^\\.][\\.\\?\\!](\\s)+\\p{Ll})");
        Matcher m = p.matcher(buf);
        if (m.find()) {
            buf = new StringBuilder();
            m.appendReplacement(buf, m.group().toUpperCase());
            while (m.find()) {
                m.appendReplacement(buf, m.group().toUpperCase());
            }
            m.appendTail(buf);
        }
        */

        return buf;
    }

    /**
     * Do all the replacements (mogrifications) specified in the translation string specified by
     * the key.
     */
    protected StringBuffer translatedReplacements (String key, StringBuffer buf)
    {
        MessageBundle bundle = _ctx.getMessageManager().getBundle(_bundle);
        if (!bundle.exists(key)) {
            return buf;
        }
        StringTokenizer st = new StringTokenizer(bundle.get(key), "#");
        // apply the replacements to each mogrification that matches
        while (st.hasMoreTokens()) {
            String pattern = st.nextToken();
            String replace = st.nextToken();
            Matcher m = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE).matcher(buf);
            if (m.find()) {
                buf = new StringBuffer();
                m.appendReplacement(buf, replace);
                // they may match more than once
                while (m.find()) {
                    m.appendReplacement(buf, replace);
                }
                m.appendTail(buf);
            }
        }
        return buf;
    }

    /**
     * Return true if we should lowercase messages containing more than half upper-case characters.
     */
    protected boolean suppressTooManyCaps ()
    {
        return true;
    }

    /**
     * Check that after mogrification the message is not too long.
     * @return an error message if it is too long, or null.
     */
    protected String checkLength (String msg)
    {
        return null; // everything's ok by default
    }

    /**
     * Returns a map containing all command handlers that match the specified command (i.e. the
     * specified command is a prefix of their registered command string). If there's an exact
     * match, only that match is returned, even if the command is a prefix of other handlers.
     */
    protected Map<String, CommandHandler> getCommandHandlers (String command)
    {
        HashMap<String, CommandHandler> matches = Maps.newHashMap();
        BodyObject user = (BodyObject)_ctx.getClient().getClientObject();
        for (Map.Entry<String, CommandHandler> entry : _handlers.entrySet()) {
            if (!isCommandPrefix(command, entry.getKey(), entry.getValue(), user)) {
                continue;
            }
            if (entry.getKey().equals(command)) {// If we hit an exact match, it wins
                matches.clear();
                matches.put(entry.getKey(), entry.getValue());
                return matches;
            }
            // if we're providing the full list and we show aliases in the usage text,
            // only map to the first alias
            String key = (_showAliasesInUsage && command.equals("")) ?
                entry.getValue()._aliases[0] : entry.getKey();
            matches.put(key, entry.getValue());
        }
        return matches;
    }

    /**
     * Returns true if <code>enteredCommand</code> equals or is a prefix of
     * <code>handlerCommand</code> and if the handler is available for <code>user</code>.
     */
    protected boolean isCommandPrefix (String enteredCommand, String handlerCommand,
        CommandHandler handler, BodyObject user)
    {
        return handlerCommand.startsWith(enteredCommand) && handler.checkAccess(user);
    }

    /**
     * Adds a chatter to our list of recent chatters.
     */
    protected void addChatter (Name name)
    {
        // check to see if the chatter validator approves..
        if ((_chatterValidator != null) && (!_chatterValidator.isChatterValid(name))) {
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
     * Notifies all registered {@link ChatterObserver}s that the list of chatters has changed.
     */
    protected void notifyChatterObservers ()
    {
        _chatterObservers.apply(new ObserverList.ObserverOp<ChatterObserver>() {
            public boolean apply (ChatterObserver observer) {
                observer.chattersUpdated(_chatters.listIterator());
                return true;
            }
        });
    }

    /**
     * Set the "client info" on the specified message, if not already set.
     */
    protected void setClientInfo (ChatMessage msg, String localType)
    {
        if (msg.localtype == null) {
            msg.setClientInfo(xlate(msg.bundle, msg.message), localType);
        }
    }

    /**
     * Translates the specified message using the specified bundle.
     */
    protected String xlate (String bundle, String message)
    {
        if (bundle != null && _ctx.getMessageManager() != null) {
            MessageBundle msgb = _ctx.getMessageManager().getBundle(bundle);
            if (msgb == null) {
                log.warning("No message bundle available to translate message", "bundle", bundle,
                            "message", message);
            } else {
                message = msgb.xlate(message);
            }
        }
        return message;
    }

    /**
     * Display the specified system message as if it had come from the server.
     */
    protected void displaySystem (String bundle, String message, byte attLevel, String localtype)
    {
        // nothing should be untranslated, so pass the default bundle if need be.
        if (bundle == null) {
            bundle = _bundle;
        }
        SystemMessage msg = new SystemMessage(message, bundle, attLevel);
        dispatchMessage(msg, localtype);
    }

    /**
     * Looks up and returns the message type associated with the specified oid.
     */
    protected String getLocalType (int oid)
    {
        String type = _auxes.get(oid);
        return (type == null) ? PLACE_CHAT_TYPE : type;
    }

    @Override
    protected void registerServices (Client client)
    {
        client.addServiceGroup(CrowdCodes.CROWD_GROUP);
    }

    @Override
    protected void fetchServices (Client client)
    {
        // get a handle on our chat service
        _cservice = client.requireService(ChatService.class);
    }

    /**
     * An operation that checks with all chat filters to properly filter a message prior to sending
     * to the server or displaying.
     */
    protected static class FilterMessageOp
        implements ObserverList.ObserverOp<ChatFilter>
    {
        public void setMessage (String msg, Name otherUser, boolean outgoing)
        {
            _msg = msg;
            _otherUser = otherUser;
            _out = outgoing;
        }

        public boolean apply (ChatFilter observer)
        {
            if (_msg != null) {
                _msg = observer.filter(_msg, _otherUser, _out);
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
    protected static class DisplayMessageOp
        implements ObserverList.ObserverOp<ChatDisplay>
    {
        public void setMessage (ChatMessage message)
        {
            _message = message;
            _displayed = false;
        }

        public boolean apply (ChatDisplay observer)
        {
            if (observer.displayMessage(_message, _displayed)) {
                _displayed = true;
            }
            return true;
        }

        protected ChatMessage _message;
        protected boolean _displayed;
    }

    /** Implements <code>/help</code>. */
    protected class HelpHandler extends CommandHandler
    {
        @Override
        public String getUsage (String command)
        {
            Map<String, CommandHandler> possibleCommands = getCommandHandlers("");
            possibleCommands.remove(command);
            return getUsage(command, possibleCommands);
        }

        @Override
        public String handleCommand (SpeakService speakSvc, String command,
                                     String args, String[] history)
        {
            // put "/help " in the history
            history[0] = command + " ";

            String hcmd = "";

            // grab the command they want help on
            if (!StringUtil.isBlank(args)) {
                hcmd = args;
                int sidx = args.indexOf(" ");
                if (sidx != -1) {
                    hcmd = args.substring(0, sidx);
                }
            }

            // let the user give commands with or with the /
            if (hcmd.startsWith("/")) {
                hcmd = hcmd.substring(1);
            }

            // handle "/help help" and "/help someboguscommand"
            Map<String, CommandHandler> possibleCommands = getCommandHandlers(hcmd);
            String usage;
            if (_handlers.get(hcmd) == this || possibleCommands.isEmpty()) {
                usage = getUsage(command);

            } else if (possibleCommands.size() > 1) {
                usage = getUsage(command, possibleCommands);

            } else {
                // if there is only one possible command display its usage
                Map.Entry<String, CommandHandler> entry =
                    possibleCommands.entrySet().iterator().next();
                usage = entry.getValue().getUsage(entry.getKey());
            }

            // this is a little funny, but we display the feedback message by hand and return
            // SUCCESS so that the chat entry field doesn't think that we've failed and
            // preserve our command text
            displayFeedback(null, usage);
            return ChatCodes.SUCCESS;
        }

        /**
         * Returns a usage message listing the provided commands.
         */
        protected String getUsage (String command, Map<String, CommandHandler> possibleCommands)
        {
            Object[] commands = possibleCommands.keySet().toArray();
            Arrays.sort(commands);
            String commandList = "";
            for (Object element : commands) {
                commandList += " /" + element;
            }
            return MessageBundle.tcompose("m.usage_help", commandList, command);
        }
    }

    /** Implements <code>/clear</code>. */
    protected class ClearHandler extends CommandHandler
    {
        @Override
        public String handleCommand (SpeakService speakSvc, String command,
                                     String args, String[] history)
        {
            clearDisplays();
            return ChatCodes.SUCCESS;
        }
    }

    /** Implements <code>/speak</code>. */
    protected class SpeakHandler extends CommandHandler
    {
        @Override
        public String handleCommand (SpeakService speakSvc, String command,
                                     String args, String[] history)
        {
            if (StringUtil.isBlank(args)) {
                return getUsage(command);
            }
            // note the command to be stored in the history
            history[0] = command + " ";
            // we do not propogate the speakSvc, because /speak means use
            // the default channel..
            return requestChat(null, args, true);
        }
    }

    /** Implements <code>/emote</code>. */
    protected class EmoteHandler extends CommandHandler
    {
        @Override
        public String handleCommand (SpeakService speakSvc, String command,
                                     String args, String[] history)
        {
            if (StringUtil.isBlank(args)) {
                return getUsage(command);
            }
            // note the command to be stored in the history
            history[0] = command + " ";
            return deliverChat(speakSvc, args, ChatCodes.EMOTE_MODE);
        }
    }

    /** Implements <code>/think</code>. */
    protected class ThinkHandler extends CommandHandler
    {
        @Override
        public String handleCommand (SpeakService speakSvc, String command,
                                     String args, String[] history)
        {
            if (StringUtil.isBlank(args)) {
                return getUsage(command);
            }
            // note the command to be stored in the history
            history[0] = command + " ";
            return deliverChat(speakSvc, args, ChatCodes.THINK_MODE);
        }
    }

    /** Implements <code>/tell</code>. */
    protected class TellHandler extends CommandHandler
    {
        @Override
        public String handleCommand (SpeakService speakSvc, final String command,
                                     String args, String[] history)
        {
            if (StringUtil.isBlank(args)) {
                return getUsage(command);
            }

            final boolean useQuotes = args.startsWith("\"");
            String[] bits = parseTell(args);
            String handle = bits[0];
            String message = bits[1];

            // validate that we didn't eat all the tokens making the handle
            if (StringUtil.isBlank(message)) {
                return getUsage(command);
            }

            // make sure we're not trying to tell something to ourselves
            BodyObject self = (BodyObject)_ctx.getClient().getClientObject();
            if (handle.equalsIgnoreCase(self.getVisibleName().toString())) {
                return "m.talk_self";
            }

            // and lets just give things an opportunity to sanitize the name
            Name target = normalizeAsName(handle);

            // mogrify the chat
            message = mogrifyChat(message);
            String err = checkLength(message);
            if (err != null) {
                return err;
            }

            // clear out from the history any tells that are mistypes
            for (Iterator<String> iter = _history.iterator(); iter.hasNext(); ) {
                String hist = iter.next();
                if (hist.startsWith("/" + command)) {
                    String harg = hist.substring(command.length() + 1).trim();
                    // we blow away any historic tells that have msg content
                    if (!StringUtil.isBlank(parseTell(harg)[1])) {
                        iter.remove();
                    }
                }
            }

            // store the full command in the history, even if it was mistyped
            final String histEntry = command + " " +
                (useQuotes ? ("\"" + target + "\"") : target.toString()) + " " + message;
            history[0] = histEntry;

            // request to send this text as a tell message
            requestTell(target, escapeMessage(message), new ResultListener<Name>() {
                public void requestCompleted (Name target) {
                    // replace the full one in the history with just: /tell "<handle>"
                    String newEntry = "/" + command + " " +
                        (useQuotes ? ("\"" + target + "\"") : String.valueOf(target)) + " ";
                    _history.remove(newEntry);
                    int dex = _history.lastIndexOf("/" + histEntry);
                    if (dex >= 0) {
                        _history.set(dex, newEntry);
                    } else {
                        _history.add(newEntry);
                    }
                }
                public void requestFailed (Exception cause) {
                    // do nothing
                }
            });

            return ChatCodes.SUCCESS;
        }

        /**
         * Parse the tell into two strings, handle and message. If either one is null then the
         * parsing did not succeed.
         */
        protected String[] parseTell (String args)
        {
            String handle, message;
            if (args.startsWith("\"")) {
                int nextQuote = args.indexOf('"', 1);
                if (nextQuote == -1 || nextQuote == 1) {
                    handle = message = null; // bogus parsing

                } else {
                    handle = args.substring(1, nextQuote).trim();
                    message = args.substring(nextQuote + 1).trim();
                }

            } else {
                StringTokenizer st = new StringTokenizer(args);
                handle = st.nextToken();
                message = args.substring(handle.length()).trim();
            }

            return new String[] { handle, message };
        }

        /**
         * Turn the user-entered string into a Name object, doing any particular normalization we
         * want to do along the way so that "/tell Bob" and "/tell BoB" don't both show up in
         * history.
         */
        protected Name normalizeAsName (String handle)
        {
            return new Name(handle);
        }

        /**
         * Escape or otherwise do any final processing on the message prior to sending it.
         */
        protected String escapeMessage (String msg)
        {
            return msg;
        }
    }

    /** Implements <code>/broadcast</code>. */
    protected class BroadcastHandler extends CommandHandler
    {
        @Override
        public String handleCommand (SpeakService speakSvc, String command,
                                     String args, String[] history)
        {
            if (StringUtil.isBlank(args)) {
                return getUsage(command);
            }

            // mogrify and verify length
            args = mogrifyChat(args);
            String err = checkLength(args);
            if (err != null) {
                return err;
            }

            // request the broadcast
            requestBroadcast(args);

            // note the command to be stored in the history
            history[0] = command + " ";

            return ChatCodes.SUCCESS;
        }

        @Override
        public boolean checkAccess (BodyObject user)
        {
            return user.checkAccess(ChatCodes.BROADCAST_ACCESS) == null;
        }
    }

    /** Our active chat context. */
    protected CrowdContext _ctx;

    /** Provides access to chat-related server-side services. */
    protected ChatService _cservice;

    /** The bundle to use for our own internal messages. */
    protected String _bundle;

    /** The place object that we currently occupy. */
    protected PlaceObject _place;

    /** The client object that we're listening to for tells. */
    protected ClientObject _clobj;

    /** Whether or not to run chat through the mogrifier. */
    protected boolean _mogrifyChat= true;

    /** A list of registered chat displays. */
    protected ObserverList<ChatDisplay> _displays = ObserverList.newSafeInOrder();

    /** A list of registered chat filters. */
    protected ObserverList<ChatFilter> _filters = ObserverList.newSafeInOrder();

    /** A mapping from auxiliary chat objects to the types under which
     * they are registered. */
    protected HashIntMap<String> _auxes = new HashIntMap<String>();

    /** Validator of who may be added to the chatters list. */
    protected ChatterValidator _chatterValidator;

    /** Usernames of users we've recently chatted with. */
    protected LinkedList<Name> _chatters = new LinkedList<Name>();

    /** Observers that are watching our chatters list. */
    protected ObserverList<ChatterObserver> _chatterObservers = ObserverList.newSafeInOrder();

    /** Operation used to filter chat messages. */
    protected FilterMessageOp _filterMessageOp = new FilterMessageOp();

    /** Operation used to display chat messages. */
    protected DisplayMessageOp _displayMessageOp = new DisplayMessageOp();

    /** A rolling chat history, or null if {@link #getHistory} is never called. */
    protected HistoryList _hlist;

    /** Registered chat command handlers. */
    protected static HashMap<String, CommandHandler> _handlers = Maps.newHashMap();

    /** A history of chat commands. */
    protected static ArrayList<String> _history = Lists.newArrayList();

    /** If set, only show the first alias (translation) of each command in the full list
     * and show the full list of aliases in the command usage. */
    protected static boolean _showAliasesInUsage;

    /** The maximum number of chatter usernames to track. */
    protected static final int MAX_CHATTERS = 6;

    /** The maximum number of commands to keep in the chat history. */
    protected static final int MAX_COMMAND_HISTORY = 10;
}
