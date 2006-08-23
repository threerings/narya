package com.threerings.mx.controls {

import flash.display.DisplayObjectContainer;

import flash.events.Event;

import mx.controls.TextArea;

import com.threerings.crowd.chat.client.ChatDirector;
import com.threerings.crowd.chat.client.ChatDisplay;
import com.threerings.crowd.chat.data.ChatMessage;
import com.threerings.crowd.chat.data.UserMessage;
import com.threerings.crowd.util.CrowdContext;

/**
 * IMPORTANT NOTE: this class was written for testing things and does not
 * necessarily represent a valid starting point for writing the chat
 * widget we'll eventually need.
 */
public class ChatDisplayBox extends TextArea
    implements ChatDisplay
{
    public function ChatDisplayBox (ctx :CrowdContext)
    {
        _ctx = ctx;
        this.editable = false;

        // TODO
        width = 400;
        height = 150;
    }

    // documentation inherited from interface ChatDisplay
    public function clear () :void
    {
        this.htmlText = "";
    }

    // documentation inherited from interface ChatDisplay
    public function displayMessage (msg :ChatMessage) :void
    {
        if (!_scrollBot) {
            _scrollBot = (verticalScrollPosition == maxVerticalScrollPosition);
        }

        // display the message
        if (msg is UserMessage) {
            this.htmlText += "<font color=\"red\">&lt;" +
                (msg as UserMessage).speaker + "&gt;</font> ";
        }
        this.htmlText += msg.message;
    }

    override public function parentChanged (p :DisplayObjectContainer) :void
    {
        super.parentChanged(p);

        var chatdir :ChatDirector = _ctx.getChatDirector();
        if (p != null) {
            chatdir.addChatDisplay(this);
        } else {
            chatdir.removeChatDisplay(this);
        }
    }

    // documentation inherited
    override protected function updateDisplayList (uw :Number, uh :Number) :void
    {
        super.updateDisplayList(uw, uh);

        if (_scrollBot) {
            verticalScrollPosition = maxVerticalScrollPosition;
            _scrollBot = false;
        }
    }

    /** The giver of life. */
    protected var _ctx :CrowdContext;

    protected var _scrollBot :Boolean;
}
}
