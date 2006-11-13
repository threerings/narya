package com.threerings.mx.controls {

import flash.display.DisplayObject;
import flash.display.Graphics;
import flash.display.Sprite;

import flash.events.Event;
import flash.events.MouseEvent;

import flash.geom.Matrix;
import flash.geom.Rectangle;

import mx.containers.Canvas;

/**
 * A control for scrolling some component separately.
 */
public class ScrollBox extends Canvas
{
    public function ScrollBox (
        target :DisplayObject, maxWidth :int, maxHeight :int)
    {
        _target = target;
        _maxWidth = maxWidth;
        _maxHeight = maxHeight;

        opaqueBackground = 0xFFFFFF;

        _box = new Sprite();
        rawChildren.addChild(_box);

        recheckBounds();

        // TODO: only when something changes
        addEventListener(Event.ENTER_FRAME, enterFrame);

        _box.addEventListener(MouseEvent.MOUSE_DOWN, spritePressed);
    }

    override public function setActualSize (w :Number, h :Number) :void
    {
        super.setActualSize(w, h);

        recheckBounds();

        graphics.clear();
        graphics.lineStyle(1, 0);
        graphics.drawRect(0, 0, width, height);
    }

    protected function enterFrame (vent :Event) :void
    {
        if (!_dragging) {
            recheckBounds();
        }
    }

    /**
     * Get the bounds of the scrollable area. Broken-out for easy overridding.
     */
    protected function getScrollBounds () :Rectangle
    {
        var bounds :Rectangle = _target.transform.pixelBounds;
//        var m :Matrix = _target.transform.concatenatedMatrix;
//        bounds.topLeft = m.transformPoint(bounds.topLeft);
//        bounds.bottomRight = m.transformPoint(bounds.bottomRight);
        return bounds;
    }

    protected function recheckBounds () :void
    {
        var bounds :Rectangle = getScrollBounds();

        // see what's visible, what's not..
        var scroller :Rectangle = _target.scrollRect;
        if (scroller == null) {
            scroller = bounds.clone();
        }

        if ((_bounds != null) && _bounds.equals(bounds) &&
                (_scroller != null) && _scroller.equals(scroller)) {
            return;
        }

        _bounds = bounds.clone();
        _scroller = scroller;

        _scale = Math.min(1,
            Math.min(_maxWidth / _bounds.width, _maxHeight / _bounds.height));
        width = _scale * _bounds.width;
        height = _scale * _bounds.height;

        drawBox(_box, _scroller.width * _scale, _scroller.height * _scale);

        _box.x = (_scroller.x - _bounds.x) * _scale;
        _box.y = (_scroller.y - _bounds.y) * _scale;
    }

    protected function drawBox (box :Sprite, ww :Number, hh :Number) :void
    {
        var g :Graphics = _box.graphics;
        g.clear();
        g.beginFill(0x0000FF);
        g.drawRect(0, 0, ww, hh);
        g.endFill();
    }

    protected function spritePressed (evt :MouseEvent) :void
    {
        _box.addEventListener(Event.ENTER_FRAME, boxUpdate);
        _box.startDrag(true,
            new Rectangle(0, 0, width - _box.width, height - _box.height));
        stage.addEventListener(MouseEvent.MOUSE_UP, spriteReleased);
        _dragging = true;
    }

    protected function spriteReleased (evt :MouseEvent) :void
    {
        stage.removeEventListener(MouseEvent.MOUSE_UP, spriteReleased);
        _box.stopDrag();
        _box.removeEventListener(Event.ENTER_FRAME, boxUpdate);
        _dragging = false;
    }

    protected function boxUpdate (evt :Event) :void
    {
        var r :Rectangle = _target.scrollRect;
        r.x = (_box.x / _scale) + _bounds.x;
        r.y = (_box.y / _scale) + _bounds.y;
        _target.scrollRect = r;
    }

    protected var _dragging :Boolean;

    protected var _target :DisplayObject;

    protected var _maxWidth :int;
    protected var _maxHeight :int;

    protected var _box :Sprite;

    protected var _scale :Number;

    protected var _bounds :Rectangle;
    protected var _scroller :Rectangle;
}
}
