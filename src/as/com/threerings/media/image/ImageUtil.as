package com.threerings.media.image {

import flash.display.Bitmap;
import flash.display.BitmapData;
import flash.display.DisplayObject;
import flash.display.Shape;

import flash.geom.Point;
import flash.geom.Rectangle;

import flash.text.TextField;
import flash.text.TextLineMetrics;

/**
 * Image and Bitmap related utility functions.
 */
public class ImageUtil
{
    /**
     * Create a DisplayObject that will display an error message
     * of the specified dimensions.
     */
    public static function createErrorImage (width :int, height :int)
            :DisplayObject
    {
        var shape :Shape = new Shape();
        shape.graphics.beginBitmapFill(createErrorBitmap());
        shape.graphics.drawRect(0, 0, width, height);
        shape.graphics.endFill();
        return shape;

        /*
        // Alternate implementation that creates an actual Bitmap object
        var src :BitmapData = createErrorBitmap();
        var data :BitmapData = new BitmapData(width, height, false);
        data.draw(src);
        var rect :Rectangle = new Rectangle(0, 0, src.width, src.height);
        for (var xx :int = 0; xx < width; xx += src.width) {
            for (var yy :int = 0; yy < height; yy += src.height) {
                if (xx != 0 || yy != 0) {
                    data.copyPixels(data, rect, new Point(xx, yy));
                }
            }
        }
        return new Bitmap(data);
        */
    }

    /**
     * Create a minimally-sized "error" BitmapData.
     */
    public static function createErrorBitmap () :BitmapData
    {
        var txt :TextField = new TextField();
        txt.text = "Error";
        var metrics :TextLineMetrics = txt.getLineMetrics(0);
        var data :BitmapData = new BitmapData(
                metrics.width + ERROR_PADDING, metrics.height + ERROR_PADDING,
                false, 0xFF4040);
        data.draw(txt);
        return data;
    }

    /** The amount to pad error messages by. */
    private static const ERROR_PADDING :int = 5;
}
}
