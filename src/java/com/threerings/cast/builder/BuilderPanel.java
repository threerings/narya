//
// $Id: BuilderPanel.java,v 1.1 2001/10/30 16:16:01 shaper Exp $

package com.threerings.cast.builder;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Iterator;

import javax.swing.*;

import com.samskivert.swing.*;

import com.threerings.cast.*;

/**
 * The builder panel presents the user with an overview of a
 * composited character and facilities for altering the individual
 * components that comprise the character's display image.
 */
public class BuilderPanel extends JPanel implements ActionListener
{
    /**
     * Constructs the builder panel.
     */
    public BuilderPanel (CharacterManager charmgr)
    {
        _charmgr = charmgr;

        setLayout(new VGroupLayout());

	// give ourselves a wee bit of a border
	setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        GroupLayout gl = new HGroupLayout(GroupLayout.STRETCH);
        gl.setOffAxisPolicy(GroupLayout.STRETCH);

        // create the component selection and sprite display panels
        JPanel sub = new JPanel(gl);
        ComponentType ctype = getComponentType();
        sub.add(_comppanel = new ComponentPanel(_charmgr, ctype));
        sub.add(_spritepanel = new SpritePanel());

        add(sub);

        // create the "OK" button
        JButton ok = new JButton("OK");
        ok.addActionListener(this);
        ok.setActionCommand("ok");
        add(ok);
    }

    public void actionPerformed (ActionEvent e)
    {
	String cmd = e.getActionCommand();
	if (cmd.equals("ok")) {
            CharacterDescriptor desc = _comppanel.getDescriptor();
            CharacterSprite sprite = _charmgr.getCharacter(desc);
            _spritepanel.setSprite(sprite);
        } else {
	    Log.warning("Unknown action command [cmd=" + cmd + "].");
        }
    }

    protected ComponentType getComponentType ()
    {
        ComponentType ctype = null;
        Iterator types = _charmgr.enumerateComponentTypes();
        // for now, fixedly choose the first component type
        // TODO: fix this hack
        return (ComponentType)types.next();
    }

    /** The component panel that displays components available for
     * selection. */
    protected ComponentPanel _comppanel;

    /** The sprite panel that displays the composited character sprite. */
    protected SpritePanel _spritepanel;

    /** The character manager. */
    protected CharacterManager _charmgr;
}
