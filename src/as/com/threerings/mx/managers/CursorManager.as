package com.threerings.mx.managers {

import flash.display.DisplayObject;
import flash.display.InteractiveObject;
import flash.display.Sprite;
import flash.events.MouseEvent;
import flash.text.TextField;
import flash.text.TextFieldType;
import flash.ui.Mouse;

import mx.core.Application;
import mx.managers.SystemManager;
import mx.styles.CSSStyleDeclaration;
import mx.styles.StyleManager;

import com.threerings.util.HashMap;

/**
 */
public class CursorManager
{
    public static const SYSTEM_CURSOR :int = 0;

    public static const BUSY_CURSOR :int = 1;

    //--------------------------------------------------------------------------
    //
    //  Class methods
    //
    //--------------------------------------------------------------------------

    /**
     *  Makes the cursor visible.
     *  Cursor visibility is not reference-counted.
     *  A single call to the <code>showCursor()</code> method
     *  always shows the cursor regardless of how many calls
     *  to the <code>hideCursor()</code> method were made.
     */
    public static function showCursor () :void
    {
        if (_currentCursorId == SYSTEM_CURSOR) {
            Mouse.show();
        } else {
            _cursorHolder.visible = true;
        }
    }
    
    /**
     *  Makes the cursor invisible.
     *  Cursor visibility is not reference-counted.
     *  A single call to the <code>hideCursor()</code> method
     *  always hides the cursor regardless of how many calls
     *  to the <code>showCursor()</code> method were made.
     */
    public static function hideCursor () :void
    {
        _cursorHolder.visible = false;
        Mouse.hide();
    }

    public static function getCurrentCursorId () :int
    {
        return _currentCursorId;
    }

    /**
     *  Creates a new cursor.
     *
     *  @param cursorClass Class of the cursor to display.
     *
     *  @param xOffset Number that specifies the x offset
     *  of the cursor, in pixels, relative to the mouse pointer.
     *  The default value is 0.
     *
     *  @param yOffset Number that specifies the y offset
     *  of the cursor, in pixels, relative to the mouse pointer.
     *  The default value is 0.
     *
     *  @return The ID of the cursor.
     */
    public static function addCursor (
            cursorClass :Class, xOffset :int = 0, yOffset :int = 0) :int 
    {
        var cursorId :int = _nextCursorId++;
        
        var rec :CursorRecord = new CursorRecord(cursorClass);
        rec.x = xOffset;
        rec.y = yOffset;

        _cursors.put(cursorId, rec);
        
        return cursorId;
    }

    /**
     * Set the current cursor.
     */
    public static function setCursor (id :int) :void
    {
        if (id == _currentCursorId) {
            return;
        }

        if (!_initialized) {
            // oh, let's set it up
            _systemManager = Application.application.systemManager;

            // set up the busy cursor
            var cursorManagerStyleDeclaration :CSSStyleDeclaration =
                StyleManager.getStyleDeclaration("CursorManager");
            var busyCursorClass :Class =
                cursorManagerStyleDeclaration.getStyle("busyCursor");
            _busyCursor = new CursorRecord(busyCursorClass);

            // The first time a cursor is requested of the CursorManager,
            // create a Sprite to hold the cursor symbol
            _cursorHolder = new Sprite();
            _cursorHolder.mouseEnabled = false;
            _systemManager.cursorChildren.addChild(_cursorHolder);

            _initialized = true;
        }

        // figure out what the new cursor will be like
        var rec :CursorRecord;
        if (id == BUSY_CURSOR) {
            rec = _busyCursor;

        } else if (id != SYSTEM_CURSOR) {
            rec = (_cursors.get(id) as CursorRecord);
            if (rec == null) {
                // a bogus id was specified
                // TODO: throw an error?
                return;
            }
        }

        if (rec != null && (rec.cursor is Class)) {
            // go ahead and instantiate the class
            try {
                var disp :DisplayObject = new (rec.cursor as Class);
                rec.cursor = disp;
                if (disp is InteractiveObject) {
                    (disp as InteractiveObject).mouseEnabled = false;
                }

            } catch (err :Error) {
                // this cursor is not usable, bail
                return;
            }
        }

        // always remove any custom cursors from the hierarchy
        if (_cursorHolder.numChildren > 0) {
            _cursorHolder.removeChildAt(0);
        }

        if (id != SYSTEM_CURSOR) {
            Mouse.hide();
            var currentCursor :DisplayObject = (rec.cursor as DisplayObject);
            _cursorHolder.addChild(currentCursor);
            _cursorHolder.x = _systemManager.mouseX + rec.x;
            _cursorHolder.y = _systemManager.mouseY + rec.y;

            _currentXOffset = rec.x;
            _currentYOffset = rec.y;
            _systemManager.stage.addEventListener(
                MouseEvent.MOUSE_MOVE, mouseMoveHandler);
        

        } else {
            _currentXOffset = 0;
            _currentYOffset = 0;
            _systemManager.stage.removeEventListener(
                MouseEvent.MOUSE_MOVE, mouseMoveHandler);

            Mouse.show();
        }

        _currentCursorId = id;
    }

    /**
     *  Removes a cursor from the cursor list.
     *  If the cursor being removed is the currently displayed cursor,
     *  the CursorManager displays the next cursor in the list, if one exists.
     *  If the list becomes empty, the CursorManager displays
     *  the default system cursor.
     *
     *  @param cursorID ID of cursor to remove.
     */
    public static function removeCursor (id :int) :void 
    {
        var rec :CursorRecord = (_cursors.remove(id) as CursorRecord);

        if (rec != null && id == _currentCursorId) {
            setCursor(SYSTEM_CURSOR);
        }
    }
    
    /**
     *  Removes all of the cursors from the cursor list
     *  and restores the system cursor.
     */
    public static function removeAllCursors () :void
    {
        _cursors.clear();
        
        if (_currentCursorId != BUSY_CURSOR) {
            setCursor(SYSTEM_CURSOR);
        }
    }
    
    //--------------------------------------------------------------------------
    //
    //  Class event handlers
    //
    //--------------------------------------------------------------------------

    /**
     *  @private
     */
    private static function mouseMoveHandler (event :MouseEvent) :void
    {
        _cursorHolder.x = _systemManager.mouseX + _currentXOffset;
        _cursorHolder.y = _systemManager.mouseY + _currentYOffset;
        
        var target :Object = event.target;
        
        // Do target test.
        if (!_overTextField &&
                target is TextField && target.type == TextFieldType.INPUT) {	
            _overTextField = true;
            _showSystemCursor = true;

        } else if (_overTextField &&
                !(target is TextField && target.type == TextFieldType.INPUT)) {
            _overTextField = false;
            _showCustomCursor = true;
        }
        
        // Handle switching between system and custom cursor.
        if (_showSystemCursor) {
            _showSystemCursor = false;
            _cursorHolder.visible = false;
            Mouse.show();
        }
        if (_showCustomCursor) {
            _showCustomCursor = false;
            _cursorHolder.visible = true;
            Mouse.hide();
        }
    }

    /** A mapping of all assigned cursor ids. */
    private static const _cursors :HashMap = new HashMap();

    private static var _currentCursorId :int = 0; // SYSTEM_CURSOR

    private static var _nextCursorId :int = 2; // skip BUSY
    
    private static var _initialized :Boolean = false;

    private static var _cursorHolder :Sprite;
    
    private static var _currentXOffset :int = 0;
    private static var _currentYOffset :int = 0;

    /** A record for the busy cursor (where it can't be removed). */
    private static var _busyCursor :CursorRecord;

    private static var _overTextField :Boolean = false;
    
    private static var _overLink :Boolean = false;
    
    private static var _showSystemCursor :Boolean = false;
    
    private static var _showCustomCursor :Boolean = false;
    
    private static var _systemManager :SystemManager = null;
}
}


/**
 */
class CursorRecord extends Object
{
    public function CursorRecord (clazz :Class)
    {
        cursor = clazz;
    }

    /** The class, or instantiated cursor. */
    public var cursor :Object

    /** The x/y offset for the hotspot. */
    public var x :Number;
    public var y :Number;
}
