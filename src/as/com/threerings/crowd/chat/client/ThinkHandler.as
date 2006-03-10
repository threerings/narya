package com.threerings.crowd.chat.client {

import com.threerings.util.StringUtil;

import com.threerings.crowd.chat.data.ChatCodes;

public class ThinkHandler extends CommandHandler
{
    public function ThinkHandler (chatdir :ChatDirector)
    {
        _chatdir = chatdir;
    }

    public override function handleCommand (
            speakSvc :SpeakService, cmd :String, args :String, history :Array)
            :String
    {
        if (StringUtil.isBlank(args)) {
            return "m.usage_think";
        }
        history[0] = cmd + " ";
        return _chatdir.deliverChat(speakSvc, args, ChatCodes.THINK_MODE);
    }

    /** Our ChatDirector. */
    protected var _chatdir :ChatDirector;
}
}
