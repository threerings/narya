#include "presents/stable.h"
#include "UsernamePasswordCreds.h"

using namespace presents::net;

DEFINE_STREAMABLE("com.threerings.presents.net.UsernamePasswordCreds", UsernamePasswordCreds);

void UsernamePasswordCreds::readObject (ObjectInputStream& in)
{
    presents::net::Credentials::readObject(in);
    username = boost::static_pointer_cast<util::Name>(in.readObject());
    password = in.readField< utf8 >();
}

void UsernamePasswordCreds::writeObject (ObjectOutputStream& out) const
{
    presents::net::Credentials::writeObject(out);
    out.writeObject(username);
    out.writeField(password);
}
