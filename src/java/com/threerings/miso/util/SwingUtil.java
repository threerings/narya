//
// $Id: SwingUtil.java,v 1.2 2001/07/18 21:45:42 shaper Exp $

package com.threerings.miso.util;

import java.awt.*;

public class SwingUtil
{
    public static void centerFrame (Frame frame)
    {
        Toolkit tk = frame.getToolkit();
        Dimension ss = tk.getScreenSize();
        int width = frame.getWidth(), height = frame.getHeight();
        frame.setBounds((ss.width-width)/2, (ss.height-height)/2,
                        width, height);
    }
}
