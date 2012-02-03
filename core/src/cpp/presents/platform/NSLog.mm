#include "presents/Util.h"
#undef interface

#include <Foundation/Foundation.h>

void presents::log(const char* msg)
{
    NSLog(@"%s", msg);
}
