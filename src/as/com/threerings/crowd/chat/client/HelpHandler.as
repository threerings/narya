//
// $Id$
//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2007 Three Rings Design, Inc., All Rights Reserved
// http://www.threerings.net/code/narya/
//
// This library is free software; you can redistribute it and/or modify it
// under the terms of the GNU Lesser General Public License as published
// by the Free Software Foundation; either version 2.1 of the License, or
// (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA

package com.threerings.crowd.chat.client {

import com.threerings.util.Map;
import com.threerings.util.MessageBundle;
import com.threerings.util.StringUtil;

import com.threerings.crowd.util.CrowdContext;

import com.threerings.crowd.chat.data.ChatCodes;

public class HelpHandler extends CommandHandler
{
    override public function handleCommand (
            ctx :CrowdContext, speakSvc :SpeakService,
            cmd :String, args :String, history :Array) :String
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

        var chatdir :ChatDirector = ctx.getChatDirector();

        // handle "/help help" and "/help boguscmd"
        var possibleCmds :Map = chatdir.getCommandHandlers(hcmd);
        if ((hcmd === "help") || (possibleCmds.size() == 0)) {
            possibleCmds = chatdir.getCommandHandlers("");
            possibleCmds.remove("help"); // remove help from the list
        }

        switch (possibleCmds.size()) {
        case 1:
            chatdir.displayFeedback(null, "m.usage_" + possibleCmds.keys()[0]);
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
}
}
