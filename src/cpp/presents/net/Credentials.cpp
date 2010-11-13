#include "presents/stable.h"
#include "Credentials.h"

using namespace presents::net;

DEFINE_STREAMABLE("com.threerings.presents.net.Credentials", Credentials);

void Credentials::readObject (ObjectInputStream& /*in*/)
{
}

void Credentials::writeObject (ObjectOutputStream& /*out*/) const
{
}
