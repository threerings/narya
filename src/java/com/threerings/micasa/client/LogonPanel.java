//
// $Id: LogonPanel.java,v 1.4 2002/04/12 16:26:12 shaper Exp $

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

import com.threerings.presents.client.Client;
import com.threerings.presents.net.Credentials;
import com.threerings.presents.net.UsernamePasswordCreds;

import com.threerings.micasa.util.MiCasaContext;

public class LogonPanel
    extends JPanel implements ActionListener
{
    public LogonPanel (MiCasaContext ctx)
    {
        // keep this around for later
        _ctx = ctx;

        GroupLayout gl = new VGroupLayout(GroupLayout.NONE);
	gl.setOffAxisPolicy(GroupLayout.EQUALIZE);
	setLayout(gl);

	// give ourselves a wee bit of a border
	setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        // create a big fat label
        JLabel title = new JLabel("Mi Casa!");
        title.setFont(new Font("Helvetica", Font.BOLD, 24));
        add(title);

        // create the username bar
        JPanel bar = new JPanel(new HGroupLayout(GroupLayout.STRETCH));
        bar.add(new JLabel("Username"), GroupLayout.FIXED);
        _username = new JTextField();
        _username.setActionCommand("skipToPassword");
        _username.addActionListener(this);
        bar.add(_username);
        add(bar);

        // create the password bar
        bar = new JPanel(new HGroupLayout(GroupLayout.STRETCH));
        bar.add(new JLabel("Password"), GroupLayout.FIXED);
        _password = new JPasswordField();
        _password.setActionCommand("logon");
        _password.addActionListener(this);
        bar.add(_password);
        add(bar);

        // create the logon button bar
        gl = new HGroupLayout(GroupLayout.NONE);
        gl.setJustification(GroupLayout.RIGHT);
        bar = new JPanel(gl);
        _logon = new JButton("Logon");
        _logon.setActionCommand("logon");
        _logon.addActionListener(this);
        bar.add(_logon);
        add(bar);

        add(new JLabel("Status"));
        _status = new JTextArea() {
            public Dimension getPreferredScrollableViewportSize ()
            {
                return new Dimension(10, 100);
            }
        };
        _status.setEditable(false);
        JScrollPane scroller = new JScrollPane(_status);
        add(scroller);

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

    protected void logon ()
    {
        // disable further logon attempts until we hear back
        setLogonEnabled(false);

        String username = _username.getText().trim();
        String password = new String(_password.getPassword()).trim();

        System.out.println("Logging on " + username + "/" + password);

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

    protected void logonFailed (Exception cause)
    {
        _status.append("Logon failed: " + cause.getMessage() + "\n");
        setLogonEnabled(true);
    }

    protected MiCasaContext _ctx;
    protected JTextField _username;
    protected JPasswordField _password;
    protected JButton _logon;
    protected JTextArea _status;
}
