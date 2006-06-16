package com.threerings.util {

import flash.ui.ContextMenuItem;
import flash.events.ContextMenuEvent;

import com.threerings.events.ControllerEvent;

/**
 */
public class MenuUtil
{
    /**
     * Create a menu item that will submit a controller command when selected.
     */
    public static function createControllerMenuItem (
            caption :String, cmd :String, arg :Object = null,
            separatorBefore :Boolean = false, enabled :Boolean = true,
            visible :Boolean = true) :ContextMenuItem
    {
        var item :ContextMenuItem =
            new ContextMenuItem(caption, separatorBefore, enabled, visible);
        item.addEventListener(ContextMenuEvent.MENU_ITEM_SELECT,
            function (event :ContextMenuEvent) :void {
                event.mouseTarget.dispatchEvent(new ControllerEvent(cmd, arg));
            });
        return item;
    }
}
}
