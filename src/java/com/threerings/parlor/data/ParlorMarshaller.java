//
// $Id: ParlorMarshaller.java,v 1.1 2002/08/14 19:07:53 mdb Exp $

package com.threerings.parlor.data;

import com.threerings.parlor.client.ParlorService;
import com.threerings.parlor.client.ParlorService.InviteListener;
import com.threerings.parlor.client.ParlorService.TableListener;
import com.threerings.parlor.game.GameConfig;
import com.threerings.presents.client.Client;
import com.threerings.presents.client.InvocationService.InvocationListener;
import com.threerings.presents.data.InvocationMarshaller;
import com.threerings.presents.dobj.InvocationResponseEvent;

/**
 * Provides the implementation of the {@link ParlorService} interface
 * that marshalls the arguments and delivers the request to the provider
 * on the server. Also provides an implementation of the response listener
 * interfaces that marshall the response arguments and deliver them back
 * to the requesting client.
 */
public class ParlorMarshaller extends InvocationMarshaller
    implements ParlorService
{
    // documentation inherited
    public static class InviteMarshaller extends ListenerMarshaller
        implements InviteListener
    {
        /** The method id used to dispatch {@link #inviteReceived}
         * responses. */
        public static final int INVITE_RECEIVED = 0;

        // documentation inherited from interface
        public void inviteReceived (int arg1)
        {
            omgr.postEvent(new InvocationResponseEvent(
                               callerOid, requestId, INVITE_RECEIVED,
                               new Object[] { new Integer(arg1) }));
        }

        // documentation inherited
        public void dispatchResponse (int methodId, Object[] args)
        {
            switch (methodId) {
            case INVITE_RECEIVED:
                ((InviteListener)listener).inviteReceived(
                    ((Integer)args[0]).intValue());
                return;

            default:
                super.dispatchResponse(methodId, args);
            }
        }
    }

    // documentation inherited
    public static class TableMarshaller extends ListenerMarshaller
        implements TableListener
    {
        /** The method id used to dispatch {@link #tableCreated}
         * responses. */
        public static final int TABLE_CREATED = 0;

        // documentation inherited from interface
        public void tableCreated (int arg1)
        {
            omgr.postEvent(new InvocationResponseEvent(
                               callerOid, requestId, TABLE_CREATED,
                               new Object[] { new Integer(arg1) }));
        }

        // documentation inherited
        public void dispatchResponse (int methodId, Object[] args)
        {
            switch (methodId) {
            case TABLE_CREATED:
                ((TableListener)listener).tableCreated(
                    ((Integer)args[0]).intValue());
                return;

            default:
                super.dispatchResponse(methodId, args);
            }
        }
    }

    /** The method id used to dispatch {@link #invite} requests. */
    public static final int INVITE = 1;

    // documentation inherited from interface
    public void invite (Client arg1, String arg2, GameConfig arg3, InviteListener arg4)
    {
        InviteMarshaller listener4 = new InviteMarshaller();
        listener4.listener = arg4;
        sendRequest(arg1, INVITE, new Object[] {
            arg2, arg3, listener4
        });
    }

    /** The method id used to dispatch {@link #respond} requests. */
    public static final int RESPOND = 2;

    // documentation inherited from interface
    public void respond (Client arg1, int arg2, int arg3, Object arg4, InvocationListener arg5)
    {
        ListenerMarshaller listener5 = new ListenerMarshaller();
        listener5.listener = arg5;
        sendRequest(arg1, RESPOND, new Object[] {
            new Integer(arg2), new Integer(arg3), arg4, listener5
        });
    }

    /** The method id used to dispatch {@link #cancel} requests. */
    public static final int CANCEL = 3;

    // documentation inherited from interface
    public void cancel (Client arg1, int arg2, InvocationListener arg3)
    {
        ListenerMarshaller listener3 = new ListenerMarshaller();
        listener3.listener = arg3;
        sendRequest(arg1, CANCEL, new Object[] {
            new Integer(arg2), listener3
        });
    }

    /** The method id used to dispatch {@link #createTable} requests. */
    public static final int CREATE_TABLE = 4;

    // documentation inherited from interface
    public void createTable (Client arg1, int arg2, GameConfig arg3, TableListener arg4)
    {
        TableMarshaller listener4 = new TableMarshaller();
        listener4.listener = arg4;
        sendRequest(arg1, CREATE_TABLE, new Object[] {
            new Integer(arg2), arg3, listener4
        });
    }

    /** The method id used to dispatch {@link #joinTable} requests. */
    public static final int JOIN_TABLE = 5;

    // documentation inherited from interface
    public void joinTable (Client arg1, int arg2, int arg3, int arg4, InvocationListener arg5)
    {
        ListenerMarshaller listener5 = new ListenerMarshaller();
        listener5.listener = arg5;
        sendRequest(arg1, JOIN_TABLE, new Object[] {
            new Integer(arg2), new Integer(arg3), new Integer(arg4), listener5
        });
    }

    /** The method id used to dispatch {@link #leaveTable} requests. */
    public static final int LEAVE_TABLE = 6;

    // documentation inherited from interface
    public void leaveTable (Client arg1, int arg2, int arg3, InvocationListener arg4)
    {
        ListenerMarshaller listener4 = new ListenerMarshaller();
        listener4.listener = arg4;
        sendRequest(arg1, LEAVE_TABLE, new Object[] {
            new Integer(arg2), new Integer(arg3), listener4
        });
    }

    // Class file generated on 00:26:01 08/11/02.
}
