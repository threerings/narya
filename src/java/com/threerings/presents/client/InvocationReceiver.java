//
// $Id: InvocationReceiver.java,v 1.1 2001/07/19 19:18:06 mdb Exp $

package com.threerings.cocktail.cher.client;

/**
 * Classes registered to process invocation notifications should extend
 * the invocation receiver class and register themselves with the
 * invocation manager. Because the invocation notification procedures are
 * looked up using reflection, there are no methods to override in the
 * receiver class, but it serves as a useful point for documentation and
 * as a useful indicator that the derived class in question is actually
 * serving as an invocation receiver.
 *
 * <p> Invocation notifications are identified by a module name and a
 * procedure name. The module name identifies which invocation receiver
 * instance will receive the notification. Receivers are registered with
 * the invocation manager as handling all notification procedures for a
 * particular module. The notification procedure name is used to construct
 * a method name which is then reflected and invoked. The name
 * construction is as follows: a notification message requesting the
 * invocation of a procedure named <code>Tell</code> will result in a
 * method named <code>handleTellNotification</code> being invoked on the
 * invocation receiver instance. The signature of that method is defined
 * by the arguments supplied with the invocation notification message.
 * These arguments must always be of the same type and must exactly match
 * the signature of the implementing method (with the standard reflection
 * argument type conversion process taken into account).
 *
 * @see InvocationManager#registerReceiver
 */
public class InvocationReceiver
{
}
