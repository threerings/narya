package com.threerings.mx.effects {

import mx.effects.effectClasses.ActionEffectInstance;

public class FunctionEffectInstance extends ActionEffectInstance
{
    public function FunctionEffectInstance (target :Object)
    {
        super(target);
    }

    /** The function to call. */
    public var func :Function;

    /** The args to pass. */
    public var args :Array;

    override public function play () :void
    {
        super.play();

        // call the function!
        func.apply(null, args);

        // and we're done
        finishRepeat();
    }
}
}
