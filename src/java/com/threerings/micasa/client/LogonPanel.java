//
// $Id: LogonPanel.java,v 1.5 2002/07/12 17:01:28 mdb Exp $

package com.threerings.micasa.client;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import com.samskivert.swing.GroupLayout;
import com.samskivert.swing.HGroupLayout;
import com.samskivert.swing.VGroupLayout;

import com.threerings.util.MessageBundle;

import com.threerings.presents.client.Client;
import com.threerings.presents.client.ClientObserver;
import com.threerings.presents.client.LogonException;
import com.threerings.presents.net.Credentials;
import com.threerings.presents.net.UsernamePasswordCreds;

import com.threerings.micasa.util.MiCasaContext;

public class LogonPanel extends JPanel
    implements ActionListener, ClientObserver
{
    public LogonPanel (MiCasaContext ctx)
    {
        // keep these around for later
        _ctx = ctx;
        _msgs = _ctx.getMessageManager().getBundle("micasa");

	setLayout(new VGroupLayout());

        // stick the logon components into a panel that will stretch them
        // to a sensible width
        JPanel box = new JPanel(
            new VGroupLayout(VGroupLayout.NONE, VGroupLayout.STRETCH,
                             5, VGroupLayout.CENTER)) {
            public Dimension getPreferredSize () {
                Dimension psize = super.getPreferredSize();
                psize.width = Math.max(psize.width, 300);
                return psize;
            }
        };
        add(box);

        // try obtaining our title text from a system property
        String tstr = null;
        try {
            tstr = System.getProperty("logon.title");
        } catch (Throwable t) {
        }
        if (tstr == null) {
            tstr = "Mi Casa!";
        }

        // create a big fat label
        JLabel title = new JLabel(tstr, JLabel.CENTER);
        title.setFont(new Font("Helvetica", Font.BOLD, 24));
        box.add(title);

        // create the username bar
        JPanel bar = new JPanel(new HGroupLayout(GroupLayout.STRETCH));
        bar.add(new JLabel(_msgs.get("m.username")), GroupLayout.FIXED);
        _username = new JTextField();
        _username.setActionCommand("skipToPassword");
        _username.addActionListener(this);
        bar.add(_username);
        box.add(bar);

        // create the password bar
        bar = new JPanel(new HGroupLayout(GroupLayout.STRETCH));
        bar.add(new JLabel(_msgs.get("m.password")), GroupLayout.FIXED);
        _password = new JPasswordField();
        _password.setActionCommand("logon");
        _password.addActionListener(this);
        bar.add(_password);
        box.add(bar);

        // create the logon button bar
        HGroupLayout gl = new HGroupLayout(GroupLayout.NONE);
        gl.setJustification(GroupLayout.RIGHT);
        bar = new JPanel(gl);
        _logon = new JButton(_msgs.get("m.logon"));
        _logon.setActionCommand("logon");
        _logon.addActionListener(this);
        bar.add(_logon);
        box.add(bar);

        box.add(new JLabel(_msgs.get("m.status")));
        _status = new JTextArea() {
            public Dimension getPreferredScrollableViewportSize ()
            {
                return new Dimension(10, 100);
            }
        };
        _status.setEditable(false);
        JScrollPane scroller = new JScrollPane(_status);
        box.add(scroller);

        // we'll want to listen for logon failure
        _ctx.getClient().addClientObserver(this);

        // start with focus in the username field
        _username.requestFocus();
    }

    public void actionPerformed (ActionEvent e)
    {
	String cmd = e.getActionCommand();
	if (cmd.equals("skipToPassword")) {
            _password.requestFocus();

        } else if (cmd.equals("logon")) {
            logon();

	} else {
	    System.out.println("Unknown action event: " + cmd);
	}
    }

    // documentation inherited from interface
    public void clientDidLogon (Client client)
    {
        _status.append(_msgs.get("m.logon_success") + "\n");
    }

    // documentation inherited from interface
    public void clientDidLogoff (Client client)
    {
        _status.append(_msgs.get("m.logged_off") + "\n");
        setLogonEnabled(true);
    }

    // documentation inherited from interface
    public void clientFailedToLogon (Client client, Exception cause)
    {
        String msg;
        if (cause instanceof LogonException) {
            msg = MessageBundle.compose("m.logon_failed", cause.getMessage());
        } else {
            msg = MessageBundle.tcompose("m.logon_failed", cause.getMessage());
        }
        _status.append(_msgs.xlate(msg) + "\n");
        setLogonEnabled(true);
    }

    // documentation inherited from interface
    public void clientConnectionFailed (Client client, Exception cause)
    {
        String msg = MessageBundle.tcompose("m.connection_failed",
                                            cause.getMessage());
        _status.append(_msgs.xlate(msg) + "\n");
        setLogonEnabled(true);
    }

    // documentation inherited from interface
    public boolean clientWillLogoff (Client client)
    {
        // no vetoing here
        return true;
    }

    protected void logon ()
    {
        // disable further logon attempts until we hear back
        setLogonEnabled(false);

        String username = _username.getText().trim();
        String password = new String(_password.getPassword()).trim();

        _status.append(_msgs.get("m.logging_on") + "\n");

        // configure the client with some credentials and logon
        Credentials creds = new UsernamePasswordCreds(username, password);
        Client client = _ctx.getClient();
        client.setCredentials(creds);
        client.logon();
    }

    protected void setLogonEnabled (boolean enabled)
    {
        _username.setEnabled(enabled);
        _password.setEnabled(enabled);
        _logon.setEnabled(enabled);
    }

    protected MiCasaContext _ctx;
    protected MessageBundle _msgs;

    protected JTextField _username;
    protected JPasswordField _password;
    protected JButton _logon;
    protected JTextArea _status;
}
