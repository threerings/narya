package com.threerings.media.image {

import flash.display.BitmapData;
import flash.display.DisplayObject;
import flash.display.Shape;
import flash.display.TextField;

import flash.text.TextLineMetrics;

/**
 * Image and Bitmap related utility functions.
 */
public class ImageUtil
{
    /**
     * Create a Shape that will display an error message of the specified
     * dimensions.
     */
    public static function createErrorImage (width :int, height :int) :Shape
    {
        var shape :Shape = new Shape();
        shape.graphics.beginBitmapFill(createErrorBitmap());
        shape.graphics.drawRect(0, 0, width, height);
        shape.graphics.endFill();
        return shape;
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
