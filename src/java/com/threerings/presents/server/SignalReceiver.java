package com.threerings.presents.server;

/**
 * Listens for Unix signals captured by {@link AbstractSignalHandler}.
 */
public interface SignalReceiver
{

    /**
     * Used to mark the receiver for the USR1 signal in guice injection. Bind implementations that
     * should receive this signal with
     * <code>annotatedWith(Names.named(SignalReceiver.USR1))</code>.
     */
    public static final String USR1 = "usr1";
    /**
     * Used to mark the receiver for the USR2 signal in guice injection. Bind implementations that
     * should receive this signal with
     * <code>annotatedWith(Names.named(SignalReceiver.USR2))</code>.
     */
    public static final String USR2 = "usr2";

    void received();
}
