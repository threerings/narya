package com.threerings.media.sprite;

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.Composite;

import com.threerings.media.util.Path;

public class FadableImageSprite extends OrientableImageSprite
{
    /**
     * Fades this sprite in over the specified duration after
     * waiting for the specified delay.
     */
    public void fadeIn (long delay, long duration)
    {
        setAlpha(0.0f);

        _fadeStamp = 0;
        _fadeDelay = delay;
        _fadeInDuration = duration;
    }

    /**
     * Puts this sprite on the specified path and fades it in over
     * the specified duration.
     *
     * @param path the path to move along
     * @param fadePortion the portion of time to spend fading in, from 0.0f
     * (no time) to 1.0f (the entire time)
     */
    public void moveAndFadeIn (Path path, long pathDuration, float fadePortion)
    {
        move(path);

        setAlpha(0.0f);

        _fadeInDuration = (long)(pathDuration*fadePortion);
    }

    /**
     * Puts this sprite on the specified path and fades it out over
     * the specified duration.
     *
     * @param path the path to move along
     * @param pathDuration the duration of the path
     * @param fadePortion the portion of time to spend fading out, from 0.0f
     * (no time) to 1.0f (the entire time)
     */
    public void moveAndFadeOut (Path path, long pathDuration, float fadePortion)
    {
        move(path);

        setAlpha(1.0f);

        _pathDuration = pathDuration;
        _fadeOutDuration = (long)(pathDuration*fadePortion);
    }

    /**
     * Puts this sprite on the specified path, fading it in over the specified
     * duration at the beginning and fading it out at the end.
     *
     * @param path the path to move along
     * @param pathDuration the duration of the path
     * @param fadePortion the portion of time to spend fading in/out, from
     * 0.0f (no time) to 1.0f (the entire time)
     */
    public void moveAndFadeInAndOut (Path path, long pathDuration,
        float fadePortion)
    {
        move(path);

        setAlpha(0.0f);

        _pathDuration = pathDuration;
        _fadeInDuration = _fadeOutDuration = (long)(pathDuration*fadePortion);
    }

    // Documentation inherited.
    public void tick (long tickStamp)
    {
        super.tick(tickStamp);

        if (_fadeInDuration != -1) {
            if (_path != null && (tickStamp-_pathStamp) <= _fadeInDuration) {
                // fading in while moving
                float alpha = (float)(tickStamp-_pathStamp)/_fadeInDuration;
                if (alpha >= 1.0f) {
                    // fade-in complete
                    setAlpha(1.0f);
                    _fadeInDuration = -1;

                } else {
                    setAlpha(alpha);
                }

            } else {
                // fading in while stationary
                if (_fadeStamp == 0) {
                    // store the time at which fade started
                    _fadeStamp = tickStamp;
                }
                if (tickStamp > _fadeStamp + _fadeDelay) {
                    // initial delay has passed
                    float alpha = (float)(tickStamp-_fadeStamp-_fadeDelay)/
                        _fadeInDuration;
                    if (alpha >= 1.0f) {
                        // fade-in complete
                        setAlpha(1.0f);
                        _fadeInDuration = -1;

                    } else {
                        setAlpha(alpha);
                    }
                }
            }

        } else if (_fadeOutDuration != -1 && _pathStamp+_pathDuration-tickStamp
            <= _fadeOutDuration) {
            // fading out while moving
            float alpha = (float)(_pathStamp+_pathDuration-tickStamp)/
                _fadeOutDuration;
            setAlpha(alpha);
        }
    }

    // Documentation inherited.
    public void pathCompleted (long timestamp)
    {
        super.pathCompleted(timestamp);

        if (_fadeInDuration != -1) {
            setAlpha(1.0f);
            _fadeInDuration = -1;

        } else if (_fadeOutDuration != -1) {
            setAlpha(0.0f);
            _fadeOutDuration = -1;
        }
    }

    // Documentation inherited.
    public void paint (Graphics2D gfx)
    {
        if (_alphaComposite.getAlpha() < 1.0f) {
            Composite ocomp = gfx.getComposite();
            gfx.setComposite(_alphaComposite);
            super.paint(gfx);
            gfx.setComposite(ocomp);

        } else {
            super.paint(gfx);
        }
    }

    /**
     * Sets the alpha value of this sprite.
     */
    public void setAlpha (float alpha)
    {
        if (alpha < 0.0f) {
            alpha = 0.0f;

        } else if (alpha > 1.0f) {
            alpha = 1.0f;
        }
        if (alpha != _alphaComposite.getAlpha()) {
            _alphaComposite =
                AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha);
            if (_mgr != null) {
                _mgr.getRegionManager().invalidateRegion(_bounds);
            }
        }
    }

    /**
     * Returns the alpha value of this sprite.
     */
    public float getAlpha ()
    {
        return _alphaComposite.getAlpha();
    }

    /** The alpha composite. */
    protected AlphaComposite _alphaComposite =
        AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f);

    /** If fading in, the fade-in duration (otherwise -1). */
    protected long _fadeInDuration = -1;

    /** If fading in without moving, the fade-in delay. */
    protected long _fadeDelay;

    /** The time at which fading started. */
    protected long _fadeStamp = -1;

    /** If fading out, the fade-out duration (otherwise -1). */
    protected long _fadeOutDuration = -1;

    /** If fading out, the path duration. */
    protected long _pathDuration;
}
