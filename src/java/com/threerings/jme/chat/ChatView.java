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

import com.jmex.bui.BContainer;
import com.jmex.bui.BTextArea;
import com.jmex.bui.BTextField;
import com.jmex.bui.event.ActionEvent;
import com.jmex.bui.event.ActionListener;
import com.jmex.bui.layout.BorderLayout;

import com.jme.renderer.ColorRGBA;

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
        add(_text = new BTextArea(), BorderLayout.CENTER);
        add(_input = new BTextField(), BorderLayout.SOUTH);

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
        _plobj = plobj;
        _chatdtr.addChatDisplay(this);
    }

    public void didLeavePlace (PlaceObject plobj)
    {
        _chatdtr.removeChatDisplay(this);
        _plobj = null;
    }

    /**
     * Instructs our chat input field to request focus.
     */
    public void requestFocus ()
    {
        _input.requestFocus();
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
                append("[" + umsg.speaker + " whispers] ", ColorRGBA.green);
                append(umsg.message + "\n");
            } else {
                append("<" + umsg.speaker + "> ", ColorRGBA.blue);
                append(umsg.message + "\n");
            }

        } else if (msg instanceof SystemMessage) {
            append(msg.message + "\n", ColorRGBA.red);

        } else {
            Log.warning("Received unknown message type: " + msg + ".");
        }
    }

    protected void displayError (String message)
    {
        append(message + "\n", ColorRGBA.red);
    }

    protected void append (String text, ColorRGBA color)
    {
        _text.appendText(text, color);
    }

    protected void append (String text)
    {
        _text.appendText(text);
    }

    protected boolean handleInput (String text)
    {
        if (text.length() == 0) {
            // no empty banter
            return false;
        }
        String msg = _chatdtr.requestChat(_plobj.speakService, text, true);
        if (msg.equals(ChatCodes.SUCCESS)) {
            return true;
        } else {
            _chatdtr.displayFeedback(null, msg);
            return false;
        }
    }

    protected ChatDirector _chatdtr;
    protected PlaceObject _plobj;
    protected BTextArea _text;
    protected BTextField _input;
}
