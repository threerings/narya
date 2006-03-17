package com.threerings.util {

public class ResourceBundle
{
    public static function getBundle (path :String) :ResourceBundle
    {
        return new ResourceBundle();
    }

    public function getString (key :String) :String
    {
        return "TODO: resource:" + key;
    }
}
}
