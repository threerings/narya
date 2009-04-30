//
// $Id$

package com.threerings.presents.dobj {

/**
 * One listener that can adapt to all the standard events.
 */
public class EventAdapter
    implements EventListener
{
    /** May be assigned a function to receive AttributeChangedEvents. */
    public var attributeChanged :Function;

    /** May be assigned a function to receive EntryAddedEvents. */
    public var entryAdded :Function;

    /** May be assigned a function to receive EntryUpdatedEvents. */
    public var entryUpdated :Function;

    /** May be assigned a function to receive EntryRemovedEvents. */
    public var entryRemoved :Function;

    /** May be assigned a function to receive ElementUpdatedEvents. */
    public var elementUpdated :Function;

    /** May be assigned a function to receive MessageEvents. */
    public var messageReceived :Function;

    /** May be assigned a function to receive ObjectAddedEvents. */
    public var objectAdded :Function;

    /** May be assigned a function to receive ObjectRemovedEvents. */
    public var objectRemoved :Function;

    /** May be assigned a function to receive ObjectDestroyedEvents. */
    public var objectDestroyed :Function;

    // from EventListener
    public function eventReceived (event :DEvent) :void
    {
        if (event is AttributeChangedEvent) {
            if (attributeChanged != null) {
                attributeChanged(AttributeChangedEvent(event));
            }

        } else if (event is EntryAddedEvent) {
            if (entryAdded != null) {
                entryAdded(EntryAddedEvent(event));
            }

        } else if (event is EntryUpdatedEvent) {
            if (entryUpdated != null) {
                entryUpdated(EntryUpdatedEvent(event));
            }

        } else if (event is EntryRemovedEvent) {
            if (entryRemoved != null) {
                entryRemoved(EntryRemovedEvent(event));
            }

        } else if (event is ElementUpdatedEvent) {
            if (elementUpdated != null) {
                elementUpdated(ElementUpdatedEvent(event));
            }

        } else if (event is MessageEvent) {
            if (messageReceived != null) {
                messageReceived(MessageEvent(event));
            }

        } else if (event is ObjectAddedEvent) {
            if (objectAdded != null) {
                objectAdded(ObjectAddedEvent(event));
            }

        } else if (event is ObjectRemovedEvent) {
            if (objectRemoved != null) {
                objectRemoved(ObjectRemovedEvent(event));
            }

        } else if (event is ObjectDestroyedEvent) {
            if (objectDestroyed != null) {
                objectDestroyed(ObjectDestroyedEvent(event));
            }
        }
    }
}
}
