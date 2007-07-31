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

package com.threerings.crowd.chat.server;

import com.threerings.util.MessageManager;

import com.threerings.presents.data.ClientObject;
import com.threerings.presents.dobj.DObject;
import com.threerings.presents.server.InvocationManager;

import com.threerings.crowd.Log;
import com.threerings.crowd.chat.data.ChatCodes;
import com.threerings.crowd.data.BodyObject;

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
         * Should return true if the supplied speaker is allowed to speak
         * via the speak provider with which this validator was
         * registered.
         */
        public boolean isValidSpeaker (DObject speakObj, ClientObject speaker, byte mode);
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
        _speakObj = speakObj;
        _validator = validator;
    }

    // from interface SpeakProvider
    public void speak (ClientObject caller, String message, byte mode)
    {
        // ensure that the caller has normal chat privileges
        BodyObject source = (BodyObject)caller;
        String errmsg = source.checkAccess(ChatCodes.CHAT_ACCESS, null);
        if (errmsg != null) {
            // we normally don't listen for responses to speak messages so we can't just throw an
            // InvocationException we have to specifically communicate the error to the user
            SpeakUtil.sendFeedback(source, MessageManager.GLOBAL_BUNDLE, errmsg);
            return;
        }

        // TODO: broadcast should be handled more like a system message rather than as a mode for a
        // user message so that we don't have to do this validation here. Or not.

        // ensure that the speaker is valid
        if ((mode == ChatCodes.BROADCAST_MODE) ||
            (_validator != null && !_validator.isValidSpeaker(_speakObj, caller, mode))) {
            Log.warning("Refusing invalid speak request [caller=" + caller.who() +
                        ", speakObj=" + _speakObj.which() +
                        ", message=" + message + ", mode=" + mode + "].");

        } else {
            // issue the speak message on our speak object
            SpeakUtil.sendSpeak(_speakObj, source.getVisibleName(), null, message, mode);
        }
    }

    /** Our speech object. */
    protected DObject _speakObj;

    /** The entity that will validate our speakers. */
    protected SpeakerValidator _validator;
}
