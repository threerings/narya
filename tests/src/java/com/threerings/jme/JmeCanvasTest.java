//
// $Id$

package com.threerings.jme;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JFrame;

import com.jme.bounding.BoundingBox;
import com.jme.math.Vector3f;
import com.jme.scene.shape.Box;

/**
 * Tests the JME/AWT integration bits.
 */
public class JmeCanvasTest extends JmeCanvasApp
{
    public static void main (String[] args)
    {
        final JmeCanvasTest app = new JmeCanvasTest();
        JFrame frame = new JFrame("JmeCanvasTest");
        frame.getContentPane().add(app.getCanvas(), BorderLayout.CENTER);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);
        frame.setVisible(true);

        JButton button = new JButton("Create box");
        frame.getContentPane().add(button, BorderLayout.SOUTH);
        button.addActionListener(new ActionListener() {
            public void actionPerformed (ActionEvent event) {
                app.addBox();
            }
        });
        app.run();
    }

    public void addBox ()
    {
        Vector3f max = new Vector3f(5, 5, 5);
        Vector3f min = new Vector3f(-5, -5, -5);

        Box box = new Box("Box", min, max);
        box.setModelBound(new BoundingBox());
        box.updateModelBound();
        box.setLocalTranslation(new Vector3f(0, 0, -10));
        _geom.attachChild(box);
        _geom.updateRenderState();
    }

    protected JmeCanvasTest ()
    {
        super(800, 600);
    }

    protected void initRoot ()
    {
        super.initRoot();
    }
}
