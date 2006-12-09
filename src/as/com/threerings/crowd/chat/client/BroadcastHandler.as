package com.threerings.crowd.chat.client {

import com.threerings.util.Name;
import com.threerings.util.ResultAdapter;
import com.threerings.util.ResultListener;
import com.threerings.util.StringUtil;

import com.threerings.crowd.data.BodyObject;
import com.threerings.crowd.util.CrowdContext;
import com.threerings.crowd.chat.data.ChatCodes;

public class BroadcastHandler extends CommandHandler
{
    override public function handleCommand (
        ctx :CrowdContext, speakSvc :SpeakService,
        cmd :String, args :String, history :Array) :String
    {
        if (StringUtil.isBlank(args)) {
            return "m.usage_broadcast";
        }

        // mogrify and verify length
        var chatdir :ChatDirector = ctx.getChatDirector();
        args = chatdir.mogrifyChat(args);
        var err :String = chatdir.checkLength(args);
        if (err != null) {
            return err;
        }

        // do the broadcast
        chatdir.requestBroadcast(args);

        history[0] = cmd + " ";
        return ChatCodes.SUCCESS;
    }

    override public function checkAccess (user :BodyObject) :Boolean
    {
        return (null == user.checkAccess(ChatCodes.BROADCAST_ACCESS, null));
    }
}
}
