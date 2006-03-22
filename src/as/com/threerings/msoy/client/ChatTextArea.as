package com.threerings.msoy.client {

import mx.controls.TextArea;

import com.threerings.crowd.chat.client.ChatDirector;
import com.threerings.crowd.chat.client.ChatDisplay;

public class ChatTextArea extends TextArea
    implements ChatDisplay
{
    public function ChatTextArea (ctx :MsoyContext)
    {
        _ctx = ctx;
        this.editable = false;

        // set up some events to manage how we'll be shown, etc.
        addEventListener("creationComplete", checkVis);
        addEventListener("show", checkVis);
        addEventListener("hide", checkVis);
    }

    // documentation inherited from interface ChatDisplay
    public function clear () :void
    {
        this.htmlText = "";
    }

    // documentation inherited from interface ChatDisplay
    public function displayMessage (msg :ChatMessage) :void
    {
        this.htmlText += "<font color=\"red\">&lt;TODO&gt;</font> " +
            msg.message;
    }

    /**
     * Check to see if we should register or unregister ourselves as a
     * ChatDisplay.
     */
    protected void checkVis (event :Event) :void
    {
        var chatdir :ChatDirector = _ctx.getChatDirector();
        if (this.visible) {
            chatdir.addChatDisplay(this);
        } else {
            chatdir.removeChatDisplay(this);
        }
    }

    /** The giver of life. */
    protected var _ctx :MsoyContext;
}
}
