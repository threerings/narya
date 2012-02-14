#include "presents/stable.h"
#include "AuthResponseData.h"

using namespace presents::net;

DEFINE_STREAMABLE("com.threerings.presents.net.AuthResponseData", AuthResponseData);

void AuthResponseData::readObject (ObjectInputStream& in)
{
    presents::dobj::DObject::readObject(in);
    code = in.readField< utf8 >();
}

void AuthResponseData::writeObject (ObjectOutputStream& out) const
{
    presents::dobj::DObject::writeObject(out);
    out.writeField(code);
}
