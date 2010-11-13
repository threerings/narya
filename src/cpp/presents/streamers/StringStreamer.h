#pragma once

#include "../Streamer.h"

class StringStreamer : public Streamer
{
public:
    Shared<void> createObject (ObjectInputStream& in);
    void writeObject (const Shared<void>& object, ObjectOutputStream& out);
};
