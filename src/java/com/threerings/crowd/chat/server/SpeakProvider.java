//
// $Id: SpeakProvider.java,v 1.1 2002/08/14 19:07:49 mdb Exp $

package com.threerings.crowd.chat;

import com.threerings.presents.data.ClientObject;
import com.threerings.presents.dobj.DObject;
import com.threerings.presents.dobj.MessageEvent;
import com.threerings.presents.server.InvocationProvider;

import com.threerings.crowd.Log;
import com.threerings.crowd.data.BodyObject;

/**
 * Provides the back-end of the chat speaking facilities. A server entity
 * can make "speech" available among the subscribers of a particular
 * distributed object by constructing a speak provider and registering it
 * with the invocation manager, then placing the resulting marshaller into
 * the distributed object in question so that subscribers to that object
 * can use it to generate "speak" requests on that object.
 */
public class SpeakProvider
    implements InvocationProvider, ChatCodes
{
    /**
     * Used to prevent abitrary users from issuing speak requests.
     */
    public static interface SpeakerValidator
    {
        /**
         * Should return true if the supplied speaker is allowed to speak
         * via the speak provider with which this validator was
         * registered.
         */
        public boolean isValidSpeaker (DObject speakObj, ClientObject speaker);
    }

    /**
     * Creates a speak provider that will provide speech on the supplied
     * distributed object.
     *
     * @param speakObj the object for which speech requests will be
     * processed.
     * @param validator an optional validator that can be used to prevent
     * arbitrary users from using the speech services on this object.
     */
    public SpeakProvider (DObject speakObj, SpeakerValidator validator)
    {
        _speakObj = speakObj;
        _validator = validator;
    }

    /**
     * Handles a {@link SpeakService#speak} request.
     */
    public void speak (ClientObject caller, String message, byte mode)
    {
        // ensure that the speaker is valid
        if (!_validator.isValidSpeaker(_speakObj, caller)) {
            Log.warning("Refusing invalid speak request " +
                        "[caller=" + caller.who() +
                        ", speakObj=" + _speakObj.which() +
                        ", message=" + message + ", mode=" + mode + "].");

        } else {
            // issue the speak message on our speak object
            sendSpeak(_speakObj, ((BodyObject)caller).username,
                      null, message, mode);
        }
    }

    /**
     * Sends a speak notification to the specified place object
     * originating with the specified speaker (the speaker optionally
     * being a server entity that wishes to fake a "speak" message) and
     * with the supplied message content.
     *
     * @param speakObj the object on which to generate the speak message.
     * @param speaker the username of the user that generated the message
     * (or some special speaker name for server messages).
     * @param bundle null when the message originates from a real human,
     * the bundle identifier that will be used by the client to translate
     * the message text when the message originates from a server entity
     * "faking" a chat message.
     * @param message the text of the speak message.
     */
    public static void sendSpeak (DObject speakObj, String speaker,
                                  String bundle, String message)
    {
        sendSpeak(speakObj, speaker, bundle, message, ChatCodes.DEFAULT_MODE);
    }

    /**
     * Sends a speak notification to the specified place object
     * originating with the specified speaker (the speaker optionally
     * being a server entity that wishes to fake a "speak" message) and
     * with the supplied message content.
     *
     * @param speakObj the object on which to generate the speak message.
     * @param speaker the username of the user that generated the message
     * (or some special speaker name for server messages).
     * @param bundle null when the message originates from a real human,
     * the bundle identifier that will be used by the client to translate
     * the message text when the message originates from a server entity
     * "faking" a chat message.
     * @param message the text of the speak message.
     * @param mode the mode of the message, see {@link
     * ChatCodes#DEFAULT_MODE}.
     */
    public static void sendSpeak (
        DObject speakObj, String speaker,
        String bundle, String message, byte mode)
    {
        Object[] outargs = null;
        if (bundle == null) {
            outargs = new Object[] { speaker, message, new Byte(mode) };
        } else {
            outargs = new Object[] { speaker, bundle, message, new Byte(mode) };
        }
        speakObj.postEvent(
            new MessageEvent(speakObj.getOid(), SPEAK_NOTIFICATION, outargs));
    }

    /**
     * Sends a system message notification to the specified object with
     * the supplied message content. A system message is one that will be
     * rendered where the speak messages are rendered, but in a way that
     * makes it clear that it is a message from the server.
     *
     * @param speakObj the object on which to deliver the message.
     * @param bundle the name of the localization bundle that should be
     * used to translate this system message prior to displaying it to the
     * client.
     * @param message the text of the message.
     */
    public static void sendSystemSpeak (
        DObject speakObj, String bundle, String message)
    {
        speakObj.postEvent(
            new MessageEvent(speakObj.getOid(), SYSTEM_NOTIFICATION,
                             new Object[] { bundle, message }));
    }

    /** Our speech object. */
    protected DObject _speakObj;

    /** The entity that will validate our speakers. */
    protected SpeakerValidator _validator;
}
