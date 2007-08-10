//
// $Id$
//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2007 Three Rings Design, Inc., All Rights Reserved
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

package com.threerings.crowd.chat.client {

import com.threerings.util.ArrayUtil;
import com.threerings.util.HashMap;
import com.threerings.util.Map;
import com.threerings.util.ObserverList;
import com.threerings.util.ResultListener;
import com.threerings.util.StringUtil;

import com.threerings.presents.client.BasicDirector;
import com.threerings.presents.client.Client;
import com.threerings.presents.client.ClientEvent;
import com.threerings.presents.client.InvocationAdapter;
import com.threerings.presents.data.ClientObject;
import com.threerings.presents.dobj.DObject;
import com.threerings.presents.dobj.MessageEvent;
import com.threerings.presents.dobj.MessageListener;

import com.threerings.util.Long;
import com.threerings.util.MessageBundle;
import com.threerings.util.MessageManager;
import com.threerings.util.Name;
import com.threerings.util.TimeUtil;

import com.threerings.crowd.client.LocationObserver;
import com.threerings.crowd.data.BodyObject;
import com.threerings.crowd.data.CrowdCodes;
import com.threerings.crowd.data.PlaceObject;
import com.threerings.crowd.util.CrowdContext;

import com.threerings.crowd.chat.data.ChatCodes;
import com.threerings.crowd.chat.data.ChatMessage;
import com.threerings.crowd.chat.data.SystemMessage;
import com.threerings.crowd.chat.data.TellFeedbackMessage;
import com.threerings.crowd.chat.data.UserMessage;
import com.threerings.crowd.chat.data.UserSystemMessage;

/**
 * The chat director is the client side coordinator of all chat related services. It handles both
 * place constrained chat as well as direct messaging.
 */
public class ChatDirector extends BasicDirector
    implements LocationObserver, MessageListener
{
    /**
     * Creates a chat director and initializes it with the supplied context. The chat director will
     * register itself as a location observer so that it can automatically process place
     * constrained chat.
     *
     * @param msgmgr the message manager via which we do our translations.
     * @param bundle the message bundle from which we obtain our chat-related translation strings.
     */
    public function ChatDirector (ctx :CrowdContext, msgmgr :MessageManager, bundle :String)
    {
        super(ctx);

        // keep the context around
        _cctx = ctx;
        _msgmgr = msgmgr;
        _bundle = bundle;

        // register ourselves as a location observer
        _cctx.getLocationDirector().addLocationObserver(this);

        if (_bundle == null || _msgmgr == null) {
            Log.getLog(this).warning("Null bundle or message manager given to ChatDirector");
            return;
        }
        var msg :MessageBundle = _msgmgr.getBundle(_bundle);
        // register our default chat handlers
        registerCommandHandler(msg, "help", new HelpHandler());
        registerCommandHandler(msg, "clear", new ClearHandler());
        registerCommandHandler(msg, "speak", new SpeakHandler());
        registerCommandHandler(msg, "emote", new EmoteHandler());
        registerCommandHandler(msg, "think", new ThinkHandler());
//        registerCommandHandler(msg, "tell", new TellHandler());
        registerCommandHandler(msg, "broadcast", new BroadcastHandler());
    }

    /**
     * Adds the supplied chat display to the front of the chat display list. It will subsequently
     * be notified of incoming chat messages as well as tell responses.
     */
    public function pushChatDisplay (display :ChatDisplay) :void
    {
        _displays.add(display, 0);
    }

    /**
     * Adds the supplied chat display to the chat display list. It will subsequently be notified of
     * incoming chat messages as well as tell responses.
     */
    public function addChatDisplay (display :ChatDisplay) :void
    {
        _displays.add(display);
    }

    /**
     * Removes the specified chat display from the chat display list. The display will no longer
     * receive chat related notifications.
     */
    public function removeChatDisplay (display :ChatDisplay) :void
    {
        _displays.remove(display);
    }

    /**
     * Adds the specified chat filter to the list of filters.  All chat requests and receipts will
     * be filtered with all filters before they being sent or dispatched locally.
     */
    public function addChatFilter (filter :ChatFilter) :void
    {
        _filters.add(filter);
    }

    /**
     * Removes the specified chat validator from the list of chat validators.
     */
    public function removeChatFilter (filter :ChatFilter) :void
    {
        _filters.remove(filter);
    }

    /**
     * Adds an observer that watches the chatters list, and updates it immediately.
     */
    public function addChatterObserver (co :ChatterObserver) :void
    {
        _chatterObservers.add(co);
        co.chattersUpdated(_chatters);
    }

    /**
     * Removes an observer from the list of chatter observers.
     */
    public function removeChatterObserver (co :ChatterObserver) :void
    {
        _chatterObservers.remove(co);
    }

    /**
     * Sets the validator that decides if a username is valid to be added to the chatter list, or
     * null if no such filtering is desired.
     */
    public function setChatterValidator (validator :ChatterValidator) :void
    {
        _chatterValidator = validator;
    }

    /**
     * Get a list of the recent users with whom we've chatted. The most recent users will be listed
     * first.
     */
    public function getChatters () :Array
    {
        return _chatters;
    }

    /**
     * Adds a chatter to our list of recent chatters. This is normally done automatically by the
     * ChatDirector, but may be called to also to forcibly add a chatter. The ChatterValidator will
     * have to approve of the chatter.
     */
    public function addChatter (name :Name) :void
    {
        // check to see if the chatter validator approves..
        if ((_chatterValidator != null) && (!_chatterValidator.isChatterValid(name))) {
            return;
        }

        var wasThere :Boolean = ArrayUtil.removeAll(_chatters, name);
        _chatters.unshift(name);

        if (!wasThere) {
            if (_chatters.length > MAX_CHATTERS) {
                _chatters.length = MAX_CHATTERS; // truncate array
            }

            // we only notify on a change to the contents, not just reordering
            notifyChatterObservers();
        }
    }

    /**
     * Registers a chat command handler.
     *
     * @param msg the message bundle via which the slash command will be translated (as
     * <code>c.</code><i>command</i>). If no translation exists the command will be
     * <code>/</code><i>command</i>.
     * @param command the name of the command that will be used to invoke this handler
     * (e.g. <code>tell</code> if the command will be invoked as <code>/tell</code>).
     * @param handler the chat command handler itself.
     */
    public function registerCommandHandler (
        msg :MessageBundle, command :String, handler :CommandHandler) :void
    {
        var key :String = "c." + command;
        if (msg.exists(key)) {
            var tokens :Array = msg.get(key).split(/\s+/);
            for each (var cmd :String in tokens) {
                _handlers.put(cmd, handler);
            }
        } else {
            // fall back to just using the English command
            _handlers.put(command, handler);
        }
    }

    /**
     * Return the current size of the history.
     */
    public function getCommandHistorySize () :int
    {
        return _history.length;
    }

    /**
     * Get the chat history entry at the specified index, with 0 being the oldest.
     */
    public function getCommandHistory (index :int) :String
    {
        return (_history[index] as String);
    }

    /**
     * Clear the chat command history.
     */
    public function clearCommandHistory () :void
    {
        _history.length = 0;
    }

    /**
     * Requests that all chat displays clear their contents.
     */
    public function clearDisplays () :void
    {
        _displays.apply(function (disp :ChatDisplay) :void {
            disp.clear();
        });
    }

    /**
     * Display a system INFO message as if it had come from the server.
     *
     * Info messages are sent when something happens that was neither directly triggered by the
     * user, nor requires direct action.
     */
    public function displayInfo (bundle :String, message :String, localtype :String = null) :void
    {
        if (localtype == null) {
            localtype = ChatCodes.PLACE_CHAT_TYPE;
        }
        displaySystem(bundle, message, SystemMessage.INFO, localtype);
    }

    /**
     * Display a system FEEDBACK message as if it had come from the server.  The localtype of the
     * message will be PLACE_CHAT_TYPE.
     *
     * Feedback messages are sent in direct response to a user action, usually to indicate success
     * or failure of the user's action.
     */
    public function displayFeedback (bundle :String, message :String) :void
    {
        displaySystem(bundle, message, SystemMessage.FEEDBACK, ChatCodes.PLACE_CHAT_TYPE);
    }

    /**
     * Display a system ATTENTION message as if it had come from the server.  The localtype of the
     * message will be PLACE_CHAT_TYPE.
     *
     * Attention messages are sent when something requires user action that did not result from
     * direct action by the user.
     */
    public function displayAttention (bundle :String, message :String) :void
    {
        displaySystem(bundle, message, SystemMessage.ATTENTION, ChatCodes.PLACE_CHAT_TYPE);
    }

    /**
     * Dispatches the provided message to our chat displays.
     */
    public function dispatchMessage (message :ChatMessage, localType :String) :void
    {
        message.setClientInfo(xlate(message.bundle, message.message), localType);
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
    public function requestChat (speakSvc :SpeakService, text :String, record :Boolean) :String
    {
        if (text.indexOf("/") == 0) {
            // split the text up into a command and arguments
            var command :String = text.substring(1).toLowerCase();
            var hist :Array = new Array();
            var args :String = "";
            var sidx :int = text.indexOf(" ");
            if (sidx != -1) {
                command = text.substring(1, sidx).toLowerCase();
                args = StringUtil.trim(text.substring(sidx+1));
            }

            var possibleCommands :Map = getCommandHandlers(command);
            switch (possibleCommands.size()) {
            case 0:
                return MessageBundle.tcompose("m.unknown_command", text.split(/\s/)[0]);

            case 1:
                var cmdName :String = possibleCommands.keys()[0];
                var cmd :CommandHandler = (possibleCommands.get(cmdName) as CommandHandler);
                var result :String = cmd.handleCommand(_cctx, speakSvc, cmdName, args, hist);
                if (result != ChatCodes.SUCCESS) {
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
                var cmds :Array = possibleCommands.keys();
                var alternativeCommands :String = "/" + cmds.join(", /");
                return MessageBundle.tcompose("m.unspecific_command", alternativeCommands);
            }
        }

        // if not a command then just speak
        var message :String = StringUtil.trim(text);
        if (StringUtil.isBlank(message)) {
            return ChatCodes.SUCCESS; // report silent failure for now
        }

        return deliverChat(speakSvc, message, ChatCodes.DEFAULT_MODE);
    }

    /**
     * Requests that a speak message with the specified mode be generated and delivered via the
     * supplied speak service instance (which will be associated with a particular "speak object").
     * The message will first be validated by all registered {@link ChatFilter}s (and possibly
     * vetoed) before being dispatched.
     *
     * @param speakService the speak service to use when generating the speak request or null if we
     * should speak in the current "place".
     * @param message the contents of the speak message.
     * @param mode a speech mode that will be interpreted by the {@link ChatDisplay}
     * implementations that eventually display this speak message.
     */
    public function requestSpeak (speakService :SpeakService, message :String, mode :int) :void
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
        speakService.speak(_cctx.getClient(), message, mode);
    }

    /**
     * Requests to send a site-wide broadcast message.
     *
     * @param message the contents of the message.
     */
    public function requestBroadcast (message :String) :void
    {
        message = filter(message, null, true);
        if (message == null) {
            displayFeedback(_bundle, MessageBundle.compose("m.broadcast_failed", "m.filtered"));
            return;
        }

        var failure :Function = function (reason :String) :void {
            reason = MessageBundle.compose("m.broadcast_failed", reason);
            displayFeedback(_bundle, reason);
        };
        _cservice.broadcast(_cctx.getClient(), message, new InvocationAdapter(failure));
    }

    /**
     * Requests that a tell message be delivered to the specified target user.
     *
     * @param target the username of the user to which the tell message should be delivered.
     * @param msg the contents of the tell message.
     * @param rl an optional result listener if you'd like to be notified of success or failure.
     */
    public function requestTell (target :Name, msg :String, rl :ResultListener) :void
    {
        // make sure they can say what they want to say
        var message :String = filter(msg, target, true);
        if (message == null) {
            if (rl != null) {
                rl.requestFailed(null);
            }
            return;
        }

        var failure :Function = function (reason :String) :void {
            var msg :String = MessageBundle.compose(
                "m.tell_failed", MessageBundle.taint(target), reason);
            var tfm :TellFeedbackMessage = new TellFeedbackMessage(target, msg, true);
            tfm.bundle = _bundle;
            dispatchMessage(tfm, ChatCodes.PLACE_CHAT_TYPE);
            if (rl != null) {
                rl.requestFailed(null);
            }
        };
        var success :Function = function (idleTime :Long, awayMessage :String) :void {
            dispatchMessage(new TellFeedbackMessage(target, message), ChatCodes.PLACE_CHAT_TYPE);
            addChatter(target);
            if (rl != null) {
                rl.requestCompleted(target);
            }

            // if they have an away message, report that
            if (awayMessage != null) {
                awayMessage = filter(awayMessage, target, false);
                if (awayMessage != null) {
                    var msg :String = MessageBundle.tcompose(
                        "m.recipient_afk", target, awayMessage);
                    displayFeedback(_bundle, msg);
                }
            }

            // if they are idle, report that
//            if (idletime > 0) {
//                // adjust by the time it took them to become idle
//                idletime += _cctx.getConfig().getValue(
//                    IDLE_TIME_KEY, DEFAULT_IDLE_TIME);
//                var msg :String = MessageBundle.compose(
//                    "m.recipient_idle", MessageBundle.taint(target),
//                    TimeUtil.getTimeOrderString(idletime, TimeUtil.MINUTE));
//                displayFeedback(_bundle, msg);
//            }
        };
        _cservice.tell(_cctx.getClient(), target, message, new TellAdapter(failure, success));
    }

    /**
     * Configures a message that will be automatically reported to anyone that sends a tell message
     * to this client to indicate that we are busy or away from the keyboard.
     */
    public function setAwayMessage (message :String) :void
    {
        if (message != null) {
            message = filter(message, null, true);
            if (message == null) {
                // they filtered away their own away message..  change it to something
                message = "...";
            }
        }
        // pass the buck right on along
        _cservice.away(_cctx.getClient(), message);
    }

    /**
     * Adds an additional object via which chat messages may arrive. The chat director assumes the
     * caller will be managing the subscription to this object and will remain subscribed to it for
     * as long as it remains in effect as an auxiliary chat source.
     *
     * @param localtype a type to be associated with all chat messages that arrive on the specified
     * DObject.
     */
    public function addAuxiliarySource (source :DObject, localtype :String) :void
    {
        source.addListener(this);
        _auxes.put(source.getOid(), localtype);
    }

    /**
     * Removes a previously added auxiliary chat source.
     */
    public function removeAuxiliarySource (source :DObject) :void
    {
        source.removeListener(this);
        _auxes.remove(source.getOid());
    }

    /**
     * Run a message through all the currently registered filters.
     */
    public function filter (msg :String, otherUser :Name, outgoing :Boolean) :String
    {
        _filters.apply(function (observer :ChatFilter) :void {
            if (msg != null) {
                msg = observer.filter(msg, otherUser, outgoing);
            }
        });
        return msg;
    }

    /**
     * Runs the supplied message through the various chat mogrifications.
     */
    public function mogrifyChat (text :String) :String
    {
        return mogrifyChatImpl(text, false, true);
    }

    // documentation inherited from interface LocationObserver
    public function locationMayChange (placeId :int) :Boolean
    {
        // we accept all location change requests
        return true;
    }

    // documentation inherited from interface LocationObserver
    public function locationDidChange (place :PlaceObject) :void
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

    // documentation inherited from interface LocationObserver
    public function locationChangeFailed (placeId :int, reason :String) :void
    {
        // nothing we care about
    }

    // documentation inherited from interface MessageListener
    public function messageReceived (event :MessageEvent) :void
    {
        if (ChatCodes.CHAT_NOTIFICATION === event.getName()) {
            var msg :ChatMessage = (event.getArgs()[0] as ChatMessage);
            var localtype :String = getLocalType(event.getTargetOid());
            processReceivedMessage(msg, localtype);
        }
    }
        
    // documentation inherited
    override public function clientDidLogon (event :ClientEvent) :void
    {
        super.clientDidLogon(event);

        // listen on the client object for tells
        addAuxiliarySource(_clobj = event.getClient().getClientObject(), ChatCodes.USER_CHAT_TYPE);
    }

    // documentation inherited
    override public function clientObjectDidChange (event :ClientEvent) :void
    {
        super.clientObjectDidChange(event);

        // change what we're listening to for tells
        removeAuxiliarySource(_clobj);
        addAuxiliarySource(_clobj = event.getClient().getClientObject(), ChatCodes.USER_CHAT_TYPE);

        clearDisplays();
    }

    // documentation inherited
    override public function clientDidLogoff (event :ClientEvent) :void
    {
        super.clientDidLogoff(event);

        // stop listening to it for tells
        if (_clobj != null) {
            removeAuxiliarySource(_clobj);
            _clobj = null;
        }
        // in fact, clear out all auxiliary sources
        _auxes.clear();

        clearDisplays();

        // clear out the list of people we've chatted with
        _chatters.length = 0;
        notifyChatterObservers();

        // clear the _place
        locationDidChange(null);

        // clear our service
        _cservice = null;
    }

    /**
     * Processes and dispatches the specified chat message.
     */
    protected function processReceivedMessage (msg :ChatMessage, localtype :String) :void
    {
        var autoResponse :String = null;
        var speaker :Name = null;
        var mode :int = -1;

        // figure out if the message was triggered by another user
        if (msg is UserMessage) {
            var umsg :UserMessage = (msg as UserMessage);
            speaker = umsg.speaker;
            mode = umsg.mode;

        } else if (msg is UserSystemMessage) {
            speaker = (msg as UserSystemMessage).speaker;
        }

        // if there was an originating speaker, see if we want to hear it
        if (speaker != null) {
            if ((msg.message = filter(msg.message, speaker, false)) == null) {
                return;
            }

            if (ChatCodes.USER_CHAT_TYPE == localtype && mode == ChatCodes.DEFAULT_MODE) {
                // if it was a tell, add the speaker as a chatter
                addChatter(speaker);

                // note whether or not we have an auto-response
                var self :BodyObject = (_cctx.getClient().getClientObject() as BodyObject);
                if (!StringUtil.isBlank(self.awayMessage)) {
                    autoResponse = self.awayMessage;
                }
            }
        }

        // and send it off!
        dispatchMessage(msg, localtype);

        // if we auto-responded, report as much
        if (autoResponse != null) {
            var amsg :String = MessageBundle.tcompose(
                "m.auto_responded", speaker, autoResponse);
            displayFeedback(_bundle, amsg);
        }
    }

    /**
     * Dispatch a message to chat displays once it is fully prepared with the clientinfo.
     */
    protected function dispatchPreparedMessage (message :ChatMessage) :void
    {
        var displayed :Boolean = false;
        _displays.apply(function (disp :ChatDisplay) :void {
            if (disp.displayMessage(message, displayed)) {
                displayed = true;
            }
        });
    }

    /**
     * Called to determine whether we are permitted to post the supplied chat message. Derived
     * classes may wish to throttle chat or restrict certain types in certain circumstances for
     * whatever reason.
     *
     * @return null if the chat is permitted, SUCCESS if the chat is permitted and has already been
     * dealt with, or a translatable string indicating the reason for rejection if not.
     */
    protected function checkCanChat (speakSvc :SpeakService , message :String, mode :int) :String
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
    internal function deliverChat (speakSvc :SpeakService, message :String, mode :int) :String
    {
        // run the message through our mogrification process
        message = mogrifyChatImpl(message, true, mode != ChatCodes.EMOTE_MODE);

        // mogrification may result in something being turned into a slash command, in which case
        // we have to run everything through again from the start
        if (message.indexOf("/") == 0) {
            return requestChat(speakSvc, message, false);
        }

        // make sure this client is not restricted from performing this chat message for some
        // reason or other
        var errmsg :String = checkCanChat(speakSvc, message, mode);
        if (errmsg != null) {
            return errmsg;
        }

        // speak on the specified service
        requestSpeak(speakSvc, message, mode);

        return ChatCodes.SUCCESS;
    }

    /**
     * Add the specified command to the history.
     */
    protected function addToHistory (cmd :String) :void
    {
        // remove any previous instance of this command
        ArrayUtil.removeAll(_history, cmd);

        // append it to the end
        _history.push(cmd);

        // prune the history once it extends beyond max size
        if (_history.length > MAX_COMMAND_HISTORY) {
            _history.shift();
        }
    }

    /**
     * Mogrify common literary crutches into more appealing chat or commands.
     *
     * @param transformsAllowed if true, the chat may transformed into a different mode. (lol ->
     * /emote laughs)
     * @param capFirst if true, the first letter of the text is capitalized. This is not desired if
     * the chat is already an emote.
     */
    protected function mogrifyChatImpl (
        text :String, transformsAllowed :Boolean, capFirst :Boolean) :String
    {
        var tlen :int = text.length;
        if (tlen == 0) {
            return text;

        // check to make sure there aren't too many caps
        } else if (tlen > 7 && suppressTooManyCaps()) {
            // count caps
            var caps :int = 0;
            for (var ii :int = 0; ii < tlen; ii++) {
                var ch :String = text.charAt(ii);
                // do a fucked-up test to see if it's uppercase make sure the uppercase version is
                // the same as the orig and the lowercase version is different
                var chU :String = ch.toUpperCase();
                if (ch == chU && chU != ch.toLowerCase()) {
                    caps++;
                    if (caps > (tlen / 2)) {
                        // lowercase the whole string if there are
                        text = text.toLowerCase();
                        break;
                    }
                }
            }
        }

        return mogrifyChatText(text, transformsAllowed, capFirst);
    }

    /** Helper function for {@link #mogrifyChat}. */
    protected function mogrifyChatText (
        text :String, transformsAllowed :Boolean, capFirst :Boolean) :String
    {
        // do the generic mogrifications and translations
        text = translatedReplacements("x.mogrifies", text);

        // perform themed expansions and transformations
        if (transformsAllowed) {
            text = translatedReplacements("x.transforms", text);
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
            buf = new StringBuffer();
            m.appendReplacement(buf, m.group().toUpperCase());
            while (m.find()) {
                m.appendReplacement(buf, m.group().toUpperCase());
            }
            m.appendTail(buf);
        }
        */

        return text;
    }

    /**
     * Do all the replacements (mogrifications) specified in the translation string specified by
     * the key.
     */
    protected function translatedReplacements (key :String, text :String) :String
    {
        var bundle :MessageBundle = _msgmgr.getBundle(_bundle);
        if (!bundle.exists(key)) {
            return text;
        }
        var repls :Array = bundle.get(key).split("#");
        // apply the replacements to each mogrification that matches
        for (var ii :int = 0; ii < repls.length; ii++) {
            var pattern :RegExp = new RegExp((repls[ii] as String), "gim");
            var replace :String = (repls[++ii] as String);
            text = text.replace(pattern, replace);
        }
        return text;
    }

    /**
     * Return true if we should lowercase messages containing more than half upper-case characters.
     */
    protected function suppressTooManyCaps () :Boolean
    {
        return true;
    }

    /**
     * Check that after mogrification the message is not too long.
     * @return an error mesage if it is too long, or null.
     */
    internal function checkLength (msg :String) :String
    {
        return null; // TODO
    }

    /**
     * Returns a hashmap containing all command handlers that match the specified command (i.e. the
     * specified command is a prefix of their registered command string).
     */
    internal function getCommandHandlers (command :String) :Map
    {
        var matches :Map = new HashMap();
        var user :BodyObject = (_cctx.getClient().getClientObject() as BodyObject);
        var keys :Array = _handlers.keys();
        for (var ii :int = 0; ii < keys.length; ii++) {
            var cmd :String = (keys[ii] as String);
            if (cmd.indexOf(command) != 0) {
                continue;
            }
            var handler :CommandHandler = (_handlers.get(cmd) as CommandHandler);
            if (handler.checkAccess(user)) {
                matches.put(cmd, handler);
            }
        }
        return matches;
    }

    internal function accessHistory () :Array
    {
        return _history;
    }

    /**
     * Notifies all registered {@link ChatterObserver}s that the list of chatters has changed.
     */
    protected function notifyChatterObservers () :void
    {
        _chatterObservers.apply(chatterObserverNotify);
    }

    /**
     * A function to be called by our _chatterObservers list to apply updates to each observer.
     */
    protected function chatterObserverNotify (obs :ChatterObserver) :void
    {
        obs.chattersUpdated(_chatters);
    }

    /**
     * Translates the specified message using the specified bundle.
     */
    protected function xlate (bundle :String, message :String) :String
    {
        if (bundle != null && _msgmgr != null) {
            var msgb :MessageBundle = _msgmgr.getBundle(bundle);
            if (msgb == null) {
                Log.getLog(this).warning("No message bundle available to translate message " +
                                         "[bundle=" + bundle + ", message=" + message + "].");
            } else {
                message = msgb.xlate(message);
            }
        }
        return message;
    }

    /**
     * Display the specified system message as if it had come from the server.
     */
    protected function displaySystem (
        bundle :String, message :String, attLevel :int, localtype :String) :void
    {
        // nothing should be untranslated, so pass the default bundle if need be.
        if (bundle == null) {
            bundle = _bundle;
        }
        var msg :SystemMessage = new SystemMessage(message, bundle, attLevel);
        dispatchMessage(msg, localtype);
    }

    /**
     * Looks up and returns the message type associated with the specified oid.
     */
    protected function getLocalType (oid :int) :String
    {
        var typ :String = (_auxes.get(oid) as String);
        return (typ == null) ? ChatCodes.PLACE_CHAT_TYPE : typ;
    }

    // from BasicDirector
    override protected function registerServices (client :Client) :void
    {
        client.addServiceGroup(CrowdCodes.CROWD_GROUP);
    }

    // from BasicDirector
    override protected function fetchServices (client :Client) :void
    {
        // get a handle on our chat service
        _cservice = (client.requireService(ChatService) as ChatService);
    }

    /** Our active chat context. */
    protected var _cctx :CrowdContext;

    /** Provides access to chat-related server-side services. */
    protected var _cservice :ChatService;

    /** The message manager. */
    protected var _msgmgr :MessageManager;

    /** The bundle to use for our own internal messages. */
    protected var _bundle :String;

    /** The place object that we currently occupy. */
    protected var _place :PlaceObject;

    /** The client object that we're listening to for tells. */
    protected var _clobj :ClientObject;

    /** A list of registered chat displays. */
    protected var _displays :ObserverList = new ObserverList();

    /** A list of registered chat filters. */
    protected var _filters :ObserverList = new ObserverList();

    /** A mapping from auxiliary chat objects to the types under which they are registered. */
    protected var _auxes :HashMap = new HashMap();

    /** Validator of who may be added to the chatters list. */
    protected var _chatterValidator :ChatterValidator;

    /** Usernames of users we've recently chatted with. */
    protected var _chatters :Array = [];

    /** Observers that are watching our chatters list. */
    protected var _chatterObservers :ObserverList = new ObserverList();

    /** Registered chat command handlers. */
    protected static const _handlers :HashMap = new HashMap();

    /** A history of chat commands. */
    protected static const _history :Array = new Array();

    /** The maximum number of chatter usernames to track. */
    protected static const MAX_CHATTERS :int = 6;

    /** The maximum number of commands to keep in the chat history. */
    protected static const MAX_COMMAND_HISTORY :int = 10;
}
}
