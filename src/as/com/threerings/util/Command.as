//
// $Id$

package com.threerings.util {

import flash.events.Event;
import flash.events.IEventDispatcher;

/**
 * Contains a simple binding function to bind events to commands.
 *
 * TODO: be able to set up a Command object, and bind it to multiple functions
 */
public class Command
{
    /**
     * Bind an event to a command.
     */
    public static function bind (
        source :IEventDispatcher, eventType :String, cmdOrFn :Object, arg :Object = null) :void
    {
        source.addEventListener(eventType, function (... ignored) :void {
            CommandEvent.dispatch(source, cmdOrFn, arg);
        });
    }

    /**
     * Convenience, since otherwise nobody usually needs to ever import CommandEvent.
     */
    public static function dispatch (
        source :IEventDispatcher, cmdOrFn :Object, arg :Object = null) :void
    {
        CommandEvent.dispatch(source, cmdOrFn, arg);
    }

//    public static function bind (
//        source :IEventDispatcher, eventType :String, cmdOrFn :Object, arg :Object = null) :Command
//    {
//        var cmd :Command = new Command(cmdOrFn, arg);
//        cmd.addBind(source, eventType);
//        return cmd;
//    }
//
//    public function Command (cmdOrFn :Object, arg :Object = null)
//    {
//        _cmdOrFn = cmdOrFn;
//        _arg = arg;
//    }
//
//    public function setOverrideSource (disp :IEventDispatcher) :void
//    {
//        _source = source;
//    }
//
//    public function addBind (source :IEventDispatcher, eventType :String) :void
//    {
//        source.addEventListener(eventType, eventHandler);
//    }
//
//    public function removeBind (source :IEventDispatcher, eventType :String) :void
//    {
//        source.removeEventListener(eventType, eventHandler);
//    }
//
//    protected function eventHandler (event :Event) :void
//    {
//        dispatch(_source || IEventDispatcher(event.source), _cmdOrFn, arg);
//    }
//
//    /** An override source for the command events, or null to use the triggering dispatcher. */
//    protected var _source :IEventDispatcher;
//    protected var _cmdOrFn :Object;
//    protected var _arg :Object;
}
}
