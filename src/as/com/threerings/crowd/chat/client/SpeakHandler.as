package com.threerings.crowd.chat.client {

import com.threerings.util.StringUtil;

import com.threerings.crowd.chat.data.ChatCodes;

public class SpeakHandler extends CommandHandler
{
    public function SpeakHandler (chatdir :ChatDirector)
    {
        _chatdir = chatdir;
    }

    public override function handleCommand (
            speakSvc :SpeakService, cmd :String, args :String, history :Array)
            :String
    {
        if (StringUtil.isBlank(args)) {
            return "m.usage_speak";
        }
        history[0] = cmd + " ";
        return _chatdir.requestChat(null, args, true);
    }

    /** Our ChatDirector. */
    protected var _chatdir :ChatDirector;
}
}
