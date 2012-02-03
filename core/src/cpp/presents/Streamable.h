//
// $Id$

#pragma once

class ObjectOutputStream;
class ObjectInputStream;

struct Streamable
{
    virtual void writeObject (ObjectOutputStream& out) const = 0;
    virtual void readObject (ObjectInputStream& in) = 0;
    virtual const utf8& getJavaClassName () const = 0;
};

