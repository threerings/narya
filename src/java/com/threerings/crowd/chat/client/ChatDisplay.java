//
// $Id: ChatDisplay.java,v 1.12 2002/07/22 22:54:03 ray Exp $

package com.threerings.crowd.chat;

/**
 * A chat display provides a means by which chat messages can be
 * displayed. The chat display will be notified when chat messages of
 * various sorts have been received by the client.
 */
public interface ChatDisplay
{
    /**
     * Called to display a speak message. A speak message is one that is
     * broadcast to all occupants of a particular place. A speak message
     * originated by this client will result in a subsequent call to this
     * method after the speak message is accepted by the server and
     * broadcast to everyone in the place.
     *
     * @param type {@link ChatCodes#PLACE_CHAT_TYPE} for a speak message
     * delivered via the place object, or for messages delivered via an
     * auxiliary chat object, the type code provided when that auxiliary
     * object was registered.
     * @param speaker the username of the speaker.
     * @param bundle for speak messages that originated with a server
     * entity rather than another client, this will be non-null and will
     * contain a bundle identifier that should be used to translate the
     * message text.
     * @param message the text of the message.
     * @param mode the mode of the speak (@see ChatCodes.DEFAULT_MODE
     *                                    @see ChatCodes.THINK_MODE,
     *                                    @see ChatCodes.EMOTE_MODE ).
     */
    public void displaySpeakMessage (
        String type, String speaker, String bundle, String message,
        byte mode);

    /**
     * Called to display a tell message. A tell message is one that is
     * delivered directly from one user to another regardless of their
     * location in the system.
     *
     * @param speaker the username of the speaker.
     * @param bundle for tell messages that originated with a server
     * entity rather than another client, this will be non-null and will
     * contain a bundle identifier that should be used to translate the
     * message text.
     * @param message the text of the message.
     */
    public void displayTellMessage (
        String speaker, String bundle, String message);

    /**
     * Called to display a system message. A system message is one that is
     * broadcast to all occupants of a particular place and rather than
     * originating from some other user in the place, originates from the
     * server and should be displayed visually differently from speak
     * messages.
     *
     * @param type {@link ChatCodes#PLACE_CHAT_TYPE} for a speak message
     * delivered via the place object, or for messages delivered via an
     * auxiliary chat object, the type code provided when that auxiliary
     * object was registered.
     * @param bundle the bundle identifier to be used when localizing this
     * message for display to the client.
     * @param message the text of the message.
     */
    public void displaySystemMessage (
        String type, String bundle, String message);

    /**
     * Called in response to a successful tell request that originated on
     * this client.  The request id supplied will match the one returned
     * when the request was generated, with the target and message
     * detailing the target user name and tell message text associated
     * with the request.
     *
     * @param reqid the request id of the request that resulted in this
     * response.
     * @param target the name of the user to whom the tell request was
     * dispatched.
     * @param message the text of the message.
     *
     * @see ChatDirector#requestTell
     */
    public void handleTellSucceeded (int reqid, String target, String message);

    /**
     * Called in response to a failed tell request that originated on this
     * client.  The request id supplied will match the one returned when
     * the request was generated, with the target and message detailing
     * the target user name and tell message text associated with the
     * request.
     *
     * @param reqid the request id of the request that resulted in this
     * response.
     * @param reason a translatable string explaining the reason for the
     * tell request failure.
     *
     * @see ChatDirector#requestTell
     */
    public void handleTellFailed (int reqid, String target, String reason);
}
