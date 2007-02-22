package com.threerings.util {

import flash.system.ApplicationDomain;

/**
 * A non-flex version of ResourceBundle.
 */
public /*abstract*/ class ResourceBundle
{
    /**
     * Get a resource bundle by name.
     */
    public static function getResourceBundle (
        baseName :String, appDom :ApplicationDomain = null) :ResourceBundle
    {
        if (appDom == null) {
            appDom = ApplicationDomain.currentDomain;
        }

        var className :String = baseName + "_properties";
        if (appDom.hasDefinition(className)) {
            var obj :Object = new (Class(appDom.getDefinition(className)))();
            if (obj is ResourceBundle) {
                var rb :ResourceBundle = ResourceBundle(obj);
                rb.initialize(baseName);
                return rb;
            }
        }

        throw new Error("Could not find resource bundle " + baseName);
    }

    /**
     * Get a resource value as a String.
     */
    public function getString (key :String) :String
    {
        return String(getObject(key));
    }

    /**
     * Get a resouce value as the raw type contained within the content
     * object.
     */
    public function getObject (key :String) :Object
    {
        var val :Object = _content[key];
        if (val != null) {
            return val;
        }
        throw new Error("Key '" + key + "' was not found in resource bundle '" +
            _name + "'.");
    }

    /**
     * Internal initialization method.
     */
    private function initialize (name :String) :void
    {
        _content = getContent();
        if (_content == null) {
            throw new Error("No content found in resource bundle '" + name + "'.");
        }
        _name = name;
    }

    /**
     * Get the content of this resource bundle.
     * You may override this to do custom stuff.
     */
    protected function getContent () :Object
    {
        return null;
    }

    /** The name of this bundle. */
    protected var _name :String;

    /** Contains resource bundle values. */
    protected var _content :Object;
}
}
