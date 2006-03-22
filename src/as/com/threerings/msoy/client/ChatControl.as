package com.threerings.msoy.client {

import mx.containers.HBox;

import mx.controls.Button;
import mx.controls.TextInput;

import mx.events.FlexEvent;

public class ChatControl extends HBox
{
    public function ChatControl (ctx :MsoyContext)
    {
        _ctx = ctx;

        addChild(_txt = new TextInput());
        var but :Button = new Button();
        but.label = "Send"; // TODO: xlate
        addChild(but);

        _txt.addEventListener(FlexEvent.ENTER, sendChat);
        but.addEventListener(FlexEvent.BUTTON_DOWN, sendChat);
    }

    protected function sendChat (event :FlexEvent) :void
    {
        var message :String = _txt.text;
        _txt.text = "";
        _ctx.getChatDirector().requestChat(null, message, true);
    }

    /** Our client-side context. */
    protected var _ctx :MsoyContext;

    protected var _txt :TextInput;
}
}
