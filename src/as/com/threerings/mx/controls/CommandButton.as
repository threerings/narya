package com.threerings.mx.controls {

import flash.events.MouseEvent;

import mx.controls.Button;

import com.threerings.mx.events.CommandEvent;

/**
 * A command button simply dispatches a Controller command (with an optional
 * argument) when it is clicked.
 */
public class CommandButton extends Button
{
    /**
     * Create a command button.
     */
    public function CommandButton (cmd :String = null, arg :Object = null)
    {
        setCommand(cmd, arg);
    }

    /**
     * Set the command and argument to be issued when this button is pressed.
     */
    public function setCommand (cmd :String, arg :Object = null) :void
    {
        _cmd = cmd;
        _arg = arg;
    }

    override protected function clickHandler (event :MouseEvent) :void
    {
        super.clickHandler(event);

        if (enabled) {
            // stop the click event
            event.stopImmediatePropagation();

            // dispatch the command event
            // if the arg is null and we're a toggle button, dispatch our state
            var arg :Object = _arg;
            if (arg == null && toggle) {
                arg = selected;
            }
            CommandEvent.dispatch(this, _cmd, arg);
        }
    }

    /** The command to submit when clicked. */
    protected var _cmd :String;

    /** The argument that accompanies our command. */
    protected var _arg :Object;
}
}
