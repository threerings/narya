package com.threerings.crowd.chat.client {

import com.threerings.crowd.data.BodyObject;

/**
 * Used to implement a slash command (e.g. <code>/who</code>).
 */
public /* abstract */ class CommandHandler
{
    /**
     * Handles the specified chat command.
     *
     * @param speakSvc an optional SpeakService object representing
     * the object to send the chat message on.
     * @param command the slash command that was used to invoke this
     * handler (e.g. <code>/tell</code>).
     * @param args the arguments provided along with the command (e.g.
     * <code>Bob hello</code>) or <code>null</code> if no arguments
     * were supplied.
     * @param history an in/out parameter that allows the command to
     * modify the text that will be appended to the chat history. If
     * this is set to null, nothing will be appended.
     *
     * @return an untranslated string that will be reported to the
     * chat box to convey an error response to the user, or {@link
     * ChatCodes#SUCCESS}.
     */
    public function handleCommand (
            speakSvc :SpeakService, cmd :String, args :String, history :Array)
            :String
    {
        throw new Error("abstract");
    }

    /**
     * Returns true if this user should have access to this chat
     * command.
     */
    public function checkAccess (user :BodyObject) :Boolean
    {
        return true;
    }
}
}
