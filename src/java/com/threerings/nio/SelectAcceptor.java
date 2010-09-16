package com.threerings.nio;

import java.util.Map;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.google.common.primitives.Ints;

import com.samskivert.util.StringUtil;

import com.threerings.nio.SelectorIterable.SelectFailureHandler;

/**
 * Base class for binding to ports with a selector and processing events on that selector.
 */
public abstract class SelectAcceptor
{
    public SelectAcceptor (SelectFailureHandler failureHandler, String bindHostname, int[] ports)
        throws IOException
    {
        Preconditions.checkNotNull(ports, "Ports must be non-null.");

        _bindHostname = bindHostname;
        _ports = ports;

        _selectorSelector = new SelectorIterable(_selector, 100, failureHandler);
    }

    /**
     * Checks the selector for ready keys and passes any through to the handlers.
     */
    public void tick (long when)
    {
        for (SelectionKey key : _selectorSelector) {
            _handlers.get(key).handle(when);
        }
    }

    public Iterable<Integer> getPorts()
    {
        return Ints.asList(_ports);
    }

    /** Helper function for creating proper bindable socket addresses. */
    protected InetSocketAddress getAddress (int port)
    {
        return StringUtil.isBlank(_bindHostname) ?
            new InetSocketAddress(port) : new InetSocketAddress(_bindHostname, port);
    }

    protected interface SelectionHandler {
        void handle(long when);
    }

    protected final int[] _ports;
    protected final String _bindHostname;
    protected final Map<SelectionKey, SelectionHandler> _handlers = Maps.newHashMap();
    protected final Selector _selector = Selector.open();
    protected final SelectorIterable _selectorSelector;
}
