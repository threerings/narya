//
// $Id: BasicYoContext.java 19661 2005-03-09 02:40:29Z andrzej $

package com.threerings.stage.util;

import com.threerings.resource.ResourceManager;
import com.threerings.util.KeyDispatcher;
import com.threerings.util.KeyboardManager;
import com.threerings.util.MessageManager;

import com.threerings.media.FrameManager;
import com.threerings.media.image.ColorPository;
import com.threerings.media.image.ImageManager;
import com.threerings.media.sound.SoundManager;

import com.threerings.cast.CharacterManager;
import com.threerings.cast.ComponentRepository;
import com.threerings.miso.util.MisoContext;

/**
 * A context that provides for the myriad requirements of the Stage
 * system.
 */
public interface StageContext
    extends MisoContext
{
    /**
     * Returns the frame manager driving our interface.
     */
    public FrameManager getFrameManager ();

    /**
     * Returns the resource manager via which all client resources are
     * loaded.
     */
    public ResourceManager getResourceManager ();

    /**
     * Access to the image manager.
     */
    public ImageManager getImageManager ();

    /**
     * Provides access to the key dispatcher.
     */
    public KeyDispatcher getKeyDispatcher ();

    /**
     * Returns a reference to the message manager used by the client.
     */
    public MessageManager getMessageManager ();

    /**
     * Returns a reference to the sound manager used by the client.
     */
    public SoundManager getSoundManager();

    /**
     * Returns a reference to the keyboard manager.
     */
    public KeyboardManager getKeyboardManager();

    /**
     * Returns the component repository in use by this client.
     */
    public ComponentRepository getComponentRepository ();

    /**
     * Returns a reference to the colorization repository.
     */
    public ColorPository getColorPository ();
    
    /**
     * Translates the specified message using the default bundle.
     */
    public String xlate (String message);

    /**
     * Translates the specified message using the specified bundle.
     */
    public String xlate (String bundle, String message);
}
