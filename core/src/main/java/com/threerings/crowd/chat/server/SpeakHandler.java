//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2025 Three Rings Design, Inc., All Rights Reserved
// https://github.com/threerings/narya/blob/master/LICENSE

package com.threerings.crowd.chat.server;

import com.threerings.util.MessageManager;

import com.threerings.presents.data.ClientObject;
import com.threerings.presents.dobj.DObject;
import com.threerings.presents.server.InvocationManager;

import com.threerings.crowd.chat.client.SpeakService;
import com.threerings.crowd.chat.data.ChatCodes;
import com.threerings.crowd.data.BodyObject;
import com.threerings.crowd.server.BodyLocator;

import static com.threerings.crowd.Log.log;

/**
 * Wires up the {@link SpeakService} to a particular distributed object. A server entity can make
 * "speech" available among the subscribers of a particular distributed object by constructing a
 * speak handler and registering it with the {@link InvocationManager}, then placing the resulting
 * marshaller into the distributed object in question so that subscribers to that object can use it
 * to generate "speak" requests on that object.
 */
public class SpeakHandler
    implements SpeakProvider
{
    /**
     * Used to prevent abitrary users from issuing speak requests.
     */
    public static interface SpeakerValidator
    {
        /**
         * Should return true if the supplied speaker is allowed to speak via the speak provider
         * with which this validator was registered.
         */
        boolean isValidSpeaker (DObject speakObj, ClientObject speaker, byte mode);
    }

    /**
     * Creates a handler that will provide speech on the supplied distributed object.
     *
     * @param speakObj the object for which speech requests will be processed.
     * @param validator an optional validator that can be used to prevent arbitrary users from
     * using the speech services on this object.
     */
    public SpeakHandler (DObject speakObj, SpeakerValidator validator)
    {
        this(null, speakObj, validator);
    }

    /**
     * Creates a handler that will provide speech on the supplied distributed object.
     *
     * @param locator the object we use to look up BodyObjects from ClientObjects
     * @param speakObj the object for which speech requests will be processed.
     * @param validator an optional validator that can be used to prevent arbitrary users from
     * using the speech services on this object.
     */
    public SpeakHandler (BodyLocator locator, DObject speakObj, SpeakerValidator validator)
    {
        _locator = locator;
        _speakObj = speakObj;
        _validator = validator;
    }

    // from interface SpeakProvider
    public void speak (ClientObject caller, String message, byte mode)
    {
        // ensure that the caller has normal chat privileges
        BodyObject source = (_locator != null) ? _locator.forClient(caller) :
            (BodyObject) caller;

        String errmsg = source.checkAccess(ChatCodes.CHAT_ACCESS, null);
        if (errmsg != null) {
            // we normally don't listen for responses to speak messages so we can't just throw an
            // InvocationException, we have to specifically communicate the error to the user
            SpeakUtil.sendFeedback(source, MessageManager.GLOBAL_BUNDLE, errmsg);
            return;
        }

        // TODO: broadcast should be handled more like a system message rather than as a mode for a
        // user message so that we don't have to do this validation here. Or not.

        // ensure that the speaker is valid
        if ((mode == ChatCodes.BROADCAST_MODE) ||
            (_validator != null && !_validator.isValidSpeaker(_speakObj, source, mode))) {
            log.warning("Refusing invalid speak request", "source", source.who(),
                        "speakObj", _speakObj.which(), "message", message, "mode", mode);

        } else {
            // issue the speak message on our speak object
            sendSpeak(source, message, mode);
        }
    }

    /**
     * Sends the actual speak message.
     */
    protected void sendSpeak (BodyObject source, String message, byte mode)
    {
        SpeakUtil.sendSpeak(_speakObj, source.getVisibleName(), null, message, mode);
    }

    /** Used for acquiring BodyObject references from Names and ClientObjects. */
    protected BodyLocator _locator;

    /** Our speech object. */
    protected DObject _speakObj;

    /** The entity that will validate our speakers. */
    protected SpeakerValidator _validator;
}
