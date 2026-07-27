//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2025 Three Rings Design, Inc., All Rights Reserved
// https://github.com/threerings/narya/blob/master/LICENSE

package com.threerings.presents.dobj;

import java.util.function.Predicate;

/**
 * Used to validate distributed object subscription requests and event
 * dispatches.
 *
 * @see DObject#setAccessController
 */
public interface AccessController
{
    /**
     * A default access controller implementation that allows for no subscribing.
     */
    public static class Default
        implements AccessController
    {
        public boolean allowSubscribe (DObject object, Subscriber<?> subscriber) {
            return false;
        }

        public boolean allowDispatch (DObject object, DEvent event)
        {
            return event.getSourceOid() == -1;
        }
    }

    /**
     * Should return true if the supplied subscriber is allowed to
     * subscribe to the specified object.
     */
    boolean allowSubscribe (DObject object, Subscriber<?> subscriber);

    /**
     * Should return true if the supplied event is legal for dispatch on
     * the specified distributed object.
     */
    boolean allowDispatch (DObject object, DEvent event);

    /**
     * Compose a new AccessController that is like this one but ALSO allows subscribers
     * that satisfy the predicate.
     */
    default AccessController orSubscribe (Predicate<Subscriber<?>> canSubscribe) {
        final AccessController ac = this;
        return new AccessController() {
            public boolean allowSubscribe (DObject object, Subscriber<?> subscriber) {
                return canSubscribe.test(subscriber) || ac.allowSubscribe(object, subscriber);
            }
            public boolean allowDispatch (DObject object, DEvent event) {
                return ac.allowDispatch(object, event);
            }
        };
    }

    /**
     * Compose a new AccessController that is like this one but ALSO allows event dispatches
     * that satisfy the predicate.
     */
    default AccessController orDispatch (Predicate<DEvent> canDispatch) {
        final AccessController ac = this;
        return new AccessController() {
            public boolean allowSubscribe (DObject object, Subscriber<?> subscriber) {
                return ac.allowSubscribe(object, subscriber);
            }
            public boolean allowDispatch (DObject object, DEvent event) {
                return canDispatch.test(event) || ac.allowDispatch(object, event);
            }
        };
    }
}

