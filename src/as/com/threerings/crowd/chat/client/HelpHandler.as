package com.threerings.crowd.chat.client {

import com.threerings.util.Map;
import com.threerings.util.MessageBundle;
import com.threerings.util.StringUtil;

import com.threerings.crowd.chat.data.ChatCodes;

public class HelpHandler extends CommandHandler
{
    public function HelpHandler (chatdir :ChatDirector)
    {
        _chatdir = chatdir;
    }

    public override function handleCommand (
            speakSvc :SpeakService, cmd :String, args :String, history :Array)
            :String
    {
        var hcmd :String = "";

        // grab the command they want help on
        if (!StringUtil.isBlank(args)) {
            hcmd = args;
            var sidx :int = args.indexOf(" ");
            if (sidx != -1) {
                hcmd = args.substring(0, sidx);
            }
        }

        // let the user give commands with or without the /
        if (hcmd.charAt(0) == "/") {
            hcmd = hcmd.substring(1);
        }

        // handle "/help help" and "/help boguscmd"
        var possibleCmds :Map = _chatdir.getCommandHandlers(hcmd);
        if ((hcmd === "help") || (possibleCmds.size() == 0)) {
            possibleCmds = _chatdir.getCommandHandlers("");
            possibleCmds.remove("help"); // remove help from the list
        }

        switch (possibleCmds.size()) {
        case 1:
            _chatdir.displayFeedback(null, "m.usage_" + possibleCmds.keys()[0]);
            return ChatCodes.SUCCESS;

        default:
            var cmds :Array = possibleCmds.keys();
            cmds.sort();
            var cmdList :String = "";
            for each (var skey :String in cmds) {
                cmdList += " /" +  skey;
            }
            return MessageBundle.tcompose("m.usage_help", cmdList);
        }
    }

    /** Our ChatDirector. */
    protected var _chatdir :ChatDirector;
}
}
