package com.threerings.crowd.chat.client {

import com.threerings.crowd.chat.data.ChatCodes;

public class ClearHandler extends CommandHandler
{
    public function ClearHandler (chatdir :ChatDirector)
    {
        _chatdir = chatdir;
    }

    public override function handleCommand (
            speakSvc :SpeakService, cmd :String, args :String, history :Array)
            :String
    {
        _chatdir.clearDisplays();
        return ChatCodes.SUCCESS;
    }

    /** Our ChatDirector. */
    protected var _chatdir :ChatDirector;
}
}
