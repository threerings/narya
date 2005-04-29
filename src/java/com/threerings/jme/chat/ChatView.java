//
// $Id$
//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2005 Three Rings Design, Inc., All Rights Reserved
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

package com.threerings.jme.chat;

import java.util.StringTokenizer;

import com.jme.bui.BContainer;
import com.jme.bui.BTextArea;
import com.jme.bui.BTextField;
import com.jme.bui.event.ActionEvent;
import com.jme.bui.event.ActionListener;
import com.jme.bui.layout.BorderLayout;

import com.threerings.util.Name;

import com.threerings.crowd.chat.client.ChatDirector;
import com.threerings.crowd.chat.client.ChatDisplay;
import com.threerings.crowd.chat.data.ChatCodes;
import com.threerings.crowd.chat.data.ChatMessage;
import com.threerings.crowd.chat.data.SystemMessage;
import com.threerings.crowd.chat.data.UserMessage;
import com.threerings.crowd.data.PlaceObject;

import com.threerings.jme.JmeContext;
import com.threerings.jme.Log;

/**
 * Displays chat messages and allows for their input.
 */
public class ChatView extends BContainer
    implements ChatDisplay
{
    public ChatView (JmeContext ctx, ChatDirector chatdtr)
    {
        _chatdtr = chatdtr;
        setLayoutManager(new BorderLayout(2, 2));
        addChild(_text = new BTextArea(), BorderLayout.CENTER);
        addChild(_input = new BTextField(), BorderLayout.SOUTH);

        _input.addListener(new ActionListener() {
            public void actionPerformed (ActionEvent event) {
                if (handleInput(_input.getText())) {
                    _input.setText("");
                }
            }
        });
    }

    public void willEnterPlace (PlaceObject plobj)
    {
        _chatdtr.addChatDisplay(this);
    }

    public void didLeavePlace (PlaceObject plobj)
    {
        _chatdtr.removeChatDisplay(this);
    }

    // documentation inherited from interface ChatDisplay
    public void clear ()
    {
    }

    // documentation inherited from interface ChatDisplay
    public void displayMessage (ChatMessage msg)
    {
        if (msg instanceof UserMessage) {
            UserMessage umsg = (UserMessage) msg;
            if (umsg.localtype == ChatCodes.USER_CHAT_TYPE) {
                append("[" + umsg.speaker + " whispers] " + umsg.message);
            } else {
                append("<" + umsg.speaker + "> " + umsg.message);
            }

        } else if (msg instanceof SystemMessage) {
            append(msg.message);

        } else {
            Log.warning("Received unknown message type: " + msg + ".");
        }
    }

    protected void displayError (String message)
    {
        append(message);
    }

    protected void append (String text)
    {
        _text.appendText(text + "\n");
    }

    protected boolean handleInput (String text)
    {
        // if the message to send begins with /tell then parse it and
        // generate a tell request rather than a speak request
        if (text.startsWith("/tell")) {
            StringTokenizer tok = new StringTokenizer(text);
            // there should be at least three tokens: '/tell target word'
            if (tok.countTokens() < 3) {
                displayError("Usage: /tell username message");
                return false;
            }

            // skip the /tell and grab the username
            tok.nextToken();
            String username = tok.nextToken();

            // now strip off everything up to the username to get the
            // message
            int uidx = text.indexOf(username);
            String message = text.substring(uidx + username.length()).trim();

            // request to send this text as a tell message
            _chatdtr.requestTell(new Name(username), message, null);

        } else if (text.startsWith("/clear")) {
            // clear the chat box
            _chatdtr.clearDisplays();

        } else if (text.startsWith("/")) {
            displayError("Error: unknown slash command.");
            return false;

        } else {
            // request to send this text as a chat message
            _chatdtr.requestSpeak(text);
        }

        return true;
    }

    protected ChatDirector _chatdtr;
    protected BTextArea _text;
    protected BTextField _input;
}
