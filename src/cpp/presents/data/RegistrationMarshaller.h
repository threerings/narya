//
// $Id$
//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2012 Three Rings Design, Inc., All Rights Reserved
// http://code.google.com/p/narya/
//
// This library is free software; you can redistribute it and/or modify it
// under the terms of the GNU Lesser General Public License as published
// by the Free Software Foundation; either version 2.1 of the License, or
// (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA

#pragma once

#include "presents/Streamable.h"
#include "presents/ObjectInputStream.h"
#include "presents/ObjectOutputStream.h"
#include "presents/data/InvocationMarshaller.h"
#include "presents/streamers/StreamableStreamer.h"
#include <boost/enable_shared_from_this.hpp>
#include "presents/client/Registration.h"

namespace presents { namespace data { 

class RegistrationMarshaller : public presents::data::InvocationMarshaller, public boost::enable_shared_from_this<RegistrationMarshaller> {
public:
    DECLARE_STREAMABLE();

    virtual ~RegistrationMarshaller () {}

    void registerReceiver (Shared<presents::PresentsClient> client, Shared<presents::client::Registration> arg1);
protected:
    Shared<RegistrationMarshaller> getSharedThis();
};

}}
