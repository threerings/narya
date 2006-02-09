package com.threerings.io {

public class ClassMapping
{
    public var code :int;
    public var classname :String;
    public var streamer :Streamer;

    public function ClassMapping (code :int, classname :String,
            streamer :Streamer)
    {
        this.code = code;
        this.classname = classname;
        this.streamer = streamer;
    }
}
}
