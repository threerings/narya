//
// $Id: ChatPanel.java,v 1.4 2001/10/11 04:13:33 mdb Exp $

package com.threerings.micasa.client;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;
import javax.swing.text.*;
import java.util.StringTokenizer;

import com.samskivert.swing.*;
import com.threerings.crowd.chat.*;
import com.threerings.crowd.client.*;
import com.threerings.crowd.data.OccupantInfo;
import com.threerings.crowd.data.PlaceObject;
import com.threerings.crowd.util.CrowdContext;

import com.threerings.micasa.Log;

public class ChatPanel
    extends JPanel
    implements ActionListener, ChatDisplay, OccupantObserver, PlaceView
{
    public ChatPanel (CrowdContext ctx)
    {
        // keep this around for later
        _ctx = ctx;

        // create our chat director and register ourselves with it
        _chatdtr = new ChatDirector(_ctx);
        _chatdtr.addChatDisplay(this);

        // register as an occupant observer
        _ctx.getOccupantManager().addOccupantObserver(this);

        GroupLayout gl = new VGroupLayout(GroupLayout.STRETCH);
	gl.setOffAxisPolicy(GroupLayout.STRETCH);
	setLayout(gl);

        // create our scrolling chat text display
        _text = new JTextPane();
        _text.setEditable(false);
        add(new JScrollPane(_text));

        // create our styles and add those to the text pane
        createStyles(_text);

        // add a label for the text entry stuff
        add(new JLabel("Type here to chat:"), GroupLayout.FIXED);

        // create a horizontal group for the text entry bar
        gl = new HGroupLayout(GroupLayout.STRETCH);
        JPanel epanel = new JPanel(gl);
        epanel.add(_entry = new JTextField());
        _entry.setActionCommand("send");
        _entry.addActionListener(this);
        _entry.setEnabled(false);

        _send = new JButton("Send");
        _send.setEnabled(false);
        _send.addActionListener(this);
        _send.setActionCommand("send");
        epanel.add(_send, GroupLayout.FIXED);
        add(epanel, GroupLayout.FIXED);

        // focus the chat input field by default
        _entry.requestFocus();
    }

    protected void createStyles (JTextPane text)
    {
        StyleContext sctx = StyleContext.getDefaultStyleContext();
        Style defstyle = sctx.getStyle(StyleContext.DEFAULT_STYLE);

        _nameStyle = text.addStyle("name", defstyle);
        StyleConstants.setForeground(_nameStyle, Color.blue);

        _msgStyle = text.addStyle("msg", defstyle);
        StyleConstants.setForeground(_msgStyle, Color.black);

        _errStyle = text.addStyle("err", defstyle);
        StyleConstants.setForeground(_errStyle, Color.red);

        _noticeStyle = text.addStyle("notice", defstyle);
        StyleConstants.setForeground(_noticeStyle, Color.magenta);
    }

    public void actionPerformed (ActionEvent e)
    {
	String cmd = e.getActionCommand();
	if (cmd.equals("send")) {
            sendText();

	} else {
	    System.out.println("Unknown action event: " + cmd);
	}
    }

    public void occupantEntered (OccupantInfo info)
    {
        displayOccupantMessage("*** " + info.username + " entered.");
    }

    public void occupantLeft (OccupantInfo info)
    {
        displayOccupantMessage("*** " + info.username + " left.");
    }

    public void occupantUpdated (OccupantInfo info)
    {
    }

    protected void displayOccupantMessage (String message)
    {
        // stick a newline on the message
        message = message + "\n";

        Document doc = _text.getDocument();
        try {
            doc.insertString(doc.getLength(), message, _noticeStyle);
        } catch (BadLocationException ble) {
            Log.warning("Unable to insert text!? [error=" + ble + "].");
        }
    }

    protected void sendText ()
    {
        String text = _entry.getText();

        // if the message to send begins with /tell then parse it and
        // generate a tell request rather than a speak request
        if (text.startsWith("/tell")) {
            StringTokenizer tok = new StringTokenizer(text);
            // there should be at least three tokens: '/tell target word'
            if (tok.countTokens() < 3) {
                displayError("Usage: /tell username message");
                return;
            }

            // skip the /tell and grab the username
            tok.nextToken();
            String username = tok.nextToken();

            // now strip off everything up to the username to get the
            // message
            int uidx = text.indexOf(username);
            String message = text.substring(uidx + username.length()).trim();

            // request to send this text as a tell message
            _chatdtr.requestTell(username, message);

        } else {
            // request to send this text as a chat message
            _chatdtr.requestSpeak(text);
        }

        // clear out the input because we sent a request
        _entry.setText("");
    }

    public void displaySpeakMessage (String speaker, String message)
    {
        // wrap the speaker in brackets
        speaker = "<" + speaker + "> ";
        // stick a newline on the message
        message = message + "\n";

        Document doc = _text.getDocument();
        try {
            doc.insertString(doc.getLength(), speaker, _nameStyle);
            doc.insertString(doc.getLength(), message, _msgStyle);
        } catch (BadLocationException ble) {
            Log.warning("Unable to insert text!? [error=" + ble + "].");
        }
    }

    protected void displayError (String message)
    {
        // stick a newline on the message
        message = message + "\n";

        Document doc = _text.getDocument();
        try {
            doc.insertString(doc.getLength(), message, _errStyle);
        } catch (BadLocationException ble) {
            Log.warning("Unable to insert text!? [error=" + ble + "].");
        }
    }

    public void displayTellMessage (String speaker, String message)
    {
        // wrap the speaker in brackets
        speaker = "[" + speaker + " whispers] ";
        // stick a newline on the message
        message = message + "\n";

        Document doc = _text.getDocument();
        try {
            doc.insertString(doc.getLength(), speaker, _nameStyle);
            doc.insertString(doc.getLength(), message, _msgStyle);
        } catch (BadLocationException ble) {
            Log.warning("Unable to insert text!? [error=" + ble + "].");
        }
    }

    public void handleResponse (int reqid, String status)
    {
        if (!status.equals(ChatCodes.SUCCESS)) {
            displayError(status);
        }
    }

    public void willEnterPlace (PlaceObject place)
    {
        Log.info("We be here: " + place);

        // enable our chat input elements since we're now somewhere that
        // we can chat
        _entry.setEnabled(true);
        _send.setEnabled(true);
        _entry.requestFocus();
    }

    public void didLeavePlace (PlaceObject place)
    {
        // nothing doing
    }

    protected CrowdContext _ctx;
    protected ChatDirector _chatdtr;

    protected JComboBox _roombox;
    protected JTextPane _text;
    protected JButton _send;
    protected JTextField _entry;

    protected Style _nameStyle;
    protected Style _msgStyle;
    protected Style _errStyle;
    protected Style _noticeStyle;
}
