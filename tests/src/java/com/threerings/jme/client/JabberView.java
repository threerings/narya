//
// $Id$

package com.threerings.jme.client;

import com.jmex.bui.BLabel;
import com.jmex.bui.BWindow;
import com.jmex.bui.layout.BorderLayout;

import com.threerings.crowd.client.PlaceView;
import com.threerings.crowd.data.PlaceObject;
import com.threerings.crowd.util.CrowdContext;

import com.threerings.jme.JmeContext;
import com.threerings.jme.chat.ChatView;

/**
 * Manages the "view" when we're in the chat room.
 */
public class JabberView extends BWindow
    implements PlaceView
{
    public JabberView (CrowdContext ctx)
    {
        super(JabberApp.stylesheet, new BorderLayout());

        _ctx = ctx;
        _jctx = (JmeContext)ctx;
        _chat = new ChatView(_jctx, _ctx.getChatDirector());
        add(_chat, BorderLayout.CENTER);

        _jctx.getRootNode().addWindow(this);
        setBounds(0, 40, 640, 240);
    }

    // documentation inherited from interface PlaceView
    public void willEnterPlace (PlaceObject plobj)
    {
        _chat.willEnterPlace(plobj);
    }

    // documentation inherited from interface PlaceView
    public void didLeavePlace (PlaceObject plobj)
    {
        _chat.didLeavePlace(plobj);
    }

    protected CrowdContext _ctx;
    protected JmeContext _jctx;
    protected ChatView _chat;
}
