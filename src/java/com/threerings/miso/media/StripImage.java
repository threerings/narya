//
// $Id: StripImage.java,v 1.1 2001/07/12 22:38:03 shaper Exp $

package com.threerings.cocktail.miso.media;

import java.awt.*;
import java.awt.image.*;

/**
 * StripImage facilitates cutting an image up into individual frames.
 */
public class StripImage
{
    public StripImage (Image img, int frameWidth, int frameHeight,
		       int framesPerRow, int numFrames)
    {
	_img = img;
	_frameWidth = frameWidth;
	_frameHeight = frameHeight;
	_framesPerRow = framesPerRow;
	_numFrames = numFrames;
    }

    public Image getFrame (int idx, ImageObserver obs)
    {
	int frameX = (idx % _framesPerRow) * _frameWidth;
	int frameY = (idx / _framesPerRow) * _frameHeight;
	CropImageFilter crop = 
	    new CropImageFilter(frameX, frameY, _frameWidth, _frameHeight);

	FilteredImageSource prod = 
	    new FilteredImageSource(_img.getSource(), crop);

	Image img = ImageManager.tk.createImage(prod);
	ImageManager.tk.prepareImage(img, -1, -1, obs);

	return img;
    }

    public Image[] getAllFrames (ImageObserver obs)
    {
	Image allImgs[] = new Image[_numFrames];

	for (int ii = 0; ii < _numFrames; ii++) {
	    allImgs[ii] = getFrame(ii, obs);
	}

	return allImgs;
    }

    protected Image _img;
    protected int _frameWidth, _frameHeight;
    protected int _framesPerRow, _numFrames;
}
