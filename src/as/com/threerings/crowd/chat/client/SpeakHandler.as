package com.threerings.crowd.chat.client {

import com.threerings.util.StringUtil;

import com.threerings.crowd.util.CrowdContext;

import com.threerings.crowd.chat.data.ChatCodes;

public class SpeakHandler extends CommandHandler
{
    override public function handleCommand (
            ctx :CrowdContext, speakSvc :SpeakService,
            cmd :String, args :String, history :Array) :String
    {
        if (StringUtil.isBlank(args)) {
            return "m.usage_speak";
        }
        history[0] = cmd + " ";
        return ctx.getChatDirector().requestChat(null, args, true);
    }
}
}
