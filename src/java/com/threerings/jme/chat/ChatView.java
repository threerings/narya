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

import com.jme.image.Texture;
import com.jme.math.Vector3f;
import com.jme.scene.Controller;
import com.jme.scene.Node;
import com.jme.scene.Text;
import com.jme.scene.state.AlphaState;
import com.jme.scene.state.TextureState;
import com.jme.ui.UIEditBox;
import com.jme.ui.UIObject;
import com.jme.util.TextureManager;

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
public class ChatView extends Node
    implements ChatDisplay
{
    public ChatView (JmeContext ctx, ChatDirector chatdtr)
    {
        super("ChatView");

        _chatdtr = chatdtr;

        // create an alpha state that will allow us to blend the text on
        // top of whatever else is below
        AlphaState astate = ctx.getRenderer().createAlphaState();
        astate.setBlendEnabled(true);
        astate.setSrcFunction(AlphaState.SB_SRC_ALPHA);
        astate.setDstFunction(AlphaState.DB_ONE);
        astate.setTestEnabled(true);
        astate.setTestFunction(AlphaState.TF_GREATER);
        astate.setEnabled(true);

        // create a font texture
        TextureState font = ctx.getRenderer().createTextureState();
        font.setTexture(
            TextureManager.loadTexture(
                getClass().getClassLoader().getResource(DEFAULT_JME_FONT),
                Texture.MM_LINEAR, Texture.FM_LINEAR));
        font.setEnabled(true);

        _entry = new UIEditBox(
            "Entry", 20, 20, 300, 20, ctx.getInputHandler(),
            ctx.getBufferedInputHandler(), ctx.getColorScheme(),
            ctx.getFonts(), "main", "", 65.0f, 0.0f, UIObject.INVERSE_BORDER);
        attachChild(_entry);

        // create a fixed number of text lines to display our text
        _text = new Text[5];
        _history = new String[_text.length];
        int ypos = 40;
        for (int ii = 0; ii < _text.length; ii++) {
            _history[ii] = "Line " + ii;
            _text[ii] = new Text("History" + ii, _history[ii]);
            _text[ii].setLocalTranslation(new Vector3f(0, ypos, 0));
            ypos += 20;
            attachChild(_text[ii]);
        }

        setRenderState(font);
        setRenderState(astate);

        // add a controller that will update our edit field
        addController(new Controller() {
            public void update (float time) {
                _entry.update(time);
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
        String text = _entry.getText();

        // if the message to send begins with /tell then parse it and
        // generate a tell request rather than a speak request
        if (text.startsWith("/tell")) {
            StringTokenizer tok = new StringTokenizer(text);
            // there should be at least three tokens: '/tell target word'
            if (tok.countTokens() < 3) {
                displayError("Usage: /tell username message");
                return;
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

        } else {
            // request to send this text as a chat message
            _chatdtr.requestSpeak(text);
        }

        // clear out the input because we sent a request
        _entry.setText("");
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
    }

    protected ChatDirector _chatdtr;
    protected UIEditBox _entry;

    protected String[] _history;
    protected Text[] _text;

    protected static final String DEFAULT_JME_FONT =
        "com/jme/app/defaultfont.tga";
}
