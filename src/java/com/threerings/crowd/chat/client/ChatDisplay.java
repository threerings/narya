//
// $Id: ChatDisplay.java,v 1.6 2001/12/17 01:06:34 mdb Exp $

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
     * auxilliary chat object, the type code provided when that auxilliary
     * object was registered.
     * @param speaker the username of the speaker.
     * @param message the text of the message.
     */
    public void displaySpeakMessage (
        String type, String speaker, String message);

    /**
     * Called to display a tell message. A tell message is one that is
     * delivered directly from one user to another regardless of their
     * location in the system.
     *
     * @param speaker the username of the speaker.
     * @param message the text of the message.
     */
    public void displayTellMessage (String speaker, String message);

    /**
     * Called to display a system message. A system message is one that is
     * broadcast to all occupants of a particular place and rather than
     * originating from some other user in the place, originates from the
     * server and should be displayed visually differently from speak
     * messages.
     *
     * @param type {@link ChatCodes#PLACE_CHAT_TYPE} for a speak message
     * delivered via the place object, or for messages delivered via an
     * auxilliary chat object, the type code provided when that auxilliary
     * object was registered.
     * @param message the text of the message.
     */
    public void displaySystemMessage (String type, String message);

    /**
     * Called in response to a chat request (either speak or tell) that
     * originated on this client. The request id supplied will match the
     * one returned when the request was generated and the status will
     * either indicate success (by being equal to <code>SUCCESS</code>) or
     * failure (in which case it will contain a message failure code which
     * can be converted into a displayable string).
     *
     * <p> A chat display should track outstanding chat requests so that
     * it can properly handle the response when it arrives.
     *
     * @param reqid the request id of the request that resulted in this
     * response.
     * @param status the message code indicating whether the chat request
     * was successful or not.
     *
     * @see ChatDirector#requestSpeak
     * @see ChatDirector#requestTell
     */
    public void handleResponse (int reqid, String status);
}
