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

import com.threerings.util.Name;
import com.threerings.util.ResultAdapter;
import com.threerings.util.ResultListener;
import com.threerings.util.StringUtil;

import com.threerings.crowd.data.BodyObject;
import com.threerings.crowd.util.CrowdContext;
import com.threerings.crowd.chat.data.ChatCodes;

public class TellHandler extends CommandHandler
{
    override public function handleCommand (
            ctx :CrowdContext, speakSvc :SpeakService,
            cmd :String, args :String, history :Array) :String
    {
        if (StringUtil.isBlank(args)) {
            return "m.usage_tell";
        }

        var chatdir :ChatDirector = ctx.getChatDirector();

        var useQuotes :Boolean = (args.charAt(0) == "\"");
        var bits :Array = parseTell(args);
        var handle :String = (bits[0] as String);
        var message :String = (bits[1] as String);

        // validate that we didn't eat all the tokens making the handle
        if (StringUtil.isBlank(message)) {
            return "m.usage_tell";
        }

        // make sure we're not trying to tell something to ourselves
        var self :BodyObject =
            (ctx.getClient().getClientObject() as BodyObject);
        if (handle.toLowerCase() ===
                self.getVisibleName().toString().toLowerCase()) {
            return "m.talk_self";
        }

        // and lets just give things an opportunity to sanitize the name
        var target :Name = normalizeAsName(handle);

        // mogrify the chat
        message = chatdir.mogrifyChat(message);
        var err :String = chatdir.checkLength(message);
        if (err != null) {
            return err;
        }

        // clear out from the history any tells that are mistypes
        var fullHist :Array = chatdir.accessHistory();
        for (var ii :int = fullHist.length - 1; ii >= 0; ii--) {
            var hist :String = (fullHist[ii] as String);
            if (0 == hist.indexOf("/" + cmd)) {
                var harg :String = StringUtil.trim(
                    hist.substring(cmd.length + 1));
                // we blow away any historic tells that have msg content
                if (!StringUtil.isBlank(parseTell(harg)[1] as String)) {
                    fullHist.splice(ii, 1);
                }
            }
        }

        // store the full command in the history, even if it was mistyped
        var histEntry :String = cmd + " " +
            (useQuotes ? ("\"" + target + "\"") : target.toString()) +
            " " + message;
        history[0] = histEntry;

        var rl :ResultListener = new ResultAdapter(
            function (result :Object) :void {

                // replace the full one in the history with just:
                // /tell "<handle>"
                var newEntry :String = "/" + cmd + " " +
                    (useQuotes ? ("\"" + result + "\"")
                               : result.toString()) + " ";
                var fullHist :Array = chatdir.accessHistory();
                var dex :int = fullHist.indexOf(newEntry);
                if (dex != -1) {
                    fullHist.splice(dex, 1);
                }
                var del :int = fullHist.lastIndexOf("/" + histEntry);
                if (del >= 0) {
                    fullHist[del] = newEntry;
                } else {
                    fullHist.push(newEntry);
                }
            }, null);
        chatdir.requestTell(target, message, rl);

        return ChatCodes.SUCCESS;
    }

    /**
     * Parse the tell into two strings, handle and message. If either
     * one is null then the parsing did not succeed.
     */
    protected function parseTell (args :String) :Array
    {
        var handle :String;
        var message :String;

        if (args.charAt(0) == "\"") {
            var nextQuote :int = args.indexOf("\"", 1);
            if (nextQuote == -1 || nextQuote == 1) {
                handle = message = null; // bogus parsing

            } else {
                handle = StringUtil.trim(args.substring(1, nextQuote));
                message = StringUtil.trim(args.substring(nextQuote + 1));
            }

        } else {
            var idx :int = args.search(/\s/);
            if (idx == -1) {
                handle = args;
                message = "";

            } else {
                handle = StringUtil.trim(args.substring(0, idx));
                message = StringUtil.trim(args.substring(idx));
            }
        }

        return [ handle, message ];
    }

    /**
     * Turn the user-entered string into a Name object, doing
     * any particular normalization we want to do along the way
     * so that "/tell Bob" and "/tell BoB" don't both show up in history.
     */
    protected function normalizeAsName (handle :String) :Name
    {
        return new Name(handle);
    }

    /**
     * Escape or otherwise do any final processing on the message
     * prior to sending it.
     */
    protected function escapeMessage (msg :String) :String
    {
        return msg;
    }
}
}
