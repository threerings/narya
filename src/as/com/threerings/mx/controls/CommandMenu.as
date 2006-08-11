package com.threerings.mx.controls {

import flash.display.DisplayObjectContainer;

import mx.controls.Menu;
import mx.core.mx_internal;
import mx.events.MenuEvent;

import com.threerings.mx.events.CommandEvent;

public class CommandMenu extends Menu
{
    public function CommandMenu ()
    {
        super();

        iconFunction = getIconFor;
        labelFunction = getLabelFor;

        addEventListener(MenuEvent.ITEM_CLICK, itemClicked);
    }

    /**
     * Factory method to create a command menu.
     *
     * @param parent The parent of this menu.
     * @param items an array of menu items, where each item is another array
     *  with the following format: [ text, iconClass, command, arg ]. Arg is
     *  optional.
     */
    public static function createMenu (
            parent :DisplayObjectContainer, items :Array) :CommandMenu
    {
        var menu :CommandMenu = new CommandMenu();
        menu.tabEnabled = false;
        menu.showRoot = true;
        Menu.popUpMenu(menu, parent, items);
        return menu;
    }

    /**
     * Callback for MenuEvent.ITEM_CLICK.
     */
    protected function itemClicked (event :MenuEvent) :void
    {
        event.stopImmediatePropagation();
        var element :Array = (event.item as Array);

        CommandEvent.dispatch(mx_internal::parentDisplayObject,
            String(element[2]), element[3]);
    }

    // our function for retrieving a label for a menu entry
    protected function getLabelFor (obj :Object) :String
    {
        var element :Array = (obj as Array);
        return String(element[0]);
    }

    // our function for retrieving an icon for a menu entry
    protected function getIconFor (obj :Object) :Class
    {
        var element :Array = (obj as Array);
        return Class(element[1]);
    }
}
}
