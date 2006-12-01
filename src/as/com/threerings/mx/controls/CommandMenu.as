package com.threerings.mx.controls {

import flash.display.DisplayObject;
import flash.display.DisplayObjectContainer;

import flash.geom.Rectangle;

import mx.controls.Menu;
import mx.core.mx_internal;
import mx.core.ScrollPolicy;
import mx.events.MenuEvent;

import com.threerings.mx.events.CommandEvent;

/**
 * A pretty standard menu that can submit CommandEvents if menu items
 * have "command" and possibly "arg" properties. Commands are submitted to
 * controllers for processing. Alternatively, you may specify
 * "callback" properties that specify a function closure to call, with the
 * "arg" property containing either a single arg or an array of args.
 *
 * Example dataProvider array:
 * [ { label: "Go home", icon: homeIconClass,
 *     command: Controller.GO_HOME, arg: homeId },
 *   { type: "separator"},
     { label: "Crazytown", callback: setCrazy, arg: [ true, false ] },
 *   { label: "Other places", children: subMenuArray }
 * ];
 *
 * See "Defining menu structure and data" in the Flex manual for the
 * full list.
 */ 
public class CommandMenu extends Menu
{
    public function CommandMenu ()
    {
        super();

        addEventListener(MenuEvent.ITEM_CLICK, itemClicked);
//        addEventListener(MenuEvent.MENU_SHOW, handleMenuShown);
    }

    /**
     * Factory method to create a command menu.
     *
     * @param parent The parent of this menu.
     * @param items an array of menu items.
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
     * Actually pop up the menu. This can be used instead of show().
     */
    public function popUp (
        trigger :DisplayObject, popUpwards :Boolean = true) :void
    {
        var r :Rectangle = trigger.getBounds(trigger.stage);

        if (popUpwards) {
            show(r.x, 0);
            // then, reposition the y once we know our size
            y = r.y - getExplicitOrMeasuredHeight();

        } else {
            // simply position it below the trigger
            show(r.x, r.y + r.height);
        }
    }

    /**
     * Callback for MenuEvent.ITEM_CLICK.
     */
    protected function itemClicked (event :MenuEvent) :void
    {
        var arg :Object = getItemArgument(event.item);
        var cmd :String = getItemCommand(event.item);
        var fn :Function;
        if (cmd == null) {
            fn = getItemCallback(event.item);
        }
        if (cmd != null || fn != null) {
            event.stopImmediatePropagation();
            if (cmd != null) {
                CommandEvent.dispatch(mx_internal::parentDisplayObject, cmd, arg);

            } else {
                try {
                    var args :Array = (arg as Array);
                    if (args == null && arg != null) {
                        args = [ arg ];
                    }
                    fn.apply(null, args);

                } catch (err :Error) {
                    Log.getLog(this).warning("Unable to call menu callback: " +
                        event.item);
                }
            }
        }
    }

//    /**
//     * Callback for MenuEvent.MENU_SHOW.
//     */
//    protected function handleMenuShown (event :MenuEvent) :void
//    {
//        // just ensure that every menu can scroll
//        event.menu.verticalScrollPolicy = ScrollPolicy.ON;
//    }

    /**
     * Get the command for the specified item, if any.
     * Somewhat similar to bits in the DefaultDataDescriptor.
     */
    protected function getItemCommand (item :Object) :String
    {
        try {
            if (item is XML) {
                return (item.@command as String);

            } else if (item is Object) {
                return (item.command as String);
            }
        } catch (e :Error) {
            // fall through
        }

        return null;
    }

    /**
     * Get the callback function for the specified item, if any.
     */
    protected function getItemCallback (item :Object) :Function
    {
        try {
            if (item is XML) {
                return (item.@callback as Function);

            } else if (item is Object) {
                return (item.callback as Function);
            }
        } catch (e :Error) {
            // fall through
        }

        return null;
    }

    /**
     * Get the command for the specified item, if any.
     * Somewhat similar to bits in the DefaultDataDescriptor.
     */
    protected function getItemArgument (item :Object) :Object
    {
        try {
            if (item is XML) {
                return item.@arg;

            } else if (item is Object) {
                return item.arg;
            }
        } catch (e :Error) {
            // fall through
        }

        return null;
    }
}
}
