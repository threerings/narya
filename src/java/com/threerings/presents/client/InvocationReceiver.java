//
// $Id: InvocationReceiver.java,v 1.4 2001/10/11 04:07:52 mdb Exp $

package com.threerings.presents.client;

/**
 * Classes registered to process invocation notifications should implement
 * the invocation receiver interface and register themselves with the
 * invocation director. Because the invocation notification procedures are
 * looked up using reflection, there are no methods to implement in the
 * receiver interface, but it serves as a useful point for documentation
 * and as a useful indicator that the class in question is serving as an
 * invocation receiver.
 *
 * <p> Invocation notifications are identified by a module name and a
 * procedure name. The module name identifies which invocation receiver
 * instance will receive the notification. Receivers are registered with
 * the invocation director as handling all notification procedures for a
 * particular module. The notification procedure name is used to construct
 * a method name which is then reflected and invoked.
 *
 * <p> The name construction is as follows: a notification message
 * requesting the invocation of a procedure named <code>Tell</code> will
 * result in a method named <code>handleTellNotification</code> being
 * invoked on the invocation receiver instance. The signature of that
 * method is defined by the arguments supplied with the invocation
 * notification message.  These arguments must always be of the same type
 * and must exactly match the signature of the implementing method (with
 * the standard reflection argument type conversion process taken into
 * account).
 *
 * @see InvocationDirector#registerReceiver
 */
public interface InvocationReceiver
{
}
