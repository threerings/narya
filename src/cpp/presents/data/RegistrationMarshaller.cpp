#include "presents/stable.h"
#include "RegistrationMarshaller.h"
#include "presents/PresentsClient.h"

using namespace presents::data;

DEFINE_STREAMABLE("com.threerings.presents.data.RegistrationMarshaller", RegistrationMarshaller);

void RegistrationMarshaller::registerReceiver (Shared<presents::PresentsClient> client, Shared<presents::client::Registration> arg1)
{
    typedef std::vector< Shared<Streamable> > StreamableList;
    Shared<StreamableList> args(new StreamableList);
    args->push_back(arg1);
    args->push_back(getSharedThis());
    client->sendRequest(invOid, invCode, 1, args);
}

Shared<RegistrationMarshaller> RegistrationMarshaller::getSharedThis()
{
    return shared_from_this();
}
