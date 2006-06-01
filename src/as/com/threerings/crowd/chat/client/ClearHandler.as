package com.threerings.crowd.chat.client {

import com.threerings.crowd.util.CrowdContext;

import com.threerings.crowd.chat.data.ChatCodes;

public class ClearHandler extends CommandHandler
{
    public override function handleCommand (
            ctx :CrowdContext, speakSvc :SpeakService,
            cmd :String, args :String, history :Array) :String
    {
        ctx.getChatDirector().clearDisplays();
        return ChatCodes.SUCCESS;
    }
}
}
